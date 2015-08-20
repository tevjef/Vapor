package com.tevinjeffrey.vapr.services;

import android.app.IntentService;
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
import com.tevinjeffrey.vapr.R;
import com.tevinjeffrey.vapr.VaprApp;
import com.tevinjeffrey.vapr.VaprUtils;
import com.tevinjeffrey.vapr.events.UploadEvent;
import com.tevinjeffrey.vapr.okcloudapp.DataManager;
import com.tevinjeffrey.vapr.okcloudapp.exceptions.FileToLargeException;
import com.tevinjeffrey.vapr.okcloudapp.exceptions.UploadLimitException;
import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapr.okcloudapp.utils.FileUtils;
import com.tevinjeffrey.vapr.okcloudapp.utils.ProgressListener;

import java.io.File;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import timber.log.Timber;



public class UploadService extends Service {
    @Inject
    DataManager dataManager;
    @Inject
    NotificationManager mNotificationManager;
    @Inject
    ClipboardManager mClipboardManager;
    @Inject
    Bus bus;

    public final int UPLOAD_ID = 1000;

    AtomicInteger uploadCount = new AtomicInteger();

    public UploadService() {
        uploadCount.set(UPLOAD_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uploadCount.incrementAndGet();
        VaprApp.objectGraph(getApplicationContext()).inject(this);

        final File file = FileUtils.getFile(this, intent.getData());
/*
        Timber.i("Data: %s", intent.getData().toString());
        Timber.i("MimeType: %s", FileUtils.getMimeType(file));
        Timber.i("Action: %s", intent.getAction());

        Timber.i("MimeType: %s", FileUtils.getMimeType(file));
*/

        final NotificationCompat.Builder notification = newNotification();

        final PublishSubject<Long> progressSubject = PublishSubject.create();
        progressSubject
                .sample(250, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Long>() {
                    final int uploadId = uploadCount.get();
                    final long filesize = file.length();

                    @Override
                    public void call(Long current) {
                        double ratio = 1 / ((double) filesize / current);
                        double percentage = ratio * 100;
                        int progress = (int) Math.floor(percentage);

                        notification.setProgress(100, progress, false);
                        notification.setContentText(file.getName());
                        notification.setContentText("Uploading to CloudApp");
                        notification.setOngoing(true);
                        notification.setContentInfo(VaprUtils.humanReadableByteCount(current, true));
                        Notification notification1 = notification.build();
                        mNotificationManager.notify(uploadId, notification1);

                    }
                })
                .subscribe();


        dataManager.upload(file, new ProgressListener() {
            @Override
            public void onProgress(long current, long max) {
                progressSubject.onNext(current);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CloudAppItem>() {
                    final int uploadId = uploadCount.get();

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
                            mNotificationManager.notify(uploadId, errorNotification.build());
                        } else if (e instanceof UnknownHostException) {
                            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
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
                        mNotificationManager.notify(uploadId, successNotification.build());

                        ClipData clip = ClipData.newPlainText("Uploaded item url", cloudAppItem.getUrl());
                        mClipboardManager.setPrimaryClip(clip);

                        bus.post(new UploadEvent());
                    }
                });

        startForeground(uploadCount.get(), notification.build());
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
