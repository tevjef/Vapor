package com.tevinjeffrey.vapor.okcloudapp;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import com.tevinjeffrey.vapor.utils.VaporUtils;

import rx.functions.Action1;

public class ProgressNotification implements Action1<Long> {
    private final Long fileSize;
    private final NotificationCompat.Builder builder;
    private final NotificationManager notificationManager;
    private final int notificationId;

    public ProgressNotification(Long fileSize, NotificationCompat.Builder builder, NotificationManager notificationManager, int notificationId) {
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
        builder.setContentInfo(VaporUtils.humanReadableByteCount(current, true));
        notificationManager.notify(notificationId, builder.build());

    }
}
