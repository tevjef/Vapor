package com.tevinjeffrey.vapor.okcloudapp

import android.os.Parcel
import android.os.Parcelable

import com.orm.SugarRecord
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.RequestBody
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent
import com.tevinjeffrey.vapor.events.LogoutEvent
import com.tevinjeffrey.vapor.okcloudapp.exceptions.FileToLargeException
import com.tevinjeffrey.vapor.okcloudapp.exceptions.UploadLimitException
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppJsonItem
import com.tevinjeffrey.vapor.okcloudapp.model.ItemModel
import com.tevinjeffrey.vapor.okcloudapp.model.UploadModel
import com.tevinjeffrey.vapor.ui.login.LoginException
import com.tevinjeffrey.vapor.utils.RxUtils

import org.joda.time.DateTime

import java.util.HashMap
import java.util.LinkedHashMap

import jonathanfinerty.once.Once
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.schedulers.Schedulers
import timber.log.Timber

class DataManager(private val cloudAppService: CloudAppService, private val userManager: UserManager, private val bus: Bus, private val client: OkHttpClient) {
    private val SERVER_ITEM_LIMIT = 40
    private val MAX_ITEM_LIMIT = 1500
    var isSyncingAllItems: Boolean = false
        private set

    init {
        bus.register(this)
    }

    fun syncAllItems(notify: Boolean) {
        if (!isSyncingAllItems) {
            cloudAppService.getAccountStats().flatMap { accountStatsModel ->
                Observable.create(Observable.OnSubscribe<kotlin.Int> { subscriber ->
                    if (!subscriber.isUnsubscribed) {
                        var i = 1
                        while (i <= Math.ceil(accountStatsModel.items.toDouble() / SERVER_ITEM_LIMIT.toDouble()) + 1
                                && i < Math.ceil((MAX_ITEM_LIMIT / SERVER_ITEM_LIMIT).toDouble())) {
                            subscriber.onNext(i)
                            i++
                        }
                        subscriber.onCompleted()
                    }
                })
                .flatMap { integer -> getListFromServer(makeListParams(integer, ItemType.ALL, false, SERVER_ITEM_LIMIT)) }
                .subscribeOn(Schedulers.io())
                .flatMap { cloudAppItems ->
                    SugarRecord.updateInTx(cloudAppItems)
                    Observable.just(cloudAppItems)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate {
                isSyncingAllItems = false
                if (notify) {
                    bus.post(DatabaseUpdateEvent())
                }
            }
            .doOnSubscribe { isSyncingAllItems = true }
            .subscribe({ cloudAppItems -> Timber.i("Synced all items. Size: %s", cloudAppItems.size) },
                    { throwable -> Timber.e(throwable, "Error syncing all items") })

            getTrashItems(ItemType.ALL, true, DataCursor())
                .subscribe({ cloudAppItems -> Timber.i("Synced all deleted items. Size: %s", cloudAppItems.size) },
                        { throwable -> Timber.e(throwable, "Error syncing all deleted items") })
        }
    }

    private fun reduceList(): Func1<List<CloudAppItem>, Observable<CloudAppItem>> {
        return Func1 { cloudAppItems -> Observable.from(cloudAppItems) }
    }

    private fun saveToDb(): Func1<CloudAppItem, CloudAppItem> {
        return Func1 { cloudAppItem ->
            SugarRecord.updateInTx(cloudAppItem)
            Timber.i("Inserting or update item: %s", cloudAppItem.itemId)
            cloudAppItem
        }
    }

    private fun deleteLocalItem(cloudAppItem: CloudAppItem) {
        SugarRecord.delete(cloudAppItem)
        Timber.i("Deleting item: %s", cloudAppItem.itemId)
    }

    fun purgeDeletedItems() {
        val expired = DateTime.now().minusDays(7).millis
        Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java,
                "DELETE FROM CLOUD_APP_ITEM WHERE deleted_at <> -1 AND deleted_at < ?",
                expired.toString())).subscribe({ cloudAppItems -> Timber.i("Deleting items: %s", cloudAppItems.size) },
                { throwable -> Timber.e(throwable, "Error deleting stale items.") })
    }

