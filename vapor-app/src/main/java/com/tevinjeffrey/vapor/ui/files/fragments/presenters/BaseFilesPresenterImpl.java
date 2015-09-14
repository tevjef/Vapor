package com.tevinjeffrey.vapor.ui.files.fragments.presenters;

import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapor.ui.base.BasePresenter;
import com.tevinjeffrey.vapor.ui.base.View;
import com.tevinjeffrey.vapor.ui.files.LayoutManager;
import com.tevinjeffrey.vapor.ui.files.LayoutManager.NavContext;
import com.tevinjeffrey.vapor.ui.files.fragments.views.FilesView;
import com.tevinjeffrey.vapor.utils.RxUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class BaseFilesPresenterImpl<V extends FilesView> extends BasePresenter<V> implements FilesPresenter<V> {

    private static final String TAG = BaseFilesPresenterImpl.class.getSimpleName();


    private Subscription mSubscription;
    private boolean isLoading;

    @Inject
    DataManager dataManager;
    @Inject
    Bus bus;
    @Inject
    LayoutManager layoutManager;

    public ItemType itemType;

    public BaseFilesPresenterImpl() {
    }

    @Override
    public void loadData(boolean pullToRefresh, boolean refreshData) {
        if (getView() != null)
            getView().showLoading(pullToRefresh);

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

                    if (data.size() == 0)
                        getView().showLayout(View.LayoutType.EMPTY);

                    if (data.size() > 0)
                        getView().showLayout(View.LayoutType.LIST);
                }
            }
        };

        Observable<List<CloudAppItem>> dataObservable;
        NavContext navContext =layoutManager.getNavContext();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        if (navContext == NavContext.ALL) {
            dataObservable = dataManager.getAllItems(itemType, refreshData);
        } else if (navContext == NavContext.POPULAR) {
            dataObservable = dataManager.getPopularItems(itemType, refreshData);

        } else if (navContext == NavContext.FAVORITE) {
            dataObservable = dataManager.getFavoriteItems(itemType, refreshData);

        } else if (navContext == NavContext.TRASH) {
            dataObservable = dataManager.getDeletedItems(itemType);
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
    public String toString() {
        return "BaseFilesPresenterImpl{" +
                "itemType=" + itemType +
                '}';
    }
}
