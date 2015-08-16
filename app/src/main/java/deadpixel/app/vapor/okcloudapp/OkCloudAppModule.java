package deadpixel.app.vapor.okcloudapp;

import com.squareup.okhttp.OkHttpClient;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.DigestScheme;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.mime.TypedFile;

@Module(
        injects = {
            DigestAuthenticator.class,
                DigestClientProvider.class,
        },
        complete = false,
        library = true)

public class OkCloudAppModule {

    @Provides
    public DigestAuthenticator provideDigestAuthenticator(DigestScheme digestScheme, Credentials credentials) {
        return new DigestAuthenticator(digestScheme, credentials);
    }

    @Provides
    public DigestScheme providesDigestScheme() {
        return new DigestScheme();
    }

    @Provides
    public Credentials providesCredentials() {
        return new UsernamePasswordCredentials(null, null);
    }

    @Provides
    @Singleton
    public OkHttpClient providesOkHttpClient() {
        return new OkHttpClient();
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
                .build();
        return restAdapter.create(CloudAppService.class);
    }

}
