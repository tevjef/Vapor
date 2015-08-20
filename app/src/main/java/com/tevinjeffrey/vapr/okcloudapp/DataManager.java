package com.tevinjeffrey.vapr.okcloudapp;

import android.support.annotation.NonNull;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapr.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapr.events.UploadEvent;
import com.tevinjeffrey.vapr.okcloudapp.exceptions.FileToLargeException;
import com.tevinjeffrey.vapr.okcloudapp.exceptions.UploadLimitException;
import com.tevinjeffrey.vapr.okcloudapp.model.AccountStatsModel;
import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapr.okcloudapp.model.ItemModel;
import com.tevinjeffrey.vapr.events.LoginEvent;
import com.tevinjeffrey.vapr.okcloudapp.model.UploadModel;
import com.tevinjeffrey.vapr.okcloudapp.utils.ProgressListener;
import com.tevinjeffrey.vapr.okcloudapp.utils.ProgressiveTypedFile;
import com.tevinjeffrey.vapr.ui.login.LoginException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.client.Client;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem.*;

public class DataManager {

    private final CloudAppService cloudAppService;
    private final Bus bus;
    private final UserManager userManager;
    private final Client client;

    private final int DEFAULT_ITEM_LIMIT = 40;
    private final int MAX_ITEM_LIMIT = 500;

    public DataManager(CloudAppService cloudAppService, UserManager userManager, Bus bus, Client client) {
        this.cloudAppService = cloudAppService;
        this.bus = bus;
        this.userManager = userManager;
        this.client = client;

        bus.register(this);
    }

    private void syncAllItems() {
        cloudAppService.getAccountStats()
                .flatMap(new Func1<AccountStatsModel, Observable<List<CloudAppItem>>>() {
                    @Override
                    public Observable<List<CloudAppItem>> call(final AccountStatsModel accountStatsModel) {
                        return Observable.create(new Observable.OnSubscribe<Integer>() {
                            @Override
                            public void call(Subscriber<? super Integer> subscriber) {
                                if (!subscriber.isUnsubscribed()) {
                                    CloudAppItem.deleteAll(CloudAppItem.class);
                                    for (int i = 1; i <= Math.ceil((double)accountStatsModel.getItems() / (double)DEFAULT_ITEM_LIMIT)
                                            && i < MAX_ITEM_LIMIT / DEFAULT_ITEM_LIMIT; i++) {
                                        subscriber.onNext(i);
                                    }
                                    subscriber.onCompleted();
                                }
                            }
                        }).subscribeOn(Schedulers.io())
                                .flatMap(new Func1<Integer, Observable<List<CloudAppItem>>>() {
                                    @Override
                                    public Observable<List<CloudAppItem>> call(Integer integer) {
                                        return getFromServer(integer, ItemType.ALL);
                                    }
                                }).subscribeOn(Schedulers.io())
                                .flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                                    @Override
                                    public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                                        CloudAppItem.saveInTx(cloudAppItems);
                                        return Observable.just(cloudAppItems);
                                    }
                                });

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        bus.post(new DatabaseUpdateEvent());
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
                .subscribe();
    }

    @NonNull
    private Func1<List<CloudAppItem>, Observable<CloudAppItem>> reduceList() {
        return new Func1<List<CloudAppItem>, Observable<CloudAppItem>>() {
            @Override
            public Observable<CloudAppItem> call(List<CloudAppItem> cloudAppItems) {
                return Observable.from(cloudAppItems);
            }
        };
    }

    @NonNull
    private Func1<CloudAppItem, CloudAppItem> saveToDb() {
        return new Func1<CloudAppItem, CloudAppItem>() {
            @Override
            public CloudAppItem call(CloudAppItem cloudAppItem) {
                long count = getItemCount(cloudAppItem);
                if (count == 1) {
                    deleteItem(cloudAppItem);
                }
                insertItem(cloudAppItem);
                return cloudAppItem;
            }
        };
    }

    private void insertItem(CloudAppItem cloudAppItem) {
        cloudAppItem.save();
        Timber.i("Inserting item: %s", cloudAppItem.getItemId());

    }

    private long getItemCount(CloudAppItem cloudAppItem) {
        return CloudAppItem.count(CloudAppItem.class,
                "ITEM_ID = ?", new String[]{String.valueOf(cloudAppItem.getItemId())});
    }

