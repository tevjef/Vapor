package deadpixel.app.vapor.networkOp;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by Tevin on 1/18/14.
 */
public class RequestHandler extends Application {

    private static String TAG = "deadpixel.app.vapor.networkOp";
    private static RequestHandler sInstance;
    private static Context context;
    public static RequestQueue queue;
    public static DefaultHttpClient client;
    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        sInstance = this;
        RequestHandler.context = getApplicationContext();
    }

    /**
     * @return RequestHandler singleton instance
     */
    public static synchronized RequestHandler getInstance() {
        return sInstance;
    }

    public RequestHandler() {

    }

    public static void setHttpAuthentication(String mail, String pw) {
        if (client == null) {
            client = new DefaultHttpClient();
        }
        AuthScope scope = new AuthScope("my.cl.ly", 80);
        client.getCredentialsProvider().setCredentials(scope,
                new UsernamePasswordCredentials(mail, pw));
        Log.i(TAG, "Authentication set.");
    }

    public static void setHttpAuthentication() {
        if (client == null) {
            client = new DefaultHttpClient();
        }
        Log.i(TAG, "No Authentication set.");
    }

    public static RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (queue == null) {
            queue = Volley.newRequestQueue(context, new HttpClientStack(client));
        }
        return queue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding + \"" + tag + "\" request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    public static <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        Log.i("Adding request to queue: %s", req.getUrl());
        Log.i("Request Info: " + "Method ", Integer.toString(req.getMethod()));
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
}
