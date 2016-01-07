package com.tevinjeffrey.vapor;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.StrictMode;

import com.bumptech.glide.Glide;
import com.facebook.stetho.Stetho;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;
import com.orm.SugarContext;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.dagger.DaggerUiComponent;
import com.tevinjeffrey.vapor.dagger.UiComponent;
import com.tevinjeffrey.vapor.dagger.VaporAppComponent;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.events.LogoutEvent;
import com.tevinjeffrey.vapor.okcloudapp.RefCountManager;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

public class VaporApp extends Application {

    private static String TAG = "VaporApp";

    @Inject
    Bus bus;
    @Inject
    NotificationManager notificationManager;
    @Inject
    RefCountManager refCountManager;

    VaporAppComponent vaporAppComponent;
    UiComponent uiComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        //Bad static initializers.
        SugarContext.init(this);
        Hawk.init(this)
        .setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
                .setStorage(HawkBuilder.newSqliteStorage(this))
                .setLogLevel(LogLevel.FULL)
                .build();


        vaporAppComponent = VaporAppComponent.Initializer.init(this);

        uiComponent = DaggerUiComponent.builder()
                .vaporAppComponent(vaporAppComponent)
                .build();

        vaporAppComponent.inject(this);

        registerBus();

        initStetho();

        if (BuildConfig.DEBUG) {
            //When debugging logs will go through the Android logger
            Timber.plant(new Timber.DebugTree());

            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectCustomSlowCalls()
            .detectDiskReads()
            .detectDiskWrites()
            .penaltyLog()
            .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .penaltyLog()
            .build());
        }

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }

    private void registerBus() {
        bus.register(this);
    }

    private void initStetho() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }


    public VaporApp() {
    }

    public static UiComponent uiComponent(Context context) {
        return ((VaporApp) context.getApplicationContext()).uiComponent;
    }

    public static VaporAppComponent appComponent(Context context) {
        return ((VaporApp) context.getApplicationContext()).vaporAppComponent;
    }

    public static void recreateUiComponent(Context context) {
        ((VaporApp) context.getApplicationContext()).uiComponent = DaggerUiComponent.builder()
                .vaporAppComponent(((VaporApp) context.getApplicationContext()).vaporAppComponent)
                .build();
    }

    public static void recreateVaporComponent(Context context) {
        ((VaporApp) context.getApplicationContext()).vaporAppComponent =
                VaporAppComponent.Initializer.init((VaporApp)context.getApplicationContext());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
        bus.unregister(this);

        for (Integer notificationId : refCountManager.getNotificationIds()) {
            notificationManager.cancel(notificationId);
            Timber.i("CLEAN UP from onTerminate with id=%s", notificationId);
        }
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
    }

    @Subscribe
    public void onLogoutEvent(LogoutEvent event) {
        VaporApp.recreateVaporComponent(this);
        VaporApp.recreateUiComponent(this);
        vaporAppComponent.inject(this);
        registerBus();

        try {
            vaporAppComponent.client().getCache().evictAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*http://stackoverflow.com/questions/4028742/how-to-clear-a-notification-if-activity-crashes*/
    public class CrashHandler implements Thread.UncaughtExceptionHandler {

        private Thread.UncaughtExceptionHandler defaultUEH;
        private NotificationManager notificationManager;

        public CrashHandler(Context context) {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        public void uncaughtException(Thread t, Throwable e) {
            for (Integer notificationId : refCountManager.getNotificationIds()) {
                notificationManager.cancel(notificationId);
                Timber.i("CLEAN UP from uncaughtException with id=%s", notificationId);
            }
            notificationManager = null;
            defaultUEH.uncaughtException(t, e);
        }
    }
}
