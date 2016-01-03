package com.tevinjeffrey.vapor;

import android.app.Application;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.facebook.stetho.Stetho;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;
import com.orm.SugarContext;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.ui.DaggerUiComponent;
import com.tevinjeffrey.vapor.ui.UiComponent;

import timber.log.Timber;

public class VaporApp extends Application {

    private static String TAG = "VaporApp";

    DataManager dataManager;

    VaporAppComponent vaporAppComponent;
    UiComponent uiComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        //Bad static initializers.
        SugarContext.init(this);
        Hawk.init(this)
        .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
                .setPassword("ASTRONGASSPASS")
                .setStorage(HawkBuilder.newSqliteStorage(this))
                .setLogLevel(LogLevel.FULL)
                .build();


        vaporAppComponent = VaporAppComponent.Initializer.init(this);

        uiComponent = DaggerUiComponent.builder()
                .vaporAppComponent(vaporAppComponent)
                .build();


        initStetho();

        if (BuildConfig.DEBUG) {
            //When debugging logs will go through the Android logger
            Timber.plant(new Timber.DebugTree());
        }
        dataManager = vaporAppComponent.dataManager();
        dataManager.syncAllItems();
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
    }
}
