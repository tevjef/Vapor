package com.tevinjeffrey.vapor.dagger;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.DaggerVaporAppComponent;
import com.tevinjeffrey.vapor.VaporApp;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.RefCountManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component (modules = {
        VaporModule.class,
        OkCloudAppModule.class
})
public interface VaporAppComponent {

    void inject(VaporApp vaporApp);

    Context context();
    Bus bus();
    UserManager userManager();
    DataManager dataManager();
    ClipboardManager clipBoardManager();
    NotificationManager notificationManager();
    DownloadManager downloadManager();
    @AuthClient OkHttpClient client();
    RefCountManager persistentManager();

    final class Initializer  {
        static VaporAppComponent init(VaporApp vaporApp) {
            return DaggerVaporAppComponent.builder()
                    .vaporModule(new VaporModule(vaporApp))
                    .build();
        }
    }
}
