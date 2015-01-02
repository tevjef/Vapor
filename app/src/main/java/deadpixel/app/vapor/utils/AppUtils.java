package deadpixel.app.vapor.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import deadpixel.app.vapor.R;
import deadpixel.app.vapor.cloudapp.api.CloudApp;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.impl.CloudAppImpl;
import deadpixel.app.vapor.database.PreferenceHandler;

/**
 * Created by Tevin on 6/24/2014.
 */
public class AppUtils extends Application {

    public static final String FILE_TOO_LARGE = "file_too_large";
    public static final String UPLOAD_TICKETS_ZERO = "upload_tickets_zero";
    public static final String PREF_MAX_UPLOAD_SIZE = "max_upload_size";
    private static String TAG = "AppUtils/Application: ";

    ////////////////////////////////////////
    //Singleton initialization
    ////////////////////////////////////////
    private static AppUtils sInstance;
    private static Context mContext;
    public static SharedPreferences mPref;

    public AppUtils() {

    }
    public AppUtils (Context context) {
        mContext = context;
    }


    @Override
    public void onCreate() {
        super.onCreate();


        // initialize the singleton
        sInstance = this;
        AppUtils.mContext = getApplicationContext();

        api = new CloudAppImpl(mContext);
        bus = new Bus(ThreadEnforcer.ANY);



        //Instantiates a preference to be acted upon.

        PreferenceHandler preferenceHandler = new PreferenceHandler(getApplicationContext());

        mPref =  PreferenceManager.getDefaultSharedPreferences(this);

        //Clear preference on application start
        //mPref.edit().clear().commit();

        SharedPreferences.Editor editor = mPref.edit();






        if(getEmail().equals("") || getEmail().equals("")) {
            Log.i("Application/AppUtils", "Email and Pass empty, must be first start up");
        } else {
            setHttpAuthentication(getEmail(), getEmail());
            Log.i("Application/AppUtils", "Authentication set");
        }

    }

    public static synchronized AppUtils getInstance() {
        return sInstance;
    }


    public static CloudApp api;
    public static CloudAppAccount account;
    public static CloudAppAccountStats accountStats;

    ////////////////////////////////////////
    //General application utilities
    ////////////////////////////////////////
    public static final String VAPOR = "Vapor_";
    public static final String APP_FIRST_START = VAPOR + "first_start";
    public static final String SIGNED_IN = VAPOR + "signed_in";
    public static final String STARTUP_ANIMATION_RAN = VAPOR + "start_up_animation_ran";
    public static final String AUTH_ACTIVITY_STATE = VAPOR + "auth_activity_state";
    public static final String FULLY_SYNCED = VAPOR + "fully_synced";
    public static final String CALCULATED_TOTAL_ITEMS = VAPOR + "calculated_total";
    public static final String FILES_INSERTED = VAPOR + "files_inserted";
    public static final String ITEMS_GOTTEN_SUCCESSFULLY = VAPOR + "pages_gotten";
    public static final int DEFAULT_FILE_REQUEST_SIZE = 40;
    public static final String EXTRA_BOOKMARK_NAME = VAPOR + "bookmark_name";
    public static final String EXTRA_BOOKMARK_URL = VAPOR + "bookmark_url";
    public static final String EXTRA_FILE_URI = VAPOR + "file_uri";
    public static final String EXTRA_FILE_NAME = VAPOR + "file_name";
    public static final String EXTRA_FILE_SIZE = VAPOR + "file_size";


    public static Typeface mNormal;
    public static Typeface mBold;
    private static Typeface typeface;
    private static SpannableString s;

    public static final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final DateFormat formatBis = new SimpleDateFormat(
            "yyyy-MM-dd");

    public enum TextStyle {
        LIGHT_NORMAL, NORMAL,  BOLD
    }

