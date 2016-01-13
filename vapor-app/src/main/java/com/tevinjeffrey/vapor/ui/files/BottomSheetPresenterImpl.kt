package com.tevinjeffrey.vapor.ui.files

import android.app.DownloadManager
import android.net.Uri

import com.tevinjeffrey.vapor.BuildConfig
import com.tevinjeffrey.vapor.okcloudapp.DataManager
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.utils.RxUtils

import javax.inject.Inject

import rx.Subscriber
import rx.Subscription
import rx.functions.Action0

class BottomSheetPresenterImpl(private val view: BottomSheetView, internal var mCloudAppItem:

CloudAppItem) : BottomSheetPresenter {
    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var downloadManager: DownloadManager
    private var mRenameSubscription: Subscription? = null
    private var mDeleteSubscription: Subscription? = null

    override fun downloadFile() {
        val request = DownloadManager.Request(Uri.parse(mCloudAppItem.downloadUrl))
        request.setDescription("Downloading " + mCloudAppItem.name!!)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setVisibleInDownloadsUi(true)
        downloadManager.enqueue(request)
    }

    override fun renameFile(newName: String) {
        val oldName = mCloudAppItem.name
        if (isViewAttached) {
            mCloudAppItem.name = newName
            view.rename(mCloudAppItem)
            view.showLoading(true)

        }

        RxUtils.unsubscribeIfNotNull(mRenameSubscription)

        mRenameSubscription = dataManager.renameCloudItem(mCloudAppItem, newName).compose(RxUtils.AndroidSchedulerTransformer<CloudAppItem>()).doOnTerminate {
            if (isViewAttached) {
                view.showLoading(false)
            }
        }.subscribe(object : Subscriber<CloudAppItem>() {
            override fun onCompleted() {

            }

            override fun onError(e: Throwable) {
                if (isViewAttached) {
                    mCloudAppItem.name = oldName
                    view.rename(mCloudAppItem)
                    var extra = ""
                    if (BuildConfig.DEBUG) {
                        extra = e.message?:""
                    }
                    view.showError("Could not rename file. %s".format(extra))
                }
            }

            override fun onNext(item: CloudAppItem) {
                if (isViewAttached) {
                    mCloudAppItem.name = item.name
                    view.rename(mCloudAppItem)
                }
            }
        })
    }

    override fun deleteFile() {
        if (isViewAttached) {
            view.showLoading(true)
        }

        RxUtils.unsubscribeIfNotNull(mDeleteSubscription)

        mDeleteSubscription = dataManager.deleteCloudItem(mCloudAppItem).compose(RxUtils.AndroidSchedulerTransformer<CloudAppItem>()).doOnTerminate {
            if (isViewAttached) {
                view.showLoading(false)
            }
        }.subscribe(object : Subscriber<CloudAppItem>() {
            override fun onCompleted() {

            }

            override fun onError(e: Throwable) {
                if (isViewAttached) {
                    mCloudAppItem.deletedAt = null
                    view.deleteItem(mCloudAppItem)

                    var extra = ""
                    if (BuildConfig.DEBUG) {
                        extra = e.message?:""
                    }
                    view.showError("Could not delete file. %s".format(extra))
                }
            }

            override fun onNext(item: CloudAppItem) {
                if (isViewAttached) {
                    mCloudAppItem = item
                    view.hideSheet()
                    view.deleteItem(mCloudAppItem)
                }
            }
        })
    }

    private val isViewAttached: Boolean
        get() = view != null
}
