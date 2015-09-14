package com.tevinjeffrey.vapor.okcloudapp;

import android.content.Context;
import android.text.TextUtils;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
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
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
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
        client.interceptors().add(new LoggingInterceptor());
        client.interceptors().add(new StethoInterceptor());

        File httpCacheDir = new File(context.getCacheDir(), context.getString(R.string.app_name));
        long httpCacheSize = 50 * 1024 * 1024; // 50 MiB
        Cache cache = new Cache(httpCacheDir, httpCacheSize);
        client.setCache(cache);
        return client;
    }

    @Provides
    @Singleton
    public DataManager providesDataManager(CloudAppService cloudAppService, UserManager userManager, Bus bus, Client client) {
        return new DataManager(cloudAppService, userManager, bus, client);
    }

    @Provides
    @Singleton
    public Client providesAuthClient(OkHttpClient client, DigestAuthenticator digestAuthenticator) {
        OkHttpClient okClient = client.clone();
        okClient.setAuthenticator(digestAuthenticator);
        return new OkClient(okClient);
    }

    @Provides
    @Singleton
    public CloudAppService providesCloudAppService(Client client) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(client)
                .setEndpoint("http://my.cl.ly")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new GsonBuilder()
                        .setPrettyPrinting()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()))
                .build();
        return restAdapter.create(CloudAppService.class);
    }

    class LoggingInterceptor implements Interceptor {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Timber.i(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Timber.i(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    }

}
