package com.tevinjeffrey.vapor;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.tevinjeffrey.vapor.ui.files.adapters.FilesVH;
import com.tevinjeffrey.vapor.services.UploadService;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BottomSheetPresenterImpl;
import com.tevinjeffrey.vapor.ui.UiModule;
import com.tevinjeffrey.vapor.ui.files.FilesActivity;
import com.tevinjeffrey.vapor.ui.login.LoginActivity;
import com.tevinjeffrey.vapor.okcloudapp.OkCloudAppModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                LoginActivity.class,
                FilesActivity.class,
                UploadService.class,
                FilesVH.class,
                BottomSheetPresenterImpl.class

        },
        includes = {OkCloudAppModule.class,
                UiModule.class
                },
library = true)

public class VaprModule {
    private final VaprApp applicationContext;

    public VaprModule(Context applicationContext) {
        this.applicationContext = (VaprApp) applicationContext;
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
