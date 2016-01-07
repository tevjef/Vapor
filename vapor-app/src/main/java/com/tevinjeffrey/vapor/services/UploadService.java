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
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.CloudAppRequestBody;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.ProgressListener;
import com.tevinjeffrey.vapor.okcloudapp.ProgressNotification;
import com.tevinjeffrey.vapor.okcloudapp.ProgressiveFileRequestBody;
import com.tevinjeffrey.vapor.okcloudapp.ProgressiveStringRequestBody;
import com.tevinjeffrey.vapor.okcloudapp.RefCountManager;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.FileToLargeException;
import com.tevinjeffrey.vapor.okcloudapp.exceptions.UploadLimitException;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils;
import com.tevinjeffrey.vapor.utils.RxUtils;

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

import static com.tevinjeffrey.vapor.services.IntentBridge.FILE_BOOKMARK;
import static com.tevinjeffrey.vapor.services.IntentBridge.FILE_TEXT;
import static com.tevinjeffrey.vapor.services.IntentBridge.FILE_TYPE;


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
        Timber.i("New Notification Id generated id=%s", notificationId);

        String action = intent.getStringExtra(ACTION_TYPE);

        switch (action) {
            case ACTION_COPY_LINK:
                // Takes the data saved the intent and adds it to the clipboard.
                Timber.i("ACTION_COPY_LINK with id=%s", notificationId);
                ClipData clip = ClipData.newPlainText("Uploaded item url", intent.getDataString());
                mClipboardManager.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Copied: " + intent.getDataString(),
                        Toast.LENGTH_SHORT).show();

                return START_NOT_STICKY;
            case ACTION_UPLOAD_CANCEL:
                // ACTION_UPLOAD_CANCEL is user action from a PendingIntent to cancel a notification
                // the the notification it was showing. Every uploads subscription is saved to a hashmap
                // along with the notificationId the upload is tied to. This can action looks up a table
                // for the Subscription (the upload) using the notificationId, then unsubscribes.
                // The networking library, Retrofit and OkHttp will close the connection after all
                // subscribers are unsubscribed. We early cancel the notification, but residual bytes
                // being writing may cause the upload notification to get recreated. Unsubscribing the
                // upload subscription causes then unsubscribes from the Subject tied to progress of
                // the notification, guaranteeing the notification was cancelled.
                int notificationIdExtra = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                Timber.i("ACTION_UPLOAD_CANCEL with id=%s", notificationIdExtra);
                if (notificationIdExtra != -1) {
                    Subscription toCancel = subscriptionHashMap.get(notificationIdExtra);
                    RxUtils.unsubscribeIfNotNull(toCancel);
                    Timber.i("Upload cancelled with id=%s", notificationIdExtra);
                    Toast.makeText(getApplicationContext(), "Upload cancelled",
                            Toast.LENGTH_SHORT).show();
                    mNotificationManager.cancel(notificationIdExtra);
                }
                return START_NOT_STICKY;

            case ACTION_UPLOAD:
                final String fileType = intent.getStringExtra(FILE_TYPE);
                final PublishSubject<Long> progressSubject = PublishSubject.create();
                final ProgressListener listener = new NotificationProgress(progressSubject);
                String fileSize = "";
                String fileName = "";
                Uri fileUri = null;
                Observable<CloudAppItem> uploadObservable;
                CloudAppRequestBody requestBody;

                switch (fileType) {
                    case FILE_TEXT: {
                        // The text is passed into a custom RequestBody. Currently the file name of
                        // UTF8 string is the first 18 characters of the the string itself.
                        // The option remains for the user to provide a name. from pass it and a
                        // listener into the RequestBody. The listener allows us to get notifications
                        // on how many bytes have been written. This information is useful as it
                        // allows us to display the upload progress to the user.
                        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                        requestBody = new ProgressiveStringRequestBody(text, null, listener);
                        fileSize = requestBody.getFileSize();
                        fileName = requestBody.getFileName();
                        uploadObservable = dataManager.upload(requestBody);
                        break;
                    }
                    case FILE_BOOKMARK: {
                        // The url is retrieved from EXTRA_TEXT, then the api call is made to create
                        // the necessary file for upload. Currently the bookmark name is the url itself.
                        // The option remains for the user to provide a name.
                        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                        uploadObservable = dataManager.bookmarkItem(text, text);
                        break;
                    }
                    default:
                        // Try URI from data, then from EXTRA_STREAM. If no valid URI exists,
                        // throw error then exit. Create a File from pass it and a listener into the
                        // RequestBody. The listener allows us to get notifications on how many bytes
                        // have been written. This information is useful as it allows us to display
                        // the upload progress to the user.
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

                final String finalFileName = fileName;
                final NotificationCompat.Builder uploadNotification = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_cloud_upload_white_18dp)
                        .setContentTitle(fileName)
                        .setContentText("Upload in Progress")
                        .setOngoing(true)
                        .addAction(0, "Cancel upload", PendingIntent.getService(UploadService.this,
                                notificationId,
                                new Intent(UploadService.this, UploadService.class)
                                        .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                                        .putExtra(ACTION_TYPE, ACTION_UPLOAD_CANCEL),
                                PendingIntent.FLAG_UPDATE_CURRENT));


                // Subject to reduce notification updates as the byes are written to the server.
                // The notification updates 4 times per second.
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
                        .doOnNext(new ProgressNotification(
                                fileSize.length() == 0 ? 0 : Long.valueOf(fileSize),
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
                                RxUtils.unsubscribeIfNotNull(progressSubscription);
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
                        .subscribe(new UploadObserver(notificationId, fileName, fileUri)));
                break;
        }

        return START_NOT_STICKY;
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

    private class UploadObserver implements Observer<CloudAppItem> {
        int notificationId;
        int newNotificationId;
        String fileName;
        Uri fileUri;

        public UploadObserver(int notificationId, String fileName, Uri fileUri) {
            this.newNotificationId = notificationId / 2;
            this.fileName = fileName;
            this.fileUri = fileUri;
        }

        @Override
        public void onCompleted() {
            Timber.i("Upload complete %s", fileName);
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Error during upload. File: %s", fileName);
            if (e instanceof FileToLargeException || e instanceof UploadLimitException) {
                String title = fileName;
                String subText = e.getMessage();
                if (e instanceof FileToLargeException) {
                    title = "File too large for your current plan";
                    subText = "Tap to view plans";
                }
                if (e instanceof UploadLimitException) {
                    title = "Monthly upload limit reached";
                    subText = "Tap to view plans";
                }

                NotificationCompat.Builder errorNotification =
                        new NotificationCompat.Builder(UploadService.this)
                                .setContentTitle(title)
                                .setContentText(subText)
                                .setSmallIcon(R.drawable.ic_cloud_error)
                                .setOngoing(false)
                                .setWhen(System.currentTimeMillis())
                                .setContentIntent(PendingIntent.getActivity(UploadService.this, newNotificationId,
                                        new Intent(Intent.ACTION_VIEW)
                                                .setData(Uri.parse("https://www.getcloudapp.com/plans")),
                                        PendingIntent.FLAG_UPDATE_CURRENT));

                // Fire notification with a new id separate from the id used to identify the upload
                // progress notification.
                mNotificationManager.notify(newNotificationId, errorNotification.build());
                Toast.makeText(getApplicationContext(), title, Toast.LENGTH_LONG).show();

            } else if (e instanceof UnknownHostException) {
                mNotificationManager.cancel(notificationId);
                Timber.i("Network error, closing notification with id=%s", notificationId);
                Toast.makeText(getApplicationContext(), "No internet connection. Upload failed.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "An error occurred. Upload failed.",
                        Toast.LENGTH_LONG).show();
                mNotificationManager.cancel(notificationId);
                Timber.i("Generic error, closing notification with id=%s", notificationId);

            }
        }


        @Override
        public void onNext(CloudAppItem cloudAppItem) {
            //
            NotificationCompat.Builder successNotification =
                    new NotificationCompat.Builder(UploadService.this)
                            .setContentTitle(cloudAppItem.getName())
                            .setSmallIcon(R.drawable.ic_cloud_done_white_18dp)
                            .setOngoing(false)
                            // Open browser when user clicks notification
                            .setContentIntent(PendingIntent.getActivity(UploadService.this, 0,
                                    new Intent(Intent.ACTION_VIEW)
                                            .setData(Uri.parse(cloudAppItem.getUrl())),
                                    PendingIntent.FLAG_UPDATE_CURRENT))
                            .setColor(ContextCompat.getColor(UploadService.this, R.color.primary))
                            .setWhen(System.currentTimeMillis())
                            .setPriority(Notification.PRIORITY_MAX)
                            .setContentText("Tap to open")
                            // Action to copy link when user clicks the copy link action.
                            .addAction(0, "Copy link",
                                    PendingIntent.getService(UploadService.this, 0,
                                        new Intent(UploadService.this, UploadService.class)
                                            .putExtra(ACTION_TYPE, ACTION_COPY_LINK)
                                            .setData(Uri.parse(cloudAppItem.getUrl())),
                                        PendingIntent.FLAG_UPDATE_CURRENT));

            // Fire notification with a new id separate from the id used to identify the upload
            // progress notification.
            mNotificationManager.notify(newNotificationId, successNotification.build());

            //Add link to clip board.
            ClipData clip = ClipData.newPlainText("Uploaded item url", cloudAppItem.getUrl());
            mClipboardManager.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Copied: " + cloudAppItem.getUrl(),
                    Toast.LENGTH_LONG).show();

            // Tell the rest of the app that a file was successfully uploaded.
            bus.post(new UploadEvent(fileUri, cloudAppItem));
        }
    }

    private class NotificationProgress implements ProgressListener {
        private final PublishSubject<Long> publishSubject;

        public NotificationProgress(PublishSubject<Long> publishSubject) {
            this.publishSubject = publishSubject;
        }

        @Override
        public void onProgress(long current, long max) {
            publishSubject.onNext(current);
        }
    }

}
