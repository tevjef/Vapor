package com.tevinjeffrey.vapr;

import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.tevinjeffrey.vapr.okcloudapp.CloudAppService;
import com.tevinjeffrey.vapr.okcloudapp.DigestAuthenticator;
import com.tevinjeffrey.vapr.okcloudapp.UserManager;
import com.tevinjeffrey.vapr.services.UploadService;
import com.tevinjeffrey.vapr.ui.UiModule;
import com.tevinjeffrey.vapr.ui.files.FilesActivity;
import com.tevinjeffrey.vapr.ui.login.LoginActivity;
import com.tevinjeffrey.vapr.okcloudapp.OkCloudAppModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                LoginActivity.class,
                FilesActivity.class,
                UploadService.class

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
    @Singleton
    public ClipboardManager provideClipboardManager() {
        return (ClipboardManager)
                applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Provides
    @Singleton
    public Bus providesEventBus() {
        return new Bus(ThreadEnforcer.MAIN);
    }
}