    private void deleteItem(CloudAppItem cloudAppItem) {
        CloudAppItem.executeQuery("DELETE FROM CLOUD_APP_ITEM WHERE ITEM_ID = ?",
                String.valueOf(cloudAppItem.getItemId()));
        Timber.i("Deleting item: %s", cloudAppItem.getItemId());

    }

    public CloudAppItem getItemById(long id) {
        return CloudAppItem.findWithQuery(CloudAppItem.class, "SELECT * FROM ITEM_MODEL where ITEM_ID = ?",
                String.valueOf(id)).get(0);
    }

    public Observable<CloudAppItem> upload(final File file, final ProgressListener listener) {
        return cloudAppService.newUpload()
                .flatMap(new Func1<UploadModel, Observable<CloudAppItem>>() {
                    @Override
                    public Observable<CloudAppItem> call(final UploadModel uploadModel) {
                        if (uploadModel.getParams() == null) {
                            return Observable.error(new UploadLimitException("Daily upload limit reached"));
                        }
                        if (file.length() > uploadModel.getMax_upload_size()) {
                            return Observable.error(new FileToLargeException("File too large for you current plan"));
                        }

                        AmazonUploadService amazonUploadService = getAmazonUploadService(uploadModel.getUrl(), client);
                        ProgressiveTypedFile typedByteArray = new ProgressiveTypedFile(file, listener);
                        return amazonUploadService.postFile(makeMultipartParams(uploadModel), typedByteArray);
                    }
                });
    }

    public Observable<List<CloudAppItem>> getItems(final ItemType type) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        if (type == ItemType.ALL) {
            return Observable.just(CloudAppItem.findWithQuery(CloudAppItem.class, "SELECT * FROM CLOUD_APP_ITEM ORDER BY ITEM_ID DESC"));
        }
        return Observable.just(CloudAppItem
                .findWithQuery(CloudAppItem.class, "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? ORDER BY ITEM_ID DESC",
                        type.toString().toLowerCase()));

    }

    public Observable<List<CloudAppItem>> refreshAndGetItems(final ItemType type) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        return
                //Gets the first 40 items of the given type from server and updates the database.
                getFromServer(1, type)
                .flatMap(reduceList()).map(saveToDb()).toList()
                //Now get the items from the database.
                .flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                    @Override
                    public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                        return getItems(type);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<CloudAppItem>, List<CloudAppItem>>() {
                    @Override
                    public List<CloudAppItem> call(List<CloudAppItem> cloudAppItems) {
                        bus.post(new DatabaseUpdateEvent());
                        return cloudAppItems;
                    }
                });

    }

    private Observable<List<CloudAppItem>> getFromServer(int page, ItemType type) {
        return cloudAppService.listItems(makeListParams(page, type))
                .onErrorResumeNext(Observable.<List<ItemModel>>empty()).subscribeOn(Schedulers.io())
                .flatMap(new Func1<List<ItemModel>, Observable<List<CloudAppItem>>>() {
                    @Override
                    public Observable<List<CloudAppItem>> call(List<ItemModel> itemModels) {
                        return Observable.from(itemModels)
                                .map(new Func1<ItemModel, CloudAppItem>() {
                                    @Override
                                    public CloudAppItem call(ItemModel itemModel) {
                                        return new CloudAppItem(itemModel);
                                    }
                                }).toList();
                    }
                });
    }

    private Map<String, String> makeListParams(int page, ItemType type) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("per_page", String.valueOf(DEFAULT_ITEM_LIMIT));
        if (type != ItemType.ALL) {
            params.put("type", type.toString().toLowerCase());
        }
        params.put("deleted", "false");

        return params;
    }

    private Map<String, String> makeMultipartParams(UploadModel uploadModel) {
        Map<String, String> params = new HashMap<>();
        params.put("AWSAccessKeyId", uploadModel.getParams().getAWSAccessKeyId());
        params.put("key", uploadModel.getParams().getKey());
        params.put("acl", uploadModel.getParams().getAcl());
        params.put("success_action_redirect", uploadModel.getParams().getSuccess_action_redirect());
        params.put("signature", uploadModel.getParams().getSignature());
        params.put("policy", uploadModel.getParams().getPolicy());
        return params;
    }

    public AmazonUploadService getAmazonUploadService(String endpoint, Client client) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint(endpoint)
                .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
                .build();
        return restAdapter.create(AmazonUploadService.class);
    }

    @Subscribe
    public void loginEvent(LoginEvent event) {
        syncAllItems();
    }

    @Subscribe
    public void onUploadEvent(UploadEvent event) {
        refreshAndGetItems(ItemType.ALL)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
                .subscribe();
    }

}
