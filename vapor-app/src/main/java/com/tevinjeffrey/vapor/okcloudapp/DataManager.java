package com.tevinjeffrey.vapor.okcloudapp;

import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.FileToLargeException;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.UploadLimitException;
import com.tevinjeffrey.vapor.okcloudapp.model.AccountStatsModel;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppJsonItem;
import com.tevinjeffrey.vapor.okcloudapp.model.ItemModel;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.UploadModel;
import com.tevinjeffrey.vapor.okcloudapp.utils.ProgressListener;
import com.tevinjeffrey.vapor.okcloudapp.utils.ProgressiveTypedFile;
import com.tevinjeffrey.vapor.ui.login.LoginException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.*;

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
                                        return getListFromServer(makeListParams(integer, ItemType.ALL, false));
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
                if (count >= 1) {
                    deleteLocalItem(cloudAppItem);
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

    private void deleteLocalItem(CloudAppItem cloudAppItem) {
        CloudAppItem.executeQuery("DELETE FROM CLOUD_APP_ITEM WHERE ITEM_ID = ?",
                String.valueOf(cloudAppItem.getItemId()));
        Timber.i("Deleting item: %s", cloudAppItem.getItemId());

    }

    public Observable<CloudAppItem> deleteCloudItem(final CloudAppItem cloudAppItem) {
        return cloudAppService.deleteItem(String.valueOf(cloudAppItem.getItemId()))
                .map(convertItemModel())
                .doOnNext(new Action1<CloudAppItem>() {
                    @Override
                    public void call(CloudAppItem item) {
                        deleteLocalItem(cloudAppItem);
                    }
                });
    }

    public Observable<CloudAppItem> renameCloudItem(final CloudAppItem cloudAppItem, String newName) {
        CloudAppJsonItem jsonItem = new CloudAppJsonItem();
        CloudAppJsonItem.Item item = jsonItem.item = new CloudAppJsonItem.Item();
        item.name = newName;

        return cloudAppService.renameItem(String.valueOf(cloudAppItem.getItemId()), jsonItem)
                .map(convertItemModel())
                .map(saveToDb());
    }

    public Observable<CloudAppItem> getItemById(final long id, boolean refresh) {
        if (refresh) {
            return getItemFromServer(id);
        }
        return Observable.defer(new Func0<Observable<CloudAppItem>>() {
            @Override
            public Observable<CloudAppItem> call() {
                return Observable.just(CloudAppItem.findWithQuery(CloudAppItem.class, "SELECT * FROM ITEM_MODEL where ITEM_ID = ?",
                        String.valueOf(id)).get(0));
            }
        });
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

    private Observable<List<CloudAppItem>> getAllItems(final ItemType type) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        if (type == ItemType.ALL) {
            return Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return Observable.just(CloudAppItem.findWithQuery(CloudAppItem.class, "SELECT * FROM CLOUD_APP_ITEM ORDER BY ITEM_ID DESC"));
                }});
        }
        return Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
            @Override
            public Observable<List<CloudAppItem>> call() {
                return Observable.just(CloudAppItem
                        .findWithQuery(CloudAppItem.class, "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? ORDER BY ITEM_ID DESC",
                                type.toString().toLowerCase()));
            }
        });
    }

    public Observable<List<CloudAppItem>> getPopularItems(final ItemType type, boolean refresh) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        final Observable<List<CloudAppItem>> observable;

        if (type == ItemType.ALL) {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return  Observable.just(CloudAppItem.findWithQuery(CloudAppItem.class, "SELECT * FROM CLOUD_APP_ITEM ORDER BY VIEW_COUNTER DESC"));
                }});
        } else {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return   Observable.just(CloudAppItem
                            .findWithQuery(CloudAppItem.class, "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? ORDER BY VIEW_COUNTER DESC",
                                    type.toString().toLowerCase()));
                }});
        }
        if (refresh) {
            return refreshFirstPage().flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                    return observable;
                }
            });
        }
        return observable;
    }

    public Observable<List<CloudAppItem>> getFavoriteItems(final ItemType type, boolean refresh) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        final Observable<List<CloudAppItem>> observable;

        if (type == ItemType.ALL) {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return  Observable.just(CloudAppItem.findWithQuery(CloudAppItem.class,
                            "SELECT * FROM CLOUD_APP_ITEM WHERE FAVORITE = 1 ORDER BY ITEM_ID DESC"));
                }
            });
        } else {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return Observable.just(CloudAppItem
                                    .findWithQuery(CloudAppItem.class, "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND FAVORITE = 1 ORDER BY ITEM_ID, VIEW_COUNTER DESC",
                                            type.toString().toLowerCase()));
                }});
        }
        if (refresh) {
            return refreshFirstPage().flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                    return observable;
                }
            });
        }
        return observable;
    }

    public Observable<List<CloudAppItem>> getDeletedItems(final ItemType type) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        return getListFromServer(makeListParams(1, type, true));
    }

    public Observable<List<CloudAppItem>> getAllItems(final ItemType type, boolean refresh) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        final Observable<List<CloudAppItem>> observable = getAllItems(type);
        if (refresh) {
            return refreshFirstPage().flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                    return observable;
                }
            });
        }
        return observable;
    }

    @NonNull
    private Observable<List<CloudAppItem>> refreshFirstPage() {
        return getListFromServer(makeListParams(1, ItemType.ALL, false))
                .flatMap(reduceList()).map(saveToDb()).toList();

    }

    private Observable<CloudAppItem> getItemFromServer(long itemId) {
        return cloudAppService.getItem(String.valueOf(itemId))
                .map(convertItemModel())
                .map(saveToDb())
                ;
    }

    private Observable<List<CloudAppItem>> getListFromServer(Map<String, String> options) {
        return cloudAppService.listItems(options)
                .onErrorResumeNext(Observable.<List<ItemModel>>empty()).subscribeOn(Schedulers.io())
                .flatMap(new Func1<List<ItemModel>, Observable<List<CloudAppItem>>>() {
                    @Override
                    public Observable<List<CloudAppItem>> call(List<ItemModel> itemModels) {
                        return Observable.from(itemModels)
                                .map(convertItemModel()).toList();
                    }
                });
    }

    @NonNull
    private Func1<ItemModel, CloudAppItem> convertItemModel() {
        return new Func1<ItemModel, CloudAppItem>() {
            @Override
            public CloudAppItem call(ItemModel itemModel) {
                return new CloudAppItem(itemModel);
            }
        };
    }

    private Map<String, String> makeListParams(int page, ItemType type, boolean deleted) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("per_page", String.valueOf(DEFAULT_ITEM_LIMIT));
        if (type != ItemType.ALL) {
            params.put("type", type.toString().toLowerCase());
        }
        params.put("deleted", String.valueOf(deleted));

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
                .setEndpoint("http://f.cl.ly")
                .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
                .setConverter(new GsonConverter(new GsonBuilder()
                        .setPrettyPrinting()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()))
                .build();
        return restAdapter.create(AmazonUploadService.class);
    }

    @Subscribe
    public void loginEvent(LoginEvent event) {
        syncAllItems();
    }

    @Subscribe
    public void onUploadEvent(UploadEvent event) {
        getAllItems(ItemType.ALL, true)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<List<CloudAppItem>>() {
                    @Override
                    public void call(List<CloudAppItem> cloudAppItems) {
                        bus.post(new DatabaseUpdateEvent());
                    }
                })
                .subscribe();
    }

    private JSONObject createBody(String[] keys, Object[] values) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject item = new JSONObject();
        for (int i = 0; i < keys.length; i++) {
            item.put(keys[i], values [i]);
        }
        json.put("item", item);
        return json;
    }
}
