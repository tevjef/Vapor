package com.tevinjeffrey.vapor.ui.files;

import android.app.DownloadManager;
import android.net.Uri;

import com.tevinjeffrey.vapor.BuildConfig;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.utils.RxUtils;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;

public class BottomSheetPresenterImpl implements BottomSheetPresenter {
    private final BottomSheetView mBottomSheetView;
    @Inject
    DataManager dataManager;
    @Inject
    DownloadManager downloadManager;

    CloudAppItem mCloudAppItem;
    private Subscription mRenameSubscription;
    private Subscription mDeleteSubscription;

    public BottomSheetPresenterImpl(BottomSheetView sheetView, CloudAppItem mCloudAppItem) {
        this.mCloudAppItem = mCloudAppItem;
        this.mBottomSheetView = sheetView;
    }

    @Override
    public void downloadFile() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mCloudAppItem.getDownloadUrl()));
        request.setDescription("Downloading " + mCloudAppItem.getName());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(true);
        downloadManager.enqueue(request);
    }

    @Override
    public void renameFile(String newName) {
        final String oldName = mCloudAppItem.getName();
        if (isViewAttached()) {
            mCloudAppItem.setName(newName);
            getView().rename(mCloudAppItem);
            getView().showLoading(true);

        }

        RxUtils.unsubscribeIfNotNull(mRenameSubscription);

        mRenameSubscription = dataManager.renameCloudItem(mCloudAppItem, newName)
                .compose(new RxUtils.AndroidSchedulerTransformer<CloudAppItem>())
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        if (isViewAttached()) {
                            getView().showLoading(false);
                        }
                    }
                })
                .subscribe(new Subscriber<CloudAppItem>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            mCloudAppItem.setName(oldName);
                            getView().rename(mCloudAppItem);
                            String extra ="";
                            if (BuildConfig.DEBUG) {
                                extra = e.getMessage();
                            }
                            getView().showError(String.format("Could not rename file. %s", extra));
                        }
                    }

                    @Override
                    public void onNext(CloudAppItem item) {
                        if (isViewAttached()) {
                            mCloudAppItem.setName(item.getName());
                            getView().rename(mCloudAppItem);
                        }
                    }
                });
    }

    @Override
    public void deleteFile() {
        if (isViewAttached()) {
            getView().showLoading(true);
        }

        RxUtils.unsubscribeIfNotNull(mDeleteSubscription);

        mDeleteSubscription = dataManager.deleteCloudItem(mCloudAppItem)
                .compose(new RxUtils.AndroidSchedulerTransformer<CloudAppItem>())
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        if (isViewAttached()) {
                            getView().showLoading(false);
                        }
                    }
                })
                .subscribe(new Subscriber<CloudAppItem>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            mCloudAppItem.setDeletedAt(null);
                            getView().deleteItem(mCloudAppItem);

                            String extra ="";
                            if (BuildConfig.DEBUG) {
                                extra = e.getMessage();
                            }
                            getView().showError(String.format("Could not delete file. %s", extra));
                        }
                    }

                    @Override
                    public void onNext(CloudAppItem item) {
                        if (isViewAttached()) {
                            mCloudAppItem = item;
                            getView().hideSheet();
                            getView().deleteItem(mCloudAppItem);
                        }
                    }
                });
    }

    private BottomSheetView getView() {
        return mBottomSheetView;
    }

    private boolean isViewAttached() {
        return mBottomSheetView != null;
    }
}
