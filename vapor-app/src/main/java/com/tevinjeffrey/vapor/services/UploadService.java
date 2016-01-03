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
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaporApp;
import com.tevinjeffrey.vapor.utils.VaporUtils;
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.FileToLargeException;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.UploadLimitException;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils;
import com.tevinjeffrey.vapor.okcloudapp.utils.ProgressListener;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


public class UploadService extends Service {

    private static final String ACTION_COPY_LINK = "com.tevinjeffrey.vapor.services.ACTION_COPY_LINK";
    private static final String ACTION_UPLOAD_CANCEL = "com.tevinjeffrey.vapor.services.ACTION_UPLOAD_CANCEL";
    private static final String EXTRA_START_ID = "com.tevinjeffrey.vapor.services.EXTRA_START_ID";
    public static final String ACTION_UPLOAD = "com.tevinjeffrey.vapor.services.ACTION_UPLOAD";

    @Inject
    DataManager dataManager;
    @Inject
    NotificationManager mNotificationManager;
    @Inject
    ClipboardManager mClipboardManager;
    @Inject
    Bus bus;

    HashMap<Integer, Subscription> subscriptionHashMap = new HashMap<>();

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        VaporApp.uiComponent(getApplicationContext()).inject(this);
        if (intent.getAction().equals(ACTION_COPY_LINK)) {
            ClipData clip = ClipData.newPlainText("Uploaded item url", intent.getDataString());
            mClipboardManager.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Copied: " + intent.getDataString(), Toast.LENGTH_SHORT).show();
            stopSelf();
        } else if (intent.getAction().equals(ACTION_UPLOAD_CANCEL)) {
                int subscriptionStartId = intent.getIntExtra(EXTRA_START_ID, -1);
            if (subscriptionStartId == -1) {
                Subscription toCancel = subscriptionHashMap.get(subscriptionStartId);
                if (!toCancel.isUnsubscribed()) {
                    toCancel.unsubscribe();
                    Toast.makeText(getApplicationContext(), "Upload cancelled", Toast.LENGTH_SHORT).show();
                }
            }
            stopSelf();
        } else if (intent.getAction().equals(ACTION_UPLOAD)) {
            final File file = FileUtils.getFile(this, intent.getData());


            final NotificationCompat.Builder uploadNotification = newNotification(startId);

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

                            uploadNotification.setProgress(100, progress, false);
                            uploadNotification.setContentTitle(file.getName());
                            uploadNotification.setContentText("Uploading to CloudApp");
                            uploadNotification.setOngoing(true);
                            uploadNotification.setContentInfo(VaporUtils.humanReadableByteCount(current, true));
                            Notification notification1 = uploadNotification.build();
                            mNotificationManager.notify(startId, notification1);

                        }
                    })
                    .subscribe();


            subscriptionHashMap.put(startId, dataManager.upload(file, new ProgressListener() {
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

                            Intent intentCopyLink = new Intent(UploadService.this, UploadService.class);
                            intent.setAction(ACTION_COPY_LINK);
                            intent.setData(Uri.parse(cloudAppItem.getUrl()));
                            PendingIntent pCopyLink  = PendingIntent.getService(UploadService.this, 0, intentCopyLink, 0);

                            NotificationCompat.Builder successNotification =
                                    new NotificationCompat.Builder(UploadService.this);
                            successNotification.setContentTitle(cloudAppItem.getName());
                            successNotification.setSmallIcon(R.drawable.ic_cloud_done_white_18dp);
                            successNotification.setOngoing(false);
                            successNotification.setContentIntent(pOpenInBrowser);
                            successNotification.setColor(ContextCompat.getColor(UploadService.this, R.color.primary));
                            successNotification.setWhen(System.currentTimeMillis());
                            successNotification.setContentText("Tap to open");
                            successNotification.addAction(0, "Copy link", pCopyLink);
                            mNotificationManager.notify(startId, successNotification.build());

                            ClipData clip = ClipData.newPlainText("Uploaded item url", cloudAppItem.getUrl());
                            mClipboardManager.setPrimaryClip(clip);

                            bus.post(new UploadEvent());
                        }
                    }));
        }

        return START_NOT_STICKY;
    }

    private NotificationCompat.Builder newNotification(int startId) {
        Intent intentCancelUpload = new Intent(UploadService.this, UploadService.class);
        intentCancelUpload.putExtra(EXTRA_START_ID, startId);
        PendingIntent pCancelUpload= PendingIntent.getService(UploadService.this, 0, intentCancelUpload, 0);

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_cloud_upload_white_18dp)
                .setContentTitle("Uploading to CloudApp")
                .setContentText("Upload in Progress")
                .addAction(0, "Cancel upload", pCancelUpload);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
