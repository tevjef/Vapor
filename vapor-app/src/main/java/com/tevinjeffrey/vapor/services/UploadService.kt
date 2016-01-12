package com.tevinjeffrey.vapor.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast

import com.squareup.otto.Bus
import com.tevinjeffrey.vapor.R
import com.tevinjeffrey.vapor.VaporApp
import com.tevinjeffrey.vapor.events.UploadEvent
import com.tevinjeffrey.vapor.events.UploadFailedEvent
import com.tevinjeffrey.vapor.okcloudapp.CloudAppRequestBody
import com.tevinjeffrey.vapor.okcloudapp.DataManager
import com.tevinjeffrey.vapor.okcloudapp.ProgressListener
import com.tevinjeffrey.vapor.okcloudapp.ProgressNotification
import com.tevinjeffrey.vapor.okcloudapp.ProgressiveFileRequestBody
import com.tevinjeffrey.vapor.okcloudapp.ProgressiveStringRequestBody
import com.tevinjeffrey.vapor.okcloudapp.RefCountManager
import com.tevinjeffrey.vapor.okcloudapp.exceptions.FileToLargeException
import com.tevinjeffrey.vapor.okcloudapp.exceptions.UploadLimitException
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils
import com.tevinjeffrey.vapor.utils.RxUtils

import java.io.File
import java.net.UnknownHostException
import java.util.HashMap
import java.util.concurrent.TimeUnit

import javax.inject.Inject

import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import timber.log.Timber

import com.tevinjeffrey.vapor.services.IntentBridge.FILE_BOOKMARK
import com.tevinjeffrey.vapor.services.IntentBridge.FILE_TEXT
import com.tevinjeffrey.vapor.services.IntentBridge.FILE_TYPE


class UploadService : Service() {

    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var mNotificationManager: NotificationManager
    @Inject
    lateinit var mClipboardManager: ClipboardManager
    @Inject
    lateinit var refCountManager: RefCountManager
    @Inject
    lateinit var bus: Bus

    internal val subscriptionHashMap = HashMap<Int, Subscription>()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        VaporApp.uiComponent(applicationContext).inject(this)
        Timber.d("Upload intent with startId=%s intent=%s ", startId, intent.toString())
        val notificationId = java.lang.Long.valueOf(System.currentTimeMillis())!!.hashCode()
        Timber.i("New Notification Id generated id=%s", notificationId)

        val action = intent.getStringExtra(ACTION_TYPE)

