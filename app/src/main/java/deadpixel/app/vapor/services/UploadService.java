package deadpixel.app.vapor.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.koushikdutta.ion.ProgressCallback;
import com.squareup.otto.Subscribe;

import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import deadpixel.app.vapor.MenuHandler;
import deadpixel.app.vapor.R;
import deadpixel.app.vapor.callbacks.DatabaseUpdateEvent;
import deadpixel.app.vapor.callbacks.UploadCallback;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ItemResponseEvent;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.cloudapp.impl.model.CloudAppUpload;
import deadpixel.app.vapor.utils.AppUtils;


public class UploadService extends Service {


    NotificationManager mManager;
    Intent currentIntent;
    int uploadNum = 0;
    private static final String TAG = "UploadService ";

    List<CloudAppUpload> uploads = new ArrayList<CloudAppUpload>();

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uploadNum++;

        Log.e(TAG, "UploadService started");

        currentIntent = intent;

        AppUtils.getEventBus().register(ItemResponseHandler);

        //We've determine that this is and intent to upload a bookmark.
        if(intent.getStringExtra(AppUtils.EXTRA_BOOKMARK_URL) != null) {

            String name = intent.getStringExtra(AppUtils.EXTRA_BOOKMARK_NAME);
            String url = intent.getStringExtra(AppUtils.EXTRA_BOOKMARK_URL);

            Log.e(TAG, "Bookmark intent captured" + "Data: " + url);

            try {
                CloudAppUpload fileUpload = new CloudAppUpload(null, null, null, uploadNum);
                uploads.add(fileUpload);
                AppUtils.addToRequestQueue(AppUtils.api
                        .createBookmark((name == null || name.equals("")) ? url : name, url));
            } catch (CloudAppException e) {
                e.printStackTrace();
            }
        } else if (intent.getStringExtra(AppUtils.EXTRA_FILE_NAME) != null) {
            String name = intent.getStringExtra(AppUtils.EXTRA_FILE_NAME);
            Uri uri = intent.getParcelableExtra(AppUtils.EXTRA_FILE_URI);
            String size = intent.getStringExtra(AppUtils.EXTRA_FILE_SIZE);

            Log.e(TAG, "File intent reached UploadService: " + "Data: " + name + " Location: " + uri);

            final NotificationCompat.Builder notification = newNotification();

            ProgressCallback progressCallback = new ProgressCallback() {
                @Override
                public void onProgress(long downloaded, long total) {

                    double ratio = 1 / ((double)total / downloaded);
                    double percentage = ratio * 100;
                    int progress = (int) Math.floor(percentage);
                    Log.d(TAG, "Progress: " + progress);
                    if(downloaded != 0) {
                        notification.setProgress(100, progress, false);
                        notification.setContentText("Upload in Progress");
                        notification.setOngoing(true);

                        mManager.notify(uploadNum, notification.build());

                    }
                }
            };

            UploadCallback uploadCallback = new UploadCallback() {
                @Override
                public void onComplete() {

                }

                @Override
                public void onError(ErrorEvent event) {
                    Log.e(TAG, "Error: " + event.getError().getMessage());

                    if(event.getImplicitError() != null) {
                        Object extras = event.getImplicitError();
                        if(extras instanceof CloudAppUpload) {
                            CloudAppUpload cloudAppUpload = (CloudAppUpload) extras;
                            makeErrorNotification(cloudAppUpload, event);
                        }
                    }
                    stopSelf();
                }
            };


            mManager.notify(uploadNum, notification.build());

            try {
                File file = FileUtils.getFile(this, uri);
                CloudAppUpload fileUpload = new CloudAppUpload(file, progressCallback, uploadCallback, uploadNum);
                uploads.add(fileUpload);
                AppUtils.api.upload(fileUpload);
            } catch (CloudAppException e) {
                e.printStackTrace();
            }

        }

        return START_NOT_STICKY;
    }

    private NotificationCompat.Builder newNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo_notification)
                        .setContentTitle("Uploading to CloudApp")
                        .setContentText("Upload in Progress");

        return mBuilder;
    }

    private void makeSuccessNotification(DatabaseUpdateEvent event) {

        ArrayList<? extends CloudAppItem> items = event.getItems();

        for(CloudAppItem item : items) {
            String itemName = item.getName();
            for(CloudAppUpload upload :uploads) {
                if(upload.getmFile().getName().equals(itemName)){

                    NotificationCompat.Builder uploadedNotification =
                            new NotificationCompat.Builder(UploadService.this);

                    String link  = MenuHandler.getLink(item);

                    uploadedNotification.setContentTitle(itemName);
                    uploadedNotification.setSmallIcon(R.drawable.ic_logo_notification);
                    uploadedNotification.setOngoing(false);
                    uploadedNotification.setWhen(System.currentTimeMillis());
                    uploadedNotification.setContentText(link);

                    //Intent to start web browser
                    Intent openInBrowser = new Intent(Intent.ACTION_VIEW);
                    openInBrowser.setData(Uri.parse(link));
                    PendingIntent pOpenInBrowser = PendingIntent.getActivity(UploadService.this,0 , openInBrowser, 0);
                    uploadedNotification.addAction(0, "Open in browser", pOpenInBrowser);


                    //Intent to copy link
                    Intent copyLink = new Intent();
                    copyLink.putExtra(ClipboardReceiver.CLIPBOARD_TEXT, link);
                    PendingIntent pCopyLink = PendingIntent.getBroadcast(this, 0, copyLink, 0);
                    uploadedNotification.addAction(0, "Copy link", pCopyLink);

                    uploadedNotification.setContentIntent(pOpenInBrowser);



                    mManager.notify(upload.getNotificationId(), uploadedNotification.build());

                }
            }

        }
    }
    private void makeErrorNotification(CloudAppUpload upload, ErrorEvent errorEvent) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);

        notification.setContentTitle("Can't upload file");
        notification.setSmallIcon(android.R.drawable.ic_dialog_alert);
        notification.setWhen(System.currentTimeMillis());


        String explicitError = errorEvent.getExplicitError();

        if(explicitError.equals(AppUtils.NO_CONNECTION)) {
            notification.setContentText("No internet connection");

            Toast.makeText(getApplicationContext(), "Check your internet connection.", Toast.LENGTH_LONG).show();

        } else if (explicitError.equals(AppUtils.FILE_TOO_LARGE)) {
            notification.setContentText("File size too large for free account");

            double maxFileSize = AppUtils.mPref.getLong(AppUtils.PREF_MAX_UPLOAD_SIZE, 26214400) / (1024 * 1024.0);
            String roundedValue = String.valueOf(5 * (Math.round(maxFileSize / 5.0)));
            Toast.makeText(getApplicationContext(), "The file is too large. The limit is " + roundedValue + "MB", Toast.LENGTH_LONG).show();

        } else if (explicitError.equals(AppUtils.UPLOAD_TICKETS_ZERO)) {
            notification.setContentText("Upload limit reached for the day");

            Toast.makeText(getApplicationContext(), "Upload limit reached for the day", Toast.LENGTH_LONG).show();

        } else {
            notification.setContentText("An error occurred");
        }

        mManager.notify(upload.getNotificationId(), notification.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private Object ItemResponseHandler = new Object() {
        @Subscribe
        public void onItemResponse(ItemResponseEvent event) {

        }
        @Subscribe
        public void onDatabaseUpdate(DatabaseUpdateEvent event) {
            makeSuccessNotification(event);
         }
        @Subscribe
        public void onErrorEvent(ErrorEvent event) {


        }
        @Subscribe
        public void onResponseEvent(ResponseEvent event) {

        }
    };
}
