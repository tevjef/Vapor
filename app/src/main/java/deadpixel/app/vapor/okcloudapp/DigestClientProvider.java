package deadpixel.app.vapor.okcloudapp;

import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;

import retrofit.client.Client;
import retrofit.client.OkClient;

public class DigestClientProvider implements CLient.Provider {
    @Inject
    DigestAuthenticator
            mDigestAuthenticator;

    @Override
    public Client get() {
        OkHttpClient client = new OkHttpClient();
        client.setAuthenticator(mDigestAuthenticator);
        return new OkClient(client);
    }
}
