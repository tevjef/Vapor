package com.tevinjeffrey.vapor;

import android.content.Context;

import com.facebook.stetho.Stetho;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;
import com.orm.SugarApp;
import com.tevinjeffrey.vapor.okcloudapp.CloudAppService;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

import dagger.ObjectGraph;
import jonathanfinerty.once.Once;
import timber.log.Timber;

public class VaprApp extends SugarApp {

    private static String TAG = "VaprApp";

    ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        Hawk.init(this)
        .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
                .setPassword("A STRONG ASS PASS")
                .setStorage(HawkBuilder.newSqliteStorage(this))
                .setLogLevel(LogLevel.FULL)
                .build();

        objectGraph = ObjectGraph.create(new VaprModule(getApplicationContext()));

        initStetho();

        if (BuildConfig.DEBUG) {
            //When debugging logs will go through the Android logger
            Timber.plant(new Timber.DebugTree());
        }

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

    public static ObjectGraph objectGraph(Context context) {
        return ((VaprApp) context.getApplicationContext()).objectGraph;
    }

    public VaprApp() {

    }


/*
    public static void setHttpAuthentication(String mail, String pw) {
        if (client == null) {
            client = new DefaultHttpClient();
        }
        AuthScope scope = new AuthScope("my.cl.ly", 80);
        client.getCredentialsProvider().setCredentials(scope,
                new UsernamePasswordCredentials(mail, pw));
    }
*/

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
