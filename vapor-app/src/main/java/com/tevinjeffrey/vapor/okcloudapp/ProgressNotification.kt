package com.tevinjeffrey.vapor.okcloudapp

import android.app.NotificationManager
import android.support.v4.app.NotificationCompat

import com.tevinjeffrey.vapor.utils.VaporUtils

import rx.functions.Action1

class ProgressNotification(private val fileSize: Long, private val builder: NotificationCompat.Builder, private val notificationManager: NotificationManager, private val notificationId: Int) : Action1<Long> {

    override fun call(current: Long?) {
        if (!fileSize.equals(0)) {
            val ratio = 1 / (fileSize as Double / current!!)
            val percentage = ratio * 100
            val progress = Math.floor(percentage).toInt()
            builder.setProgress(100, progress, false)
        }
        builder.setContentInfo(VaporUtils.humanReadableByteCount(current!!, true))
        notificationManager.notify(notificationId, builder.build())

    }
}
