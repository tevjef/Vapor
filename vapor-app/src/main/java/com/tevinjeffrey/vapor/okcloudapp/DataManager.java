package com.tevinjeffrey.vapor.okcloudapp;

import android.support.annotation.NonNull;

import com.orm.SugarRecord;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.AppLaunchEvent;
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapor.events.LogoutEvent;
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.FileToLargeException;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.UploadLimitException;
import com.tevinjeffrey.vapor.okcloudapp.model.AccountStatsModel;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppJsonItem;
import com.tevinjeffrey.vapor.okcloudapp.model.ItemModel;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.UploadModel;
import com.tevinjeffrey.vapor.okcloudapp.utils.ProgressListener;
import com.tevinjeffrey.vapor.okcloudapp.utils.ProgressiveRequestBody;
import com.tevinjeffrey.vapor.ui.login.LoginException;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class DataManager {

    private final CloudAppService cloudAppService;
    private final Bus bus;
    private final UserManager userManager;

    private final int SERVER_ITEM_LIMIT = 40;
    private final int MAX_ITEM_LIMIT = 500;
    private final OkHttpClient client;

    public DataManager(CloudAppService cloudAppService, UserManager userManager, Bus bus, OkHttpClient client) {
        this.cloudAppService = cloudAppService;
        this.bus = bus;
        this.userManager = userManager;
        this.client = client;
        bus.register(this);
    }

    public void syncAllItems() {
        cloudAppService.getAccountStats()
                .flatMap(new Func1<AccountStatsModel, Observable<List<CloudAppItem>>>() {
                    @Override
                    public Observable<List<CloudAppItem>> call(final AccountStatsModel accountStatsModel) {
                        return Observable.create(new Observable.OnSubscribe<Integer>() {
                            @Override
                            public void call(Subscriber<? super Integer> subscriber) {
                                if (!subscriber.isUnsubscribed()) {
                                    SugarRecord.deleteAll(CloudAppItem.class);
                                    for (int i = 1; i <= Math.ceil((double)accountStatsModel.getItems() / (double) SERVER_ITEM_LIMIT)
                                            && i < MAX_ITEM_LIMIT / SERVER_ITEM_LIMIT; i++) {
                                        subscriber.onNext(i);
                                    }
                                    subscriber.onCompleted();
                                }
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .flatMap(new Func1<Integer, Observable<List<CloudAppItem>>>() {
                            @Override
                            public Observable<List<CloudAppItem>> call(Integer integer) {
                                return getListFromServer(makeListParams(integer, ItemType.ALL, false));
                            }
                        }).subscribeOn(Schedulers.io())
                        .flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                            @Override
                            public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                                SugarRecord.updateInTx(cloudAppItems);
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
                .subscribe(new Action1<List<CloudAppItem>>() {
                    @Override
                    public void call(List<CloudAppItem> cloudAppItems) {
                        Timber.i("Synced all items. Size: %s", cloudAppItems.size());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Error syncing all items");
                    }
                });

                getTrashItems(ItemType.ALL, true, new DataCursor())
                        .subscribe(new Action1<List<CloudAppItem>>() {
                            @Override
                            public void call(List<CloudAppItem> cloudAppItems) {
                                Timber.i("Synced all deleted items. Size: %s", cloudAppItems.size());
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Timber.e(throwable, "Error syncing all deleted items");
                            }
                        });
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
                cloudAppItem.update();
                Timber.i("Inserting or update item: %s", cloudAppItem.getItemId());
                return cloudAppItem;
            }
        };
    }

    private void deleteLocalItem(CloudAppItem cloudAppItem) {
        SugarRecord.delete(cloudAppItem);
        Timber.i("Deleting item: %s", cloudAppItem.getItemId());
    }

    public void purgeDeletedItems() {
        long expired = DateTime.now().minusDays(7).getMillis();
        Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                "DELETE FROM CLOUD_APP_ITEM WHERE deleted_at <> -1 AND deleted_at < ?",
                String.valueOf(expired)))
                .subscribe(new Action1<List<CloudAppItem>>() {
                    @Override
                    public void call(List<CloudAppItem> cloudAppItems) {
                        Timber.i("Deleting items: %s", cloudAppItems.size());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Error deleting stale items.");
                    }
                });
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

    public Observable<CloudAppItem> upload(final File file, final ProgressListener listener) {
        return cloudAppService.newUpload(makeQueryMap(file))
                .flatMap(new Func1<UploadModel, Observable<CloudAppItem>>() {
                    @Override
                    public Observable<CloudAppItem> call(final UploadModel uploadModel) {
                        if (uploadModel.getParams() == null) {
                            return Observable.error(new UploadLimitException("Daily upload limit reached"));
                        }
                        if (file.length() > uploadModel.getMax_upload_size()) {
                            return Observable.error(new FileToLargeException("File too large for you current plan"));
                        }
                        ProgressiveRequestBody requestBody = new ProgressiveRequestBody(file, listener);
                        return cloudAppService.uploadFile(String.valueOf(file.length()), makeMultipartParams(uploadModel, requestBody));
                    }
                });
    }

    private Map<String, String> makeQueryMap(File file) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("name", file.getName());
        params.put("file_size", String.valueOf(file.length()));
        return params;
    }

    private Observable<List<CloudAppItem>> getAllItems(final ItemType type, final DataCursor cursor) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        if (type == ItemType.ALL) {
            return Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                            "SELECT * FROM CLOUD_APP_ITEM WHERE deleted_at = -1 ORDER BY ITEM_ID DESC LIMIT ? OFFSET ?",
                            String.valueOf(cursor.getLimit()), String.valueOf(cursor.getOffset())));
                }});
        }
        return Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
            @Override
            public Observable<List<CloudAppItem>> call() {
                return Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                        "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND deleted_at = -1 ORDER BY ITEM_ID DESC LIMIT ? OFFSET ?",
                                type.toString().toLowerCase(), String.valueOf(cursor.getLimit()), String.valueOf(cursor.getOffset())));
            }
        });
    }

    public Observable<List<CloudAppItem>> getPopularItems(final ItemType type, boolean refresh, final DataCursor cursor) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        final Observable<List<CloudAppItem>> observable;

        if (type == ItemType.ALL) {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return  Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                            "SELECT * FROM CLOUD_APP_ITEM WHERE deleted_at = -1 ORDER BY VIEW_COUNTER DESC LIMIT ? OFFSET ?",
                            String.valueOf(cursor.getLimit()), String.valueOf(cursor.getOffset())));
                }});
        } else {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                            "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND deleted_at = -1 ORDER BY VIEW_COUNTER DESC LIMIT ? OFFSET ?",
                                    type.toString().toLowerCase(), String.valueOf(cursor.getLimit()), String.valueOf(cursor.getOffset())));
                }});
        }
        if (refresh) {
            return refreshPage(1, false)
                    .flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                    return observable;
                }
            });
        }
        return observable;
    }

    public Observable<List<CloudAppItem>> getFavoriteItems(final ItemType type, boolean refresh, final DataCursor cursor) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        final Observable<List<CloudAppItem>> observable;

        if (type == ItemType.ALL) {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return  Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                            "SELECT * FROM CLOUD_APP_ITEM WHERE FAVORITE = 1 AND deleted_at = -1 ORDER BY ITEM_ID DESC LIMIT ? OFFSET ?",
                            String.valueOf(cursor.getLimit()), String.valueOf(cursor.getOffset())));
                }
            });
        } else {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                            "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND FAVORITE = 1 AND deleted_at = -1 ORDER BY ITEM_ID, VIEW_COUNTER DESC LIMIT ? OFFSET ?",
                                            type.toString().toLowerCase(), String.valueOf(cursor.getLimit()), String.valueOf(cursor.getOffset())));
                }});
        }
        if (refresh) {
            return refreshPage(1, false).flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                    return observable;
                }
            });
        }
        return observable;
    }

    public Observable<List<CloudAppItem>> getTrashItems(final ItemType type, boolean refresh, final DataCursor cursor) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        final Observable<List<CloudAppItem>> observable;
        if (type == ItemType.ALL) {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return  Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                            "SELECT * FROM CLOUD_APP_ITEM WHERE DELETED_AT <> -1 ORDER BY ITEM_ID DESC LIMIT ? OFFSET ?",
                            String.valueOf(cursor.getLimit()), String.valueOf(cursor.getOffset())));
                }
            });
        } else {
            observable = Observable.defer(new Func0<Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call() {
                    return Observable.just(SugarRecord.findWithQuery(CloudAppItem.class,
                            "SELECT * FROM CLOUD_APP_ITEM WHERE ITEM_TYPE = ? AND DELETED_AT <> -1 ORDER BY ITEM_ID, VIEW_COUNTER DESC LIMIT ? OFFSET ?",
                            type.toString().toLowerCase(), String.valueOf(cursor.getLimit()), String.valueOf(cursor.getOffset())));
                }});
        }
        if (refresh) {
            return refreshPage(1, true).flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                    return observable;
                }
            });
        }
        return observable;
    }

    public Observable<List<CloudAppItem>> getAllItems(final ItemType type, boolean refresh, final DataCursor cursor) {
        if (!userManager.isLoggedIn()) {
            return Observable.error(new LoginException());
        }
        final Observable<List<CloudAppItem>> observable = getAllItems(type, cursor).doOnNext(new Action1<List<CloudAppItem>>() {
            @Override
            public void call(List<CloudAppItem> cloudAppItems) {
                cursor.setOffset(cursor.getOffset() + cloudAppItems.size());
            }
        });
        if (refresh) {
            return refreshPage(1, false).flatMap(new Func1<List<CloudAppItem>, Observable<List<CloudAppItem>>>() {
                @Override
                public Observable<List<CloudAppItem>> call(List<CloudAppItem> cloudAppItems) {
                    return observable;
                }
            });
        }
        return observable;
    }

    @NonNull
    private Observable<List<CloudAppItem>> refreshPage(int page, boolean deleted) {
        return getListFromServer(makeListParams(page, ItemType.ALL, deleted))
                .flatMap(reduceList())
                .map(saveToDb())
                .toList();

    }

    private Observable<CloudAppItem> getItemFromServer(long itemId) {
        return cloudAppService.getItem(String.valueOf(itemId))
                .map(convertItemModel())
                .map(saveToDb());
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
        params.put("per_page", String.valueOf(SERVER_ITEM_LIMIT));
        if (type != ItemType.ALL) {
            params.put("type", type.toString().toLowerCase());
        }
        params.put("deleted", String.valueOf(deleted));

        return params;
    }

    private Map<String, RequestBody> makeMultipartParams(UploadModel uploadModel, ProgressiveRequestBody body) {
        String filename = "file\"; filename=\"" + body.getFile().getName();
        Map<String, RequestBody> params = new LinkedHashMap<>();
        params.put("AWSAccessKeyId", createStringBody(uploadModel.getParams().getAWSAccessKeyId()));
        params.put("key", createStringBody(uploadModel.getParams().getKey()));
        params.put("acl", createStringBody(uploadModel.getParams().getAcl()));
        params.put("success_action_redirect", createStringBody(uploadModel.getParams().getSuccess_action_redirect()));
        params.put("signature", createStringBody(uploadModel.getParams().getSignature()));
        params.put("policy",createStringBody( uploadModel.getParams().getPolicy()));
        params.put(filename, body);
        return params;
    }

    private RequestBody createStringBody(String string) {
        return RequestBody.create(MediaType.parse("text/plain"), string);
    }

    @Subscribe
    public void onLoginEvent(LoginEvent event) {
        syncAllItems();
    }

    @Subscribe
    public void onLaunchEvent(AppLaunchEvent event) {
        syncAllItems();
    }

    @Subscribe
    public void onLogoutEvent(LogoutEvent event) {
        SugarRecord.deleteAll(CloudAppItem.class);
        CloudAppItem.executeQuery("VACUUM");
    }

    @Subscribe
    public void onUploadEvent(UploadEvent event) {
        refreshPage(1, false)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Timber.e(throwable, "Error refreshing after upload.");
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
