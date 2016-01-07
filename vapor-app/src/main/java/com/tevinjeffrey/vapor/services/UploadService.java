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
import com.tevinjeffrey.vapor.okcloudapp.CloudAppRequestBody;
import com.tevinjeffrey.vapor.okcloudapp.ProgressNotification;
import com.tevinjeffrey.vapor.okcloudapp.ProgressiveFileRequestBody;
import com.tevinjeffrey.vapor.okcloudapp.ProgressiveStringRequestBody;
import com.tevinjeffrey.vapor.okcloudapp.RefCountManager;
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.FileToLargeException;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.UploadLimitException;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils;
import com.tevinjeffrey.vapor.okcloudapp.ProgressListener;
import com.tevinjeffrey.vapor.ui.login.LoginException;

import java.io.File;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

import static com.tevinjeffrey.vapor.services.IntentBridge.*;


public class UploadService extends Service {

    public static final String ACTION_TYPE = "com.tevinjeffrey.vapor.services.ACTION_TYPE";
    private static final String ACTION_COPY_LINK = "com.tevinjeffrey.vapor.services.ACTION_COPY_LINK";
    private static final String ACTION_UPLOAD_CANCEL = "com.tevinjeffrey.vapor.services.ACTION_UPLOAD_CANCEL";
    private static final String EXTRA_NOTIFICATION_ID = "com.tevinjeffrey.vapor.services.EXTRA_START_ID";
    public static final String ACTION_UPLOAD = "com.tevinjeffrey.vapor.services.ACTION_UPLOAD";

    @Inject
    DataManager dataManager;
    @Inject
    NotificationManager mNotificationManager;
    @Inject
    ClipboardManager mClipboardManager;
    @Inject
    RefCountManager refCountManager;
    @Inject
    Bus bus;

    HashMap<Integer, Subscription> subscriptionHashMap = new HashMap<>();

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        VaporApp.uiComponent(getApplicationContext()).inject(this);
        Timber.d("Upload intent with startId=%s intent=%s ", startId, intent.toString());
        final int notificationId = Long.valueOf(System.currentTimeMillis()).hashCode();
        Timber.i("Notification Id generated id=%s", notificationId);

        String action = intent.getStringExtra(ACTION_TYPE);
        if (action == null) {
            Timber.d("Action is null for intent=%s", intent.toString());
           
            return START_NOT_STICKY;
        }
        if (action.equals(ACTION_COPY_LINK)) {
            Timber.i("ACTION_COPY_LINK with id=%s", notificationId);
            ClipData clip = ClipData.newPlainText("Uploaded item url", intent.getDataString());
            mClipboardManager.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Copied: " + intent.getDataString(),
                    Toast.LENGTH_SHORT).show();
           
