package com.tevinjeffrey.vapr.ui.files.fragments.presenters.base;

import com.squareup.otto.Bus;
import com.tevinjeffrey.vapr.okcloudapp.DataManager;
import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapr.ui.base.BasePresenter;
import com.tevinjeffrey.vapr.ui.base.View;
import com.tevinjeffrey.vapr.ui.files.fragments.FilesPresenter;
import com.tevinjeffrey.vapr.ui.files.fragments.FilesView;
import com.tevinjeffrey.vapr.utils.RxUtils;

import java.util.List;

import javax.inject.Inject;

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


        mSubscription = (refreshData?dataManager.refreshAndGetItems(itemType):dataManager.getItems(itemType))
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