    fun deleteCloudItem(cloudAppItem: CloudAppItem): Observable<CloudAppItem> {
        return cloudAppService.deleteItem(cloudAppItem.itemId.toString())
                .map(convertItemModel())
                .doOnNext{ deleteLocalItem(cloudAppItem) }
    }

    fun renameCloudItem(cloudAppItem: CloudAppItem, newName: String): Observable<CloudAppItem> {
        val jsonItem = CloudAppJsonItem()
        jsonItem.item = CloudAppJsonItem.Item(name = newName)

        return cloudAppService.renameItem(cloudAppItem.itemId.toString(), jsonItem).map(convertItemModel()).map(saveToDb())
    }

    fun bookmarkItem(name: String, url: String): Observable<CloudAppItem> {
        val jsonItem = CloudAppJsonItem()
        jsonItem.item = CloudAppJsonItem.Item(name = name, redirectUrl = url)

        return cloudAppService.bookmarkLink(jsonItem)
                .map(convertItemModel())
                .map(saveToDb())
                .retryWhen(RxUtils.RetryWithDelay(2, 2000))
    }

    fun upload(requestBody: CloudAppRequestBody): Observable<CloudAppItem> {
        return cloudAppService.newUpload(makeQueryMap(requestBody.fileName, requestBody.contentLength().toString()))
                .flatMap(Func1<UploadModel, rx.Observable<CloudAppItem>> { uploadModel ->
            if (uploadModel.params == null) {
                return@Func1 Observable.error<CloudAppItem>(UploadLimitException("Daily upload limit reached"))
            }
            if (requestBody.contentLength() > uploadModel.maxUploadSize) {
                return@Func1 Observable.error<CloudAppItem>(FileToLargeException("File too large for you current plan"))
            }
            cloudAppService.uploadFile(makeMultipartParams(uploadModel, requestBody))
                    .map(convertItemModel())
                    .map(saveToDb())
                    .retryWhen(RxUtils.RetryWithDelay(4, 2000))
        })
    }

    private fun makeQueryMap(name: String, size: String): Map<String, String> {
        val params = LinkedHashMap<String, String>()
        params.put("name", name)
        params.put("file_size", size)
        return params
    }

