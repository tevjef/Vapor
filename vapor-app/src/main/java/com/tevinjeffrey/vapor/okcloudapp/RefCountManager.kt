package com.tevinjeffrey.vapor.okcloudapp

import android.content.Context
import android.net.Uri

import com.squareup.otto.Subscribe
import com.tevinjeffrey.vapor.events.UploadEvent
import com.tevinjeffrey.vapor.events.UploadFailedEvent
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils

import java.io.File
import java.util.ArrayList

import rx.Observable
import rx.Observer
import rx.functions.Func0
import rx.functions.Func1
import rx.schedulers.Schedulers
import timber.log.Timber

class RefCountManager(private val context: Context) {
    private val uris = ArrayList<Uri>()
    private val notificationIds = ArrayList<Int>()

    fun addNotificationId(startId: Int) {
        notificationIds.add(startId)
    }

    fun removeNotificationId(startId: Int) {
        notificationIds.remove(Integer.valueOf(startId))
    }

    fun getNotificationIds(): List<Int> {
        return notificationIds
    }

            fun addUri(uri: Uri) {
                uris.add(uri)
            }

            private fun cleanUpUri(uri: Uri) {
                // If the list of Uris contains an instance of the parameter uri, then create a file out of it
                // then delete it. Use of RxJava is completely unnecessary.
                Observable.defer {
                    Observable.just(uri).filter {
                        uri -> uri in uris
                    }.flatMap {
                        uri -> Observable.just(FileUtils.getFile(context, uri))
                    }.map {
                        file -> file?.delete()
                    }
                }.subscribeOn(Schedulers.io()).subscribe(object : Observer<Boolean?> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e, "Error while attempting to delete a file created during upload")
                    }

                    override fun onNext(fileDeleted: Boolean?) {
                        if (fileDeleted!!) {
                            Timber.i("Clean up successful")
                        } else {
                            Timber.i("Clean up failed")
                        }
                    }
                })
            }

            @Subscribe
            fun onUploadEvent(uploadEvent: UploadEvent) {
                if (uploadEvent.uri != null) {
                    cleanUpUri(uploadEvent.uri)
                }
            }

            @Subscribe
            fun onUploadFailed(uploadEvent: UploadFailedEvent) {
                if (uploadEvent.uri != null) {
            cleanUpUri(uploadEvent.uri)
        }
    }
}