    public static boolean isValidWebAddress(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.DOMAIN_NAME.matcher(target).matches();
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    public static boolean isEmpty(CharSequence target) {
        return TextUtils.isEmpty(target);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = null;
        if(cm != null) {
            ni = cm.getActiveNetworkInfo();
        }

        boolean b = (ni != null)&& ni.isConnectedOrConnecting();
        return b;
    }

    public static Typeface getTextStyle(TextStyle ts) {

        switch (ts) {
            case LIGHT_NORMAL:
                typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
                break;
            case NORMAL:
                typeface = Typeface.create("sans-serif", Typeface.NORMAL);
                break;
            case BOLD:
                typeface = Typeface.create("sans-serif", Typeface.BOLD);
                break;

            default:
                break;
        }
        return typeface;
    }

    public void linkShareIntent(String link) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, link);
        getApplicationContext().startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    public void linkDownloadIntent(String link) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        getApplicationContext().startActivity(i);
    }

    public final static String AM_ACCOUNT_TYPE = "AM_ACCOUNT_TYPE";
    public final static String AM_SUBSCRIPTION_END = "AM_SUBSCRIPTION_END";
    public final static String AM_CUSTOM_DOMAIN = "AM_CUSTOM_DOMAIN";
    public final static String AM_MANAGEMENT = "AM_MANAGEMENT";
    public final static String AM_CHANGE_EMAIL = "AM_CHANGE_EMAIL";
    public final static String AM_CHANGE_PASSWORD = "AM_CHANGE_PASSWORD";
    public final static String AM_PRIVATE_LINKS = "AM_PRIVATE_LINKS";
    public final static String AM_STATISTICS = "AM_STATISTICS";
    public final static String AM_TOTAL_VIEWS = "AM_TOTAL_VIEWS";
    public final static String AM_UPLOADS_TODAY = "AM_UPLOADS_TODAY";
    public final static String AM_TOTAL_ITEMS = "AM_TOTAL_ITEMS";
    public final static String AM_MEMBER_SINCE = "AM_MEMBER_SINCE";


    public final static String CD_CUSTOM_DOMAIN = "CD_CUSTOM_DOMAIN";
    public final static String CD_CUSTOM_DOMAIN_HOMEPAGE = "CD_CUSTOM_DOMAIN_HOMEPAGE";
    public final static String CD_CUSTOM_DOMAIN_HOMEPAGE_FIELD = "CD_CUSTOM_DOMAIN_HOMEPAGE_FIELD";
    public final static String CD_FIELD_CUSTOM_DOMAIN_FIELD= "CD_FIELD_CUSTOM_DOMAIN_FIELD";
    public final static String CD_BTN_UPDATE = "CD_BTN_UPDATE";

    public final static String CE_CURRENT_EMAIL = "CE_CURRENT_EMAIL";
    public final static String CE_EMAIL_FIELD = "CE_EMAIL_FIELD";
    public final static String CE_PASS_FIELD = "CE_PASS_FIELD";
    public final static String CE_BTN_UPDATE = "CD_BTN_UPDATE";

    public final static String CP_CURRENT_PASSWORD = "CP_CURRENT_PASSWORD";
    public final static String CP_NEW_PASSWORD_FIELD = "CP_NEW_PASSWORD_FIELD";
    public final static String CP_CONFIRM_NEW_PASSWORD_FIELD = "CP_CONFIRM_NEW_PASSWORD_FIELD";
    public final static String CP_BTN_UPDATE = "CP_BTN_UPDATE";


    public final static String PREF_EMAIL = TAG + "email";
    public final static String PREF_DOMAIN = TAG + "domain";
    public final static String PREF_DOMAIN_HOMEPAGE = TAG + "domain_homepage";
    public final static String PREF_SUBSCRIBED = TAG + "is_subscribed";
    public final static String PREF_PRIVATE_ITEMS = TAG + "is_privateItems";
    public final static String PREF_SUBSCRIPTION_EXPIRES_AT = TAG + "subscription_expires_at";
    public final static String PREF_CREATED_AT = TAG + "created_at";
    public final static String PREF_UPDATED_AT = TAG + "updated_at";
    public final static String PREF_ACTIVATED_AT = TAG + "activated_at";
    public final static String PREF_SOCKET_AUTH_URL = TAG + "auth_url";
    public final static String PREF_SOCKET_API_KEY = TAG + "api_key";
    public final static String PREF_CHANNEL_ITEMS = TAG + "channel_items";
    public final static String PREF_SOCKET_APP_ID = TAG + "app_id";
    public final static String PREF_ID = TAG + "id";

    public final static String PREF_TOTAL_VIEWS = TAG + "total_views";
    public final static String PREF_TOTAL_ITEMS = TAG + "total_items";
    public final static String PREF_UPLAODS_TODAY = TAG + "uploads_today";


    public final static String NO_CONNECTION = "No connection";


    ////////////////////////////////////////
    //Volley Library Setup
    ////////////////////////////////////////

    public static RequestQueue queue;
    public static DefaultHttpClient client;


    public static void setPass(String pass) {
        invalidateAuthentication();
        mPref.edit().putString("pass", pass).commit();
        Log.i("Pass saved to Preferences", "  " + pass);
    }
    public static void setEmail(String email) {
        invalidateAuthentication();
        mPref.edit().putString("email", email).commit();
        Log.i("Email saved to Preferences", "  " + email);
    }
    public static String getPass() {
        return mPref.getString("pass", "");
    }
    public static String getEmail() {
        String s = mPref.getString("email", "");
        return s;
    }

    public static void invalidateAuthentication() {
        queue = null;
        client = null;
    }

    public static void setAuth() {
        String e = mPref.getString("email", null);
        String p = mPref.getString("pass", null);
        if(e == null || p == null) {
            Log.e(TAG, "Email or Password from preferences is null");
        } else    {
            AppUtils.setHttpAuthentication(e,p);
        }
    }

    private static void setHttpAuthentication(String mail, String pw) {
        if (client == null) {
            client = new DefaultHttpClient();
        }


        AuthScope scope = new AuthScope("my.cl.ly", 80);
        client.getCredentialsProvider().setCredentials(scope,
                new UsernamePasswordCredentials(mail, pw));
        Log.i(TAG, "Setting Authentication of client" + mPref.getString("email", null) + "  " + mPref.getString("pass", null));
    }

    public static RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time

        if (queue == null) {
            queue = Volley.newRequestQueue(mContext, new HttpClientStack(client));
        }
        return queue;
    }

    public static <T> void addToRequestQueue(Request<T> req) {
            // set the default tag if tag is empty
            Log.i("Adding request to queue: %s", req.getUrl());
            Log.i("Request Info: " + "Method ", Integer.toString(req.getMethod()));
            //req.setShouldCache(false);
            req.setTag(TAG);

            req.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 15000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 5;
                }

                @Override
                public void retry(VolleyError volleyError) throws VolleyError {

                }
            });
            setAuth();

            getRequestQueue().add(req);
        }

    ////////////////////////////////////////
    //EventBus library Setup
    ////////////////////////////////////////

    private static Bus bus;

    public static synchronized Bus getEventBus() {
        return bus;
    }

    ////////////////////////////////////////
    //Crouton library Setup
    ////////////////////////////////////////

    public enum Style {
        INFO, ALERT, SUCCESS, PROGRESS
    }
    public static void makeCrouton(Activity context, CharSequence charSequence, Style style) {

        Configuration config = new Configuration.Builder()
                .setDuration(Configuration.DURATION_LONG).build();


        if (style == Style.ALERT) {
            Crouton.makeText(context, charSequence, new de.keyboardsurfer.android.widget.crouton.Style.Builder()
                    //.setTextAppearance(R.style.Text_Light)
                    .setBackgroundColor(R.color.error)
                    .setTextColor(R.color.white)
                    .setTextShadowColor(R.color.primary_text_color)
                    .build()).show();
        } else if (style == Style.INFO) {
            Crouton.makeText(context, charSequence, new de.keyboardsurfer.android.widget.crouton.Style.Builder()
                    .setHeightDimensionResId(R.dimen.toast)
                    //.setTextAppearance(R.style.Text_Light)
                    .setBackgroundColor(R.color.activity_bg)
                    .setTextColor(R.color.primary_text_color)
                    .build()).show();

        } else if (style == Style.SUCCESS) {
            Crouton.makeText(context, charSequence, new de.keyboardsurfer.android.widget.crouton.Style.Builder()
                    //.setTextAppearance(R.style.Text_Light_Small)
                    .setBackgroundColor(R.color.success)
                    .setTextColor(R.color.white)
                    .setTextShadowColor(R.color.primary_text_color)
                    .setConfiguration(config)
                    .build()).show();
        }
    }

}