    private fun getAllItems(type: ItemType, cursor: DataCursor): Observable<List<CloudAppItem>> {
        if (!userManager.isLoggedIn) {
            return Observable.error<List<CloudAppItem>>(LoginException())
        }
        if (type == ItemType.ALL) {
            return Observable.defer {
                val query = "SELECT * FROM CLOUD_APP_ITEM WHERE deleted_at = -1 ORDER BY ITEM_ID DESC LIMIT ? OFFSET ?"
                Timber.d(query.replace("?", "%s"),
                        cursor.limit.toString(), cursor.offset.toString())
                Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java, query,
                        cursor.limit.toString(), cursor.offset.toString()))
            }
        }
        return Observable.defer {
            val query = "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND deleted_at = -1 ORDER BY ITEM_ID DESC LIMIT ? OFFSET ?"
            Timber.d(query.replace("?", "%s"),
                    type.toString().toLowerCase(), cursor.limit.toString(), cursor.offset.toString())
            Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java, query,
                    type.toString().toLowerCase(), cursor.limit.toString(), cursor.offset.toString()))
        }
    }

    fun getPopularItems(type: ItemType, refresh: Boolean, cursor: DataCursor): Observable<List<CloudAppItem>> {
        if (!userManager.isLoggedIn) {
            return Observable.error<List<CloudAppItem>>(LoginException())
        }
        val observable: Observable<List<CloudAppItem>>

        if (type == ItemType.ALL) {
            observable = Observable.defer {
                val query = "SELECT * FROM CLOUD_APP_ITEM WHERE deleted_at = -1 ORDER BY VIEW_COUNTER DESC LIMIT ? OFFSET ?"
                Timber.d(query.replace("?", "%s"), cursor.limit.toString(), cursor.offset.toString())
                Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java,
                        query, cursor.limit.toString(), cursor.offset.toString()))
            }
        } else {
            observable = Observable.defer {
                val query = "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND deleted_at = -1 ORDER BY VIEW_COUNTER DESC LIMIT ? OFFSET ?"
                Timber.d(query.replace("?", "%s"), type.toString().toLowerCase(), cursor.limit.toString(), cursor.offset.toString())
                Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java,
                        query, type.toString().toLowerCase(), cursor.limit.toString(), cursor.offset.toString()))
            }
        }
        if (refresh) {
            return refreshPage(1, false).flatMap { observable }
        }
        return observable.doOnNext { cloudAppItems -> cursor.offset += cloudAppItems.size }
    }

    fun getFavoriteItems(type: ItemType, refresh: Boolean, cursor: DataCursor): Observable<List<CloudAppItem>> {
        if (!userManager.isLoggedIn) {
            return Observable.error<List<CloudAppItem>>(LoginException())
        }
        val observable: Observable<List<CloudAppItem>>

        if (type == ItemType.ALL) {
            observable = Observable.defer {
                val query = "SELECT * FROM CLOUD_APP_ITEM WHERE IS_FAVORITE = 1 AND deleted_at = -1 ORDER BY ITEM_ID DESC LIMIT ? OFFSET ?"
                Timber.d(query.replace("?", "%s"), cursor.limit.toString(), cursor.offset.toString())
                Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java,
                        query, cursor.limit.toString(), cursor.offset.toString()))
            }
        } else {
            observable = Observable.defer {
                val query = "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND FAVORITE = 1 AND deleted_at = -1 ORDER BY ITEM_ID, VIEW_COUNTER DESC LIMIT ? OFFSET ?"
                Timber.d(query.replace("?", "%s"), type.toString().toLowerCase(), cursor.limit.toString(), cursor.offset.toString())
                Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java,
                        query, type.toString().toLowerCase(), cursor.limit.toString(), cursor.offset.toString()))
            }
        }
        if (refresh) {
            return refreshPage(1, false).flatMap { observable }
        }
        return observable.doOnNext { cloudAppItems -> cursor.offset += cloudAppItems.size }
    }

    fun getTrashItems(type: ItemType, refresh: Boolean, cursor: DataCursor): Observable<List<CloudAppItem>> {
        if (!userManager.isLoggedIn) {
            return Observable.error<List<CloudAppItem>>(LoginException())
        }
        val observable: Observable<List<CloudAppItem>>
        if (type == ItemType.ALL) {
            observable = Observable.defer {
                val query = "SELECT * FROM CLOUD_APP_ITEM WHERE DELETED_AT <> -1 ORDER BY DELETED_AT DESC LIMIT ? OFFSET ?"
                Timber.d(query.replace("?", "%s"), cursor.limit.toString(), cursor.offset.toString())
                Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java,
                        query, cursor.limit.toString(), cursor.offset.toString()))
            }
        } else {
            observable = Observable.defer {
                val query = "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND DELETED_AT <> -1 ORDER BY DELETED_AT DESC LIMIT ? OFFSET ?"
                Timber.d(query.replace("?", "%s"), type.toString().toLowerCase(), cursor.limit.toString(), cursor.offset.toString())
                Observable.just(SugarRecord.findWithQuery<CloudAppItem>(CloudAppItem::class.java,
                        query, type.toString().toLowerCase(), cursor.limit.toString(), cursor.offset.toString()))
            }
        }
        if (refresh) {
            return refreshPage(1, true).flatMap { observable }
        }
        return observable.doOnNext { cloudAppItems -> cursor.offset += cloudAppItems.size }
    }

    fun getAllItems(type: ItemType, refresh: Boolean, cursor: DataCursor): Observable<List<CloudAppItem>> {
        if (!userManager.isLoggedIn) {
            return Observable.error<List<CloudAppItem>>(LoginException())
        }
        val observable = getAllItems(type, cursor)
        if (refresh) {
            return refreshPage(1, false).flatMap { observable }
        }
        return observable.doOnNext { cloudAppItems -> cursor.offset += cloudAppItems.size }
    }

    private fun refreshPage(page: Int, deleted: Boolean): Observable<List<CloudAppItem>> {
        return getListFromServer(makeListParams(page, ItemType.ALL, deleted, SERVER_ITEM_LIMIT))
                .flatMap(reduceList())
                .map(saveToDb())
                .toList()
    }

    private fun getListFromServer(options: Map<String, String>): Observable<List<CloudAppItem>> {
        return cloudAppService.listItems(options)
                .retryWhen(RxUtils.RetryWithDelay(2, 2000))
                .subscribeOn(Schedulers.io())
                .flatMap { itemModels -> Observable.from(itemModels).map(convertItemModel()).toList() }
    }

    private fun convertItemModel(): Func1<ItemModel, CloudAppItem> {
        return Func1 { itemModel -> CloudAppItem(itemModel) }
    }

    private fun makeListParams(page: Int, type: ItemType, deleted: Boolean, pageSize: Int): Map<String, String> {
        val params = HashMap<String, String>()
        params.put("page", page.toString())
        params.put("per_page", pageSize.toString())
        if (type != ItemType.ALL) {
            params.put("type", type.toString().toLowerCase())
        }
        params.put("deleted", deleted.toString())

        return params
    }

    private fun makeMultipartParams(uploadModel: UploadModel, body: CloudAppRequestBody): Map<String, RequestBody> {
        val filename = "file\"; filename=\"" + body.fileName
        val params = LinkedHashMap<String, RequestBody>()
        val uploadParams = uploadModel.params
        params.put("AWSAccessKeyId", createStringBody(uploadParams?.AWSAccessKeyId!!))
        params.put("key", createStringBody(uploadParams?.key!!))
        params.put("acl", createStringBody(uploadParams?.acl!!))
        params.put("success_action_redirect", createStringBody(uploadParams?.successActionRedirect!!))
        params.put("signature", createStringBody(uploadParams?.signature!!))
        params.put("policy", createStringBody(uploadParams?.policy!!))
        params.put(filename, body)
        return params
    }

    private fun createStringBody(string: String): RequestBody {
        return RequestBody.create(MediaType.parse("text/plain"), string)
    }

    @Subscribe
    fun onLogoutEvent(event: LogoutEvent) {
        val cloudAppItems = SugarRecord.listAll<CloudAppItem>(CloudAppItem::class.java)
        SugarRecord.deleteInTx(cloudAppItems)
        Once.clearAll()
        SugarRecord.executeQuery("DELETE FROM sqlite_sequence WHERE NAME = 'CLOUD_APP_ITEM'")
        SugarRecord.executeQuery("VACUUM")
    }

    class DataCursor : Parcelable {
        val limit = 100
        var offset = 0
            set(value) {
                Timber.i("%s Cursor was %s now %s", owner, this.offset, offset)
                field = value
            }
        internal var owner = "Dummy"

        constructor(owner: String) {
            this.owner = owner
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(this.offset)
        }

        constructor() {
        }

        protected constructor(`in`: Parcel) {
            this.offset = `in`.readInt()
        }

        companion object {

            val CREATOR: Parcelable.Creator<DataCursor> = object : Parcelable.Creator<DataCursor> {
                override fun createFromParcel(source: Parcel): DataCursor {
                    return DataCursor(source)
                }

                override fun newArray(size: Int): Array<DataCursor?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {

        val SYNC_ALL_ITEMS = DataManager::class.java.getPackage().getName() + ".SYNC_ALL_ITEMS"
    }
}
