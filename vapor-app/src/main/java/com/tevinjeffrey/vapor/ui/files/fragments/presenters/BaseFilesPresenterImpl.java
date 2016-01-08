package com.tevinjeffrey.vapor.ui.files.fragments.presenters;

import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapor.ui.base.BasePresenter;
import com.tevinjeffrey.vapor.ui.files.FilesActivityPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.FilesFragmentView;
import com.tevinjeffrey.vapor.utils.RxUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class BaseFilesPresenterImpl extends BasePresenter<FilesFragmentView> implements FilesPresenter<FilesFragmentView> {

    private static final String TAG = BaseFilesPresenterImpl.class.getSimpleName();

    private Subscription mSubscription;
    private boolean isLoading;

    @Inject
    DataManager dataManager;
    @Inject
    UserManager userManager;
    @Inject
    Bus bus;
    @Inject
    FilesActivityPresenter layoutManager;

    ItemType itemType;

    public BaseFilesPresenterImpl() {
    }

    @Override
    public void loadData(boolean pullToRefresh, boolean refreshData, boolean useCursor) {
        if (!userManager.isLoggedIn()) return;
        if (getView() != null) getView().showLoading(pullToRefresh);

        RxUtils.unsubscribeIfNotNull(mSubscription);

        Subscriber<List<CloudAppItem>> mSubscriber = new Subscriber<List<CloudAppItem>>() {
            @Override
            public void onCompleted() {
                if (getView() != null)
                    getView().showLoading(false);
            }

            @Override
            public void onError(Throwable e) {
                //Lets the view decide what to display depending on what itemType of exception it is.
                if (getView() != null)
                    getView().showError(e);
                //Removes the animated loading drawable
                if (getView() != null) {
                    getView().showLoading(false);
                }

            }

            @Override
            public void onNext(List<CloudAppItem> data) {
                if (getView() != null) {
                    getView().setData(data);
                }
            }
        };

        DataManager.DataCursor cursor = new DataManager.DataCursor();
        if (useCursor) {
            if (getView() != null) {
                cursor = getView().getCursor();
            }
        }
        Observable<List<CloudAppItem>> dataObservable;
        FilesActivityPresenter.NavContext navContext = layoutManager.getNavContext();
        if (navContext == FilesActivityPresenter.NavContext.ALL) {
            dataObservable = dataManager.getAllItems(itemType, refreshData, cursor);
        } else if (navContext == FilesActivityPresenter.NavContext.POPULAR) {
            dataObservable = dataManager.getPopularItems(itemType, refreshData, cursor);

        } else if (navContext == FilesActivityPresenter.NavContext.FAVORITE) {
            dataObservable = dataManager.getFavoriteItems(itemType, refreshData, cursor);

        } else if (navContext == FilesActivityPresenter.NavContext.TRASH) {
            dataObservable = dataManager.getTrashItems(itemType, refreshData, cursor);
            dataManager.purgeDeletedItems();
        } else {
            throw new RuntimeException("Unknown nav context");
        }

        mSubscription = dataObservable
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        isLoading = true;
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        isLoading = false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSubscriber);
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public boolean shouldShowEmpty() {
        return userManager.isLoggedIn() && !dataManager.isSyncingAllItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(false, false, false);
    }

    @Override
    public String toString() {
        return "BaseFilesPresenterImpl{" +
                "itemType=" + itemType +
                '}';
    }
}