            return START_NOT_STICKY;
        } else if (action.equals(ACTION_UPLOAD_CANCEL)) {
            int notificationIdExtra = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
            Timber.i("ACTION_UPLOAD_CANCEL with id=%s", notificationIdExtra);
            if (notificationIdExtra != -1) {
                Subscription toCancel = subscriptionHashMap.get(notificationIdExtra);
                if (toCancel != null && !toCancel.isUnsubscribed()) {
                    Timber.i("Upload cancelled with id=%s", notificationIdExtra);
                    toCancel.unsubscribe();
                    Toast.makeText(getApplicationContext(), "Upload cancelled",
                            Toast.LENGTH_SHORT).show();
                }
                mNotificationManager.cancel(notificationIdExtra);
            }

            return START_NOT_STICKY;
        } else if (action.equals(ACTION_UPLOAD)) {
            final String fileType = intent.getStringExtra(FILE_TYPE);
            final PublishSubject<Long> progressSubject = PublishSubject.create();

            final ProgressListener listener = new ProgressListener() {
                @Override
                public void onProgress(long current, long max) {
                    //Timber.i("Progress: %s Max: %s", current, max);
                    progressSubject.onNext(current);
                }
            };

            String fileSize = "";
            String fileName = "";
            Uri fileUri = null;
            Observable<CloudAppItem> uploadObservable;
            CloudAppRequestBody requestBody;

            switch (fileType) {
                case FILE_TEXT: {
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    requestBody = new ProgressiveStringRequestBody(text, null, listener);
                    fileSize = requestBody.getFileSize();
                    fileName = requestBody.getFileName();
                    uploadObservable = dataManager.upload(requestBody);
                    break;
                }
                case FILE_BOOKMARK: {
                    String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                    uploadObservable = dataManager.bookmarkItem(text, text);
                    break;
                }
                default:
                    fileUri = intent.getData();
                    if (fileUri == null) {
                        fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    }
                    final File file = FileUtils.getFile(this, fileUri);
                    if (file == null) {
                        Toast.makeText(this, "Cannot upload file", Toast.LENGTH_SHORT).show();
                       
                        return START_NOT_STICKY;
                    }
                    requestBody = new ProgressiveFileRequestBody(file, listener);
                    fileSize = requestBody.getFileSize();
                    fileName = requestBody.getFileName();
                    uploadObservable = dataManager.upload(requestBody);
                    break;
            }

            Timber.i("ACTION_UPLOAD with id=%s and fileName=%s, fileSize=%s, filetype=%s",
                    notificationId, fileName, fileSize, fileType);

            final Uri finalFileUri = fileUri;
            final String finalFileName = fileName;

            final NotificationCompat.Builder uploadNotification = newNotification(notificationId);

            final Subscription progressSubscription = progressSubject
                    .sample(250, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Timber.e(throwable, "Error creating progress listener.");
                        }
                    })
                    .doOnUnsubscribe(new Action0() {
                        @Override
                        public void call() {
                            Timber.i("Progress unsubscribed %s", finalFileName);
                            mNotificationManager.cancel(notificationId);
                        }
                    })
                    .doOnNext(new ProgressNotification(fileName,
                            fileSize.length() == 0?0:Long.valueOf(fileSize),
                            uploadNotification,
                            mNotificationManager,
                            notificationId))
                    .onBackpressureDrop()
                    .subscribe();

            refCountManager.addNotificationId(notificationId);
            subscriptionHashMap.put(notificationId, uploadObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(new Action0() {
                        @Override
                        public void call() {
                            Timber.i("Upload unsubscribed %s", finalFileName);
                            progressSubscription.unsubscribe();
                        }
                    })
                    .doOnTerminate(new Action0() {
                        @Override
                        public void call() {
                            Timber.i("Upload terminated %s", finalFileName);
                            refCountManager.removeNotificationId(notificationId);
                        }
                    })
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            Toast.makeText(getApplicationContext(), "Adding " + finalFileName + " to Vapor",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .subscribe(new Observer<CloudAppItem>() {

                        int newNotificationId = notificationId / 2;
                        @Override
                        public void onCompleted() {
                            Timber.i("Upload complete %s", finalFileName);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.e(e, "Error during upload. File: %s", finalFileName);
                            if (e instanceof FileToLargeException || e instanceof UploadLimitException) {
                                String title = finalFileName;
                                String subText = e.getMessage();
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
                                mNotificationManager.notify(newNotificationId, errorNotification.build());
                                Toast.makeText(getApplicationContext(), title, Toast.LENGTH_LONG).show();

                            } else if (e instanceof UnknownHostException) {
                                mNotificationManager.cancel(notificationId);
                                Timber.i("Network error, closing notification with id=%s", notificationId);
                                Toast.makeText(getApplicationContext(), "No internet connection. Upload failed.",
                                        Toast.LENGTH_LONG).show();
                            } else if (e instanceof LoginException) {
                                mNotificationManager.cancel(notificationId);
                            } else {
                                Toast.makeText(getApplicationContext(), "An error occurred. Upload failed.",
                                        Toast.LENGTH_LONG).show();
                                mNotificationManager.cancel(notificationId);
                                Timber.i("Generic error, closing notification with id=%s", notificationId);

                            }
                        }

                        @Override
                        public void onNext(CloudAppItem cloudAppItem) {

                            Intent openInBrowser = new Intent(Intent.ACTION_VIEW);
                            openInBrowser.setData(Uri.parse(cloudAppItem.getUrl()));
                            PendingIntent pOpenInBrowser =
                                    PendingIntent.getActivity(UploadService.this, 0, openInBrowser,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
                            Intent intentCopyLink = new Intent(UploadService.this, UploadService.class);
                            intentCopyLink.putExtra(ACTION_TYPE, ACTION_COPY_LINK);
                            intentCopyLink.setData(Uri.parse(cloudAppItem.getUrl()));
                            PendingIntent pCopyLink  = PendingIntent.getService(UploadService.this, 0,
                                    intentCopyLink, PendingIntent.FLAG_UPDATE_CURRENT);
                            NotificationCompat.Builder successNotification =
                                    new NotificationCompat.Builder(UploadService.this)
                                            .setContentTitle(cloudAppItem.getName())
                                            .setSmallIcon(R.drawable.ic_cloud_done_white_18dp)
                                            .setOngoing(false)
                                            .setContentIntent(pOpenInBrowser)
                                            .setColor(ContextCompat.getColor(UploadService.this, R.color.primary))
                                            .setWhen(System.currentTimeMillis())
                                            .setPriority(Notification.PRIORITY_MAX)
                                            .setContentText("Tap to open")
                                            .addAction(0, "Copy link", pCopyLink);
                            mNotificationManager.notify(newNotificationId, successNotification.build());

                            ClipData clip = ClipData.newPlainText("Uploaded item url", cloudAppItem.getUrl());
                            mClipboardManager.setPrimaryClip(clip);
                            Toast.makeText(getApplicationContext(), "Copied: " + cloudAppItem.getUrl(),
                                    Toast.LENGTH_LONG).show();
                            bus.post(new UploadEvent(finalFileUri, cloudAppItem));
                        }
                    }));
        }

        return START_NOT_STICKY;
    }

    private NotificationCompat.Builder newNotification(int notificationId) {
        Intent intentCancelUpload = new Intent(UploadService.this, UploadService.class);
        intentCancelUpload.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        intentCancelUpload.putExtra(ACTION_TYPE, ACTION_UPLOAD_CANCEL);
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_cloud_upload_white_18dp)
                .setContentTitle("Uploading to CloudApp")
                .setContentText("Upload in Progress")
                .addAction(0, "Cancel upload", PendingIntent.getService(UploadService.this, notificationId,
                        intentCancelUpload, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        for (Integer notificationId : refCountManager.getNotificationIds()) {
            Subscription subscription = subscriptionHashMap.get(notificationId);
            if (subscription != null) {
                subscription.unsubscribe();
            }
            mNotificationManager.cancel(notificationId);
            Timber.i("CLEAN UP from onTaskRemoved with id=%s", notificationId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Integer notificationId : refCountManager.getNotificationIds()) {
            Subscription subscription = subscriptionHashMap.get(notificationId);
            if (subscription != null) {
                subscription.unsubscribe();
            }
            mNotificationManager.cancel(notificationId);
            Timber.i("CLEAN UP from onDestroy with id=%s", notificationId);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
