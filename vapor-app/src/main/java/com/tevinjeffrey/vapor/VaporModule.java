package com.tevinjeffrey.vapor;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class VaporModule {
    private final VaporApp applicationContext;

    public VaporModule(Context applicationContext) {
        this.applicationContext = (VaporApp) applicationContext;
    }


    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return applicationContext;
    }

    @Provides
    @Singleton
    public NotificationManager provideNotificationManager() {
        return (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    public ClipboardManager provideClipboardManager() {
        return (ClipboardManager)
                applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Provides
    public DownloadManager provideDownloadManager() {
        return (DownloadManager)
                applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Provides
    @Singleton
    public Bus providesEventBus() {
        return new Bus(ThreadEnforcer.MAIN);
    }
}
