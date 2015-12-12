package com.tevinjeffrey.vapor.okcloudapp;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.R;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.DigestScheme;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import timber.log.Timber;

@Module(
        injects = {
            DigestAuthenticator.class,
        },
        complete = false,
        library = true)

public class OkCloudAppModule {

    @Provides
    @Singleton
    public DigestAuthenticator provideDigestAuthenticator(DigestScheme digestScheme, Credentials credentials) {
        return new DigestAuthenticator(digestScheme, credentials);
    }

    @Provides
    @Singleton
    public DigestScheme providesDigestScheme() {
        return new DigestScheme();
    }

    @Provides
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
        client.interceptors().add(getFixInterceptor());
        client.networkInterceptors().add(new LoggingInterceptor());
        client.networkInterceptors().add(new StethoInterceptor());

        File httpCacheDir = new File(context.getCacheDir(), context.getString(R.string.app_name));
        long httpCacheSize = 50 * 1024 * 1024; // 50 MiB
        Cache cache = new Cache(httpCacheDir, httpCacheSize);
        client.setCache(cache);
        return client;
    }

    @Provides
    @Singleton
    public DataManager providesDataManager(CloudAppService cloudAppService, UserManager userManager, Bus bus) {
        return new DataManager(cloudAppService, userManager, bus);
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

    class LoggingInterceptor implements Interceptor {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Timber.i(String.format("%nSending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Timber.i(String.format("%nReceived response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));
            return response;
        }
    }

    public Interceptor getFixInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request request = chain.request();
                request.headers();
                return chain.proceed(chain.request());
            }
        };
    }


}
