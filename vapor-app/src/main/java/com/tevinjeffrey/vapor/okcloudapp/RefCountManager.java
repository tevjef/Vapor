package com.tevinjeffrey.vapor.okcloudapp;

import android.content.Context;
import android.net.Uri;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RefCountManager {

    private final Bus bus;
    private final Context context;
    List<Uri> uris = new ArrayList<>();
    List<Integer> notificationIds = new ArrayList<>();

    public RefCountManager(Context context, Bus bus) {
        this.context = context;
        this.bus = bus;
        bus.register(this);
    }

    public void addNotificationId(int startId) {
        notificationIds.add(startId);
    }

    public void removeNotificationId(int startId) {
        notificationIds.remove(Integer.valueOf(startId));
    }

    public List<Integer> getNotificationIds() {
        return notificationIds;
    }

    public void addUri(Uri uri) {
        uris.add(uri);
    }

    private void cleanUpUri(final Uri uri) {
        // If the list of Uris contains an instance of the parameter uri, then create a file out of it
        // then delete it. Use of RxJava is completely unnecessary.
        Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                return Observable.just(uri)
                        .filter(new Func1<Uri, Boolean>() {
                            @Override
                            public Boolean call(Uri uri) {
                                return uris.contains(uri);
                            }
                        })
                        .flatMap(new Func1<Uri, Observable<File>>() {
                            @Override
                            public Observable<File> call(Uri uri) {
                                return Observable.just(FileUtils.getFile(context, uri));
                            }
                        })
                        .map(new Func1<File, Boolean>() {
                            @Override
                            public Boolean call(File file) {
                                return file.delete();
                            }
                        });
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e, "Error while attempting to delete a file created during upload");
            }

            @Override
            public void onNext(Boolean fileDeleted) {
                if (fileDeleted) {
                    Timber.i("Clean up successful");
                } else {
                    Timber.i("Clean up failed");
                }
            }
        });
    }

    @Subscribe
    public void onUploadEvent(UploadEvent uploadEvent) {
        if (uploadEvent.getUri() != null) {
            cleanUpUri(uploadEvent.getUri());
        }
    }
}
