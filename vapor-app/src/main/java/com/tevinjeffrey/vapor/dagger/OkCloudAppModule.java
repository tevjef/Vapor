package com.tevinjeffrey.vapor.dagger;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;
import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.okcloudapp.CloudAppService;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.DigestAuthenticator;
import com.tevinjeffrey.vapor.okcloudapp.RefCountManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.DigestScheme;

import java.io.File;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

@Module
class OkCloudAppModule {

    @Provides
    @Singleton
    public DigestAuthenticator provideDigestAuthenticator(DigestScheme digestScheme, @Nullable Credentials credentials) {
        return new DigestAuthenticator(digestScheme, credentials);
    }

    @Provides
    @Singleton
    public DigestScheme providesDigestScheme() {
        return new DigestScheme();
    }

    @Provides
    @Nullable
    public Credentials providesCredentials() {
        String email = UserManager.getUserName();
        String pass = UserManager.getPassword();

        if (email != null & pass != null && !TextUtils.isEmpty(email))
            return new UsernamePasswordCredentials(email, pass);
        else
            return null;
    }

    @Provides
    @Singleton
    public UserManager provideUserManager(CloudAppService cloudAppService, DigestAuthenticator digestAuthenticator, Bus bus) {
        return new UserManager(cloudAppService, digestAuthenticator, bus);
    }

    @Provides
    @Singleton
    public OkHttpClient providesOkHttpClient(Context context) {
        OkHttpClient client = new OkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        client.interceptors().add(interceptor);
        client.networkInterceptors().add(new StethoInterceptor());
        File httpCacheDir = new File(context.getCacheDir() + "/okhttp");
        httpCacheDir.mkdirs();
        cleanDir(httpCacheDir);
        long httpCacheSize = 50 * 1024 * 1024; // 50 MiB
        Cache cache = new Cache(httpCacheDir, httpCacheSize);
        client.setCache(cache);
        return client;
    }

    private static void cleanDir(File dir) {
        File[] files = dir.listFiles();

        for (File file : files) {
            file.delete();
        }
    }

    @Provides
    @Singleton
    public DataManager providesDataManager(CloudAppService cloudAppService, UserManager userManager, Bus bus, @AuthClient OkHttpClient client) {
        return new DataManager(cloudAppService, userManager, bus, client);
    }

    @Provides
    @Singleton
    @AuthClient
    public OkHttpClient providesAuthClient(OkHttpClient client, DigestAuthenticator digestAuthenticator) {
        OkHttpClient okClient = client.clone();
        okClient.setAuthenticator(digestAuthenticator);
        return okClient;
    }

    @Provides
    @Singleton
    public CloudAppService providesCloudAppService(@AuthClient OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(HttpUrl.parse("http://my.cl.ly"))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setPrettyPrinting()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()))
                .build();
        return retrofit.create(CloudAppService.class);
    }

    @Provides
    @Singleton
    public RefCountManager providePersistentManager(Context context, Bus bus) {
        RefCountManager refCountManager = new RefCountManager(context);
        bus.register(refCountManager);
        return refCountManager;
    }
}
