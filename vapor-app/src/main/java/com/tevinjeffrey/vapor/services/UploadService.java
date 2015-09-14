package com.tevinjeffrey.vapor.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaprApp;
import com.tevinjeffrey.vapor.utils.VaprUtils;
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.FileToLargeException;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.UploadLimitException;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils;
import com.tevinjeffrey.vapor.okcloudapp.utils.ProgressListener;

import java.io.File;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


public class UploadService extends Service {
    @Inject
    DataManager dataManager;
    @Inject
    NotificationManager mNotificationManager;
    @Inject
    ClipboardManager mClipboardManager;
    @Inject
    Bus bus;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        VaprApp.objectGraph(getApplicationContext()).inject(this);

        final File file = FileUtils.getFile(this, intent.getData());
/*
        Timber.i("Data: %s", intent.getData().toString());
        Timber.i("MimeType: %s", FileUtils.getMimeType(file));
        Timber.i("Action: %s", intent.getAction());

        Timber.i("MimeType: %s", FileUtils.getMimeType(file));
*/

        final NotificationCompat.Builder notification = newNotification();
        //startForeground(uploadCount.get(), notification.build());

        final PublishSubject<Long> progressSubject = PublishSubject.create();
        progressSubject
                .sample(250, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
                .doOnNext(new Action1<Long>() {
                    final long filesize = file.length();
                    @Override
                    public void call(Long current) {
                        double ratio = 1 / ((double) filesize / current);
                        double percentage = ratio * 100;
                        int progress = (int) Math.floor(percentage);

                        notification.setProgress(100, progress, false);
                        notification.setContentTitle(file.getName());
                        notification.setContentText("Uploading to CloudApp");
                        notification.setOngoing(true);
                        notification.setContentInfo(VaprUtils.humanReadableByteCount(current, true));
                        Notification notification1 = notification.build();
                        mNotificationManager.notify(startId, notification1);

                    }
                })
                .subscribe();


        dataManager.upload(file, new ProgressListener() {
            @Override
            public void onProgress(long current, long max) {
                //Timber.i("Progress: %s Max: %s", current, max);
                progressSubject.onNext(current);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CloudAppItem>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (e instanceof FileToLargeException || e instanceof UploadLimitException) {
                            String title = null;
                            String subText = null;
                            if (e instanceof FileToLargeException) {
                                title = "File too large for your current plan";
                                subText = "Tap to view plans";
                            }
                            if (e instanceof UploadLimitException) {
                                title = "Monthly upload limit reached";
                                subText = "Tap to view plans";
                            }
                            Intent openInBrowser = new Intent(Intent.ACTION_VIEW);
                            openInBrowser.setData(Uri.parse("https://www.getcloudapp.com/plans"));
                            PendingIntent pOpenInBrowser =
                                    PendingIntent.getActivity(UploadService.this, 0, openInBrowser, 0);

                            NotificationCompat.Builder errorNotification =
                                    new NotificationCompat.Builder(UploadService.this);
                            errorNotification.setContentTitle(title);
                            errorNotification.setContentText(subText);
                            errorNotification.setSmallIcon(R.drawable.ic_cloud_error);
                            errorNotification.setOngoing(false);
                            errorNotification.setWhen(System.currentTimeMillis());
                            errorNotification.setContentIntent(pOpenInBrowser);
                            mNotificationManager.notify(startId, errorNotification.build());
                        } else if (e instanceof UnknownHostException) {
                            mNotificationManager.cancel(startId);
                            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_LONG).show();
                            mNotificationManager.cancel(startId);
                        }
                    }

                    @Override
                    public void onNext(CloudAppItem cloudAppItem) {

                        Intent openInBrowser = new Intent(Intent.ACTION_VIEW);
                        openInBrowser.setData(Uri.parse(cloudAppItem.getUrl()));
                        PendingIntent pOpenInBrowser =
                                PendingIntent.getActivity(UploadService.this, 0, openInBrowser, 0);

                        NotificationCompat.Builder successNotification =
                                new NotificationCompat.Builder(UploadService.this);
                        successNotification.setContentTitle(cloudAppItem.getName());
                        successNotification.setSmallIcon(R.drawable.ic_cloud_done_white_18dp);
                        successNotification.setOngoing(false);
                        successNotification.setContentIntent(pOpenInBrowser);
                        successNotification.setColor(getResources().getColor(R.color.primary));
                        successNotification.setWhen(System.currentTimeMillis());
                        successNotification.setContentText("Tap to open");
                        mNotificationManager.notify(startId, successNotification.build());

                        ClipData clip = ClipData.newPlainText("Uploaded item url", cloudAppItem.getUrl());
                        mClipboardManager.setPrimaryClip(clip);

                        bus.post(new UploadEvent());
                    }
                });

        return START_NOT_STICKY;
    }

    private NotificationCompat.Builder newNotification() {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_cloud_upload_white_18dp)
                .setContentTitle("Uploading to CloudApp")
                .setContentText("Upload in Progress");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