        when (action) {
            ACTION_COPY_LINK -> {
                // Takes the data saved the intent and adds it to the clipboard.
                Timber.i("ACTION_COPY_LINK with id=%s", notificationId)
                val clip = ClipData.newPlainText("Uploaded item url", intent.dataString)
                mClipboardManager.primaryClip = clip
                Toast.makeText(applicationContext, "Copied: " + intent.dataString,
                        Toast.LENGTH_SHORT).show()

                return Service.START_NOT_STICKY
            }
            ACTION_UPLOAD_CANCEL -> {
                // ACTION_UPLOAD_CANCEL is user action from a PendingIntent to cancel a notification
                // the the notification it was showing. Every uploads subscription is saved to a hashmap
                // along with the notificationId the upload is tied to. This can action looks up a table
                // for the Subscription (the upload) using the notificationId, then unsubscribes.
                // The networking library, Retrofit and OkHttp will close the connection after all
                // subscribers are unsubscribed. We early cancel the notification, but residual bytes
                // being writing may cause the upload notification to get recreated. Unsubscribing the
                // upload subscription causes then unsubscribes from the Subject tied to progress of
                // the notification, guaranteeing the notification was cancelled.
                val notificationIdExtra = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                Timber.i("ACTION_UPLOAD_CANCEL with id=%s", notificationIdExtra)
                if (notificationIdExtra != -1) {
                    val toCancel = subscriptionHashMap[notificationIdExtra]
                    RxUtils.unsubscribeIfNotNull(toCancel)
                    Timber.i("Upload cancelled with id=%s", notificationIdExtra)
                    Toast.makeText(applicationContext, "Upload cancelled",
                            Toast.LENGTH_SHORT).show()
                    mNotificationManager.cancel(notificationIdExtra)
                }
                return Service.START_NOT_STICKY
            }

            ACTION_UPLOAD -> {
                val fileType = intent.getStringExtra(FILE_TYPE)
                val progressSubject = PublishSubject.create<Long>()
                val listener = NotificationProgress(progressSubject)
                var fileSize = ""
                var fileName = ""
                var fileUri: Uri? = null
                val uploadObservable: Observable<CloudAppItem>
                val requestBody: CloudAppRequestBody

                when (fileType) {
                    FILE_TEXT -> {
                        // The text is passed into a custom RequestBody. Currently the file name of
                        // UTF8 string is the first 18 characters of the the string itself.
                        // The option remains for the user to provide a name. from pass it and a
                        // listener into the RequestBody. The listener allows us to get notifications
                        // on how many bytes have been written. This information is useful as it
                        // allows us to display the upload progress to the user.
                        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                        requestBody = ProgressiveStringRequestBody(text, null, listener)
                        fileSize = requestBody.fileSize
                        fileName = requestBody.fileName
                        uploadObservable = dataManager.upload(requestBody)
                    }
                    FILE_BOOKMARK -> {
                        // The url is retrieved from EXTRA_TEXT, then the api call is made to create
                        // the necessary file for upload. Currently the bookmark name is the url itself.
                        // The option remains for the user to provide a name.
                        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                        uploadObservable = dataManager.bookmarkItem(text, text)
                    }
                    else -> {
                        // Try URI from data, then from EXTRA_STREAM. If no valid URI exists,
                        // throw error then exit. Create a File from pass it and a listener into the
                        // RequestBody. The listener allows us to get notifications on how many bytes
                        // have been written. This information is useful as it allows us to display
                        // the upload progress to the user.
                        fileUri = intent.data
                        if (fileUri == null) {
                            fileUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                        }
                        val file = FileUtils.getFile(this, fileUri)
                        if (file == null) {
                            Toast.makeText(this, "Cannot upload file", Toast.LENGTH_SHORT).show()
                            return Service.START_NOT_STICKY
                        }
                        requestBody = ProgressiveFileRequestBody(file, listener)
                        fileSize = requestBody.fileSize
                        fileName = requestBody.fileName
                        uploadObservable = dataManager.upload(requestBody)
                    }
                }

                Timber.i("ACTION_UPLOAD with id=%s and fileName=%s, fileSize=%s, filetype=%s",
                        notificationId, fileName, fileSize, fileType)

                val finalFileName = fileName
                val uploadNotification = NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_cloud_upload)
                        .setContentTitle(fileName)
                        .setContentText("Upload in Progress")
                        .setOngoing(true)
                        .addAction(0, "Cancel upload", PendingIntent.getService(this@UploadService,
                        notificationId, Intent(this@UploadService, UploadService::class.java)
                                .putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                                .putExtra(ACTION_TYPE, ACTION_UPLOAD_CANCEL),
                                PendingIntent.FLAG_UPDATE_CURRENT))


                // Subject to reduce notification updates as the byes are written to the server.
                // The notification updates 4 times per second.
                val progressSubscription = progressSubject.sample(250, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { throwable -> Timber.e(throwable, "Error creating progress listener.") }
                        .doOnUnsubscribe {
                            Timber.i("Progress unsubscribed %s", finalFileName)
                            mNotificationManager.cancel(notificationId)
                        }
                        .doOnNext(ProgressNotification(
                            if (fileSize.length == 0) 0 else java.lang.Long.valueOf(fileSize),
                            uploadNotification,
                            mNotificationManager,
                            notificationId))
                        .onBackpressureDrop()
                        .subscribe()

                refCountManager.addNotificationId(notificationId)
                subscriptionHashMap.put(notificationId, uploadObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnUnsubscribe {
                            Timber.i("Upload unsubscribed %s", finalFileName)
                            RxUtils.unsubscribeIfNotNull(progressSubscription)
                        }.doOnTerminate {
                            Timber.i("Upload terminated %s", finalFileName)
                            refCountManager.removeNotificationId(notificationId)
                        }.doOnSubscribe {
                            Toast.makeText(applicationContext, "Adding $finalFileName to Vapor",
                                    Toast.LENGTH_SHORT).show()
                        }.subscribe(UploadObserver(notificationId, fileName, fileUri)))
            }
        }

