package com.tevinjeffrey.vapor.okcloudapp;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;

import com.tevinjeffrey.vapor.utils.VaporUtils;

import rx.functions.Action1;

public class ProgressNotification implements Action1<Long> {
    String fileName;
    Long fileSize;
    NotificationCompat.Builder builder;
    NotificationManager notificationManager;
    int notificationId;

    public ProgressNotification(String fileName, Long fileSize, NotificationCompat.Builder builder, NotificationManager notificationManager, int notificationId) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.builder = builder;
        this.notificationManager = notificationManager;
        this.notificationId = notificationId;
    }

    @Override
    public void call(Long current) {
        if (fileSize != 0) {
            double ratio = 1 / ((double) fileSize / current);
            double percentage = ratio * 100;
            int progress = (int) Math.floor(percentage);
            builder.setProgress(100, progress, false);
        }
        builder.setContentTitle(fileName)
                .setContentText("Uploading to CloudApp")
                .setOngoing(true)
                .setContentInfo(VaporUtils.humanReadableByteCount(current, true));
        notificationManager.notify(notificationId, builder.build());

    }
}
