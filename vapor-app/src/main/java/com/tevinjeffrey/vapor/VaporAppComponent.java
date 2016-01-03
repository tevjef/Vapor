package com.tevinjeffrey.vapor;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.okcloudapp.utils.AuthClient;
import com.tevinjeffrey.vapor.okcloudapp.utils.OkCloudAppModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component (modules = {
        VaporModule.class,
        OkCloudAppModule.class
})
public interface VaporAppComponent {

    Context context();
    Bus bus();
    UserManager userManager();
    DataManager dataManager();
    ClipboardManager clipBoardManager();
    NotificationManager notificationManager();
    DownloadManager downloadManager();
    @AuthClient OkHttpClient client();

    final class Initializer  {
        static VaporAppComponent init(VaporApp vaporApp) {
            return DaggerVaporAppComponent.builder()
                    .vaporModule(new VaporModule(vaporApp))
                    .build();
        }
    }
}
