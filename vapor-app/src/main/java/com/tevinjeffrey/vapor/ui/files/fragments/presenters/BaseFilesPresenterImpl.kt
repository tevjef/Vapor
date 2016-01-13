package com.tevinjeffrey.vapor.ui.files.fragments.presenters

import com.squareup.otto.Bus
import com.tevinjeffrey.vapor.okcloudapp.DataManager
import com.tevinjeffrey.vapor.okcloudapp.UserManager
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType
import com.tevinjeffrey.vapor.ui.base.BasePresenter
import com.tevinjeffrey.vapor.ui.files.FilesActivityPresenter
import com.tevinjeffrey.vapor.ui.files.fragments.FilesFragmentView
import com.tevinjeffrey.vapor.utils.RxUtils

import javax.inject.Inject

import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.schedulers.Schedulers

open class BaseFilesPresenterImpl : BasePresenter<FilesFragmentView>(), FilesPresenter<FilesFragmentView> {

    private var mSubscription: Subscription? = null
    private var isLoading: Boolean = false

    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var userManager: UserManager
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var layoutManager: FilesActivityPresenter

    lateinit var itemType: ItemType

    override fun loadData(pullToRefresh: Boolean, refreshData: Boolean, useCursor: Boolean) {
        if (!userManager.isLoggedIn) return
        if (view != null) view!!.showLoading(pullToRefresh)

        RxUtils.unsubscribeIfNotNull(mSubscription)

        val mSubscriber = object : Subscriber<List<CloudAppItem>>() {
            override fun onCompleted() {
                if (view != null)
                    view!!.showLoading(false)
            }

            override fun onError(e: Throwable) {
                //Lets the view decide what to display depending on what itemType of exception it is.
                if (view != null)
                    view!!.showError(e)
                //Removes the animated loading drawable
                if (view != null) {
                    view!!.showLoading(false)
                }

            }

            override fun onNext(data: List<CloudAppItem>) {
                if (view != null) {
                    view!!.setData(data)
                }
            }
        }

        var cursor = DataManager.DataCursor()
        if (useCursor) {
            if (view != null) {
                cursor = view!!.cursor
            }
        }
        val dataObservable: Observable<List<CloudAppItem>>
        val navContext = layoutManager.navContext
        if (navContext === FilesActivityPresenter.NavContext.ALL) {
            dataObservable = dataManager.getAllItems(itemType, refreshData, cursor)
        } else if (navContext === FilesActivityPresenter.NavContext.POPULAR) {
            dataObservable = dataManager.getPopularItems(itemType, refreshData, cursor)

        } else if (navContext === FilesActivityPresenter.NavContext.FAVORITE) {
            dataObservable = dataManager.getFavoriteItems(itemType, refreshData, cursor)

        } else if (navContext === FilesActivityPresenter.NavContext.TRASH) {
            dataObservable = dataManager.getTrashItems(itemType, refreshData, cursor)
            dataManager.purgeDeletedItems()
        } else {
            throw RuntimeException("Unknown nav context")
        }

        mSubscription = dataObservable
                .doOnSubscribe { isLoading = true }
                .doOnTerminate { isLoading = false }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSubscriber)
    }

    override val isNotLoading: Boolean
        get() = !isLoading

    override fun shouldShowEmpty(): Boolean {
        return userManager.isLoggedIn && !dataManager.isSyncingAllItems
    }

    override fun onResume() {
        super.onResume()
        loadData(false, false, false)
    }

    override fun toString(): String {
        return "BaseFilesPresenterImpl{itemType=$itemType}"
    }

    companion object {

        private val TAG = BaseFilesPresenterImpl::class.java.simpleName
    }
}