        return Service.START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        for (notificationId in refCountManager.getNotificationIds()) {
            val subscription = subscriptionHashMap[notificationId]
            subscription?.unsubscribe()
            mNotificationManager.cancel(notificationId)
            Timber.i("CLEAN UP from onTaskRemoved with id=%s", notificationId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (notificationId in refCountManager.getNotificationIds()) {
            val subscription = subscriptionHashMap[notificationId]
            subscription?.unsubscribe()
            mNotificationManager.cancel(notificationId)
            Timber.i("CLEAN UP from onDestroy with id=%s", notificationId)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private inner class UploadObserver(notificationId: Int, internal val fileName: String, internal val fileUri: Uri?) : Observer<CloudAppItem> {
        internal var notificationId: Int = 0
        internal val newNotificationId: Int

        init {
            this.newNotificationId = notificationId / 2
        }

        override fun onCompleted() {
            Timber.i("Upload complete %s", fileName)
        }

        override fun onError(e: Throwable) {
            Timber.e(e, "Error during upload. File: %s", fileName)
            if (e is FileToLargeException || e is UploadLimitException) {
                var title = fileName
                var subText = e.message
                if (e is FileToLargeException) {
                    title = "File too large for your current plan"
                    subText = "Tap to view plans"
                }
                if (e is UploadLimitException) {
                    title = "Monthly upload limit reached"
                    subText = "Tap to view plans"
                }

                val errorNotification = NotificationCompat.Builder(this@UploadService).setContentTitle(title).setContentText(subText).setSmallIcon(R.drawable.ic_cloud_fail).setOngoing(false).setWhen(System.currentTimeMillis()).setContentIntent(PendingIntent.getActivity(this@UploadService, newNotificationId,
                        Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.getcloudapp.com/plans")),
                        PendingIntent.FLAG_UPDATE_CURRENT))

                // Fire notification with a new id separate from the id used to identify the upload
                // progress notification.
                mNotificationManager.notify(newNotificationId, errorNotification.build())
                Toast.makeText(applicationContext, title, Toast.LENGTH_LONG).show()

            } else if (e is UnknownHostException) {
                mNotificationManager.cancel(notificationId)
                Timber.i("Network error, closing notification with id=%s", notificationId)
                Toast.makeText(applicationContext, "No internet connection. Upload failed.",
                        Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "An error occurred. Upload failed.",
                        Toast.LENGTH_LONG).show()
                mNotificationManager.cancel(notificationId)
                Timber.i("Generic error, closing notification with id=%s", notificationId)

                bus.post(UploadFailedEvent(fileUri))
            }
        }


        override fun onNext(cloudAppItem: CloudAppItem) {
            val successNotification = NotificationCompat.Builder(this@UploadService)
                    .setContentTitle(cloudAppItem.name)
                    .setSmallIcon(R.drawable.ic_cloud_done)
                    .setOngoing(false)
                    // Open browser when user clicks notification
                    .setContentIntent(
                        PendingIntent.getActivity(this@UploadService, 0,
                                Intent(Intent.ACTION_VIEW).setData(Uri.parse(cloudAppItem.getUrl())),
                                PendingIntent.FLAG_UPDATE_CURRENT))

                    .setColor(ContextCompat.getColor(this@UploadService, R.color.primary))
                    .setWhen(System.currentTimeMillis()).setPriority(Notification.PRIORITY_MAX)
                    .setContentText("Tap to open")
                    // Action to copy link when user clicks the copy link action.
                    .addAction(
                        0, "Copy link", PendingIntent.getService(this@UploadService, 0,
                        Intent(this@UploadService, UploadService::class.java).putExtra(ACTION_TYPE, ACTION_COPY_LINK)
                                .setData(Uri.parse(cloudAppItem.getUrl())),
                    PendingIntent.FLAG_UPDATE_CURRENT))

            // Fire notification with a new id separate from the id used to identify the upload
            // progress notification.
            mNotificationManager.notify(newNotificationId, successNotification.build())

            //Add link to clip board.
            val clip = ClipData.newPlainText("Uploaded item url", cloudAppItem.getUrl())
            mClipboardManager.primaryClip = clip
            Toast.makeText(applicationContext, "Copied: " + cloudAppItem.getUrl(),
                    Toast.LENGTH_LONG).show()

            // Tell the rest of the app that a file was successfully uploaded.
            bus.post(UploadEvent(fileUri, cloudAppItem))
        }
    }

    private inner class NotificationProgress(private val publishSubject: PublishSubject<Long>) : ProgressListener {

        override fun onProgress(current: Long, max: Long) {
            publishSubject.onNext(current)
        }
    }

    companion object {

        val ACTION_TYPE = "com.tevinjeffrey.vapor.services.ACTION_TYPE"
        private val ACTION_COPY_LINK = "com.tevinjeffrey.vapor.services.ACTION_COPY_LINK"
        private val ACTION_UPLOAD_CANCEL = "com.tevinjeffrey.vapor.services.ACTION_UPLOAD_CANCEL"
        private val EXTRA_NOTIFICATION_ID = "com.tevinjeffrey.vapor.services.EXTRA_START_ID"
        val ACTION_UPLOAD = "com.tevinjeffrey.vapor.services.ACTION_UPLOAD"
    }

}
