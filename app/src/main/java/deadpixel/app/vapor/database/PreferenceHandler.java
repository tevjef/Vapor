package deadpixel.app.vapor.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;

import deadpixel.app.vapor.callbacks.AccountStatsUpdateEvent;
import deadpixel.app.vapor.callbacks.AccountUpdateEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.okcloudapp.model.AccountModel;
import deadpixel.app.vapor.okcloudapp.model.AccountStatsModel;
import deadpixel.app.vapor.cloudapp.impl.model.UploadResponseModel;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/27/2014.
 */
public class PreferenceHandler {

    static Context mContext;

    private static final String TAG = "PreferenceHandler";

    public PreferenceHandler(Context context) {
        mContext = context;
    }


    public static void saveUploadDetails(UploadResponseModel uploadResponseModel) {
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor prefEditor = prefs.edit();

        if(uploadResponseModel != null) {

            if (uploadResponseModel.getMax_upload_size() != null) {
                prefEditor.putLong(AppUtils.PREF_MAX_UPLOAD_SIZE, uploadResponseModel.getMax_upload_size());
            }
            if (uploadResponseModel.getUploads_remaining() != null) {
                prefEditor.putLong(AppUtils.AM_UPLOADS_TODAY, uploadResponseModel.getUploads_remaining());
            }
        }
    }

    public static void saveAccountStats(CloudAppAccountStats stats) {
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor prefEditor = prefs.edit();

        if(stats != null) {

            AppUtils.accountStats =  stats;

            prefEditor.putLong(AppUtils.PREF_TOTAL_ITEMS, stats.getItems());
            prefEditor.putLong(AppUtils.PREF_TOTAL_VIEWS, stats.getViews());
            prefEditor.commit();

            Log.i(TAG, "AccountStats has been updated in SharedPreferences.");
            AppUtils.getEventBus().post(new AccountStatsUpdateEvent(getAccountStats()));
        }


    }

    public static CloudAppAccountStats getAccountStats() {

        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(mContext);

        AccountStatsModel stats = new AccountStatsModel();

        stats.setItems(prefs.getLong(AppUtils.PREF_TOTAL_ITEMS, 0));
        stats.setViews(prefs.getLong(AppUtils.PREF_TOTAL_VIEWS, 0));

        return stats;
    }
    public static void saveAccountDetails(CloudAppAccount account) throws CloudAppException, ParseException {

        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(mContext);

        SharedPreferences.Editor prefEditor = prefs.edit();

        if(account != null) {
            AppUtils.account = account;

            //Save email
            prefEditor.putString(AppUtils.PREF_EMAIL, account.getEmail());
            AppUtils.setEmail(account.getEmail());

            //Save Id
            prefEditor.putLong(AppUtils.PREF_ID, account.getId());

            //Save Id
            prefEditor.putLong(AppUtils.PREF_ID, account.getId());

            //Save Subscription Status
            prefEditor.putBoolean(AppUtils.PREF_SUBSCRIBED, account.isSubscribed());

            //Save account Privacy
            prefEditor.putBoolean(AppUtils.PREF_PRIVATE_ITEMS, account.getDefaultSecurity() == CloudAppAccount.DefaultSecurity.PRIVATE);

            if (account.getDomain() == null) {
                //Save Domain
                prefEditor.putString(AppUtils.PREF_DOMAIN, "null_value");
            } else {
                //Save Domain
                prefEditor.putString(AppUtils.PREF_DOMAIN, account.getDomain());
            }

            if (account.getDomainHomePage() == null) {
                //Save Domain Homepage
                prefEditor.putString(AppUtils.PREF_DOMAIN_HOMEPAGE, "null_value");
            } else {
                //Save Domain Homepage
                prefEditor.putString(AppUtils.PREF_DOMAIN_HOMEPAGE, account.getDomainHomePage());
            }

            if (account.getSubscriptionExpiresAt() == null) {
                //Save Subscription Expire At
                prefEditor.putString(AppUtils.PREF_SUBSCRIPTION_EXPIRES_AT, "null_value");
            } else {
                //Save Subscription Expire At
                prefEditor.putString(AppUtils.PREF_SUBSCRIPTION_EXPIRES_AT, account.getSubscriptionExpiresAt());
            }

            if (account.getActivatedAt() == null) {
                //Save Activated At
                prefEditor.putString(AppUtils.PREF_ACTIVATED_AT, "null_value");
            } else {
                //Save Activated At
                prefEditor.putString(AppUtils.PREF_ACTIVATED_AT, account.getActivatedAt());
            }

            if (account.getCreatedAt() == null) {
                //Save Created At
                prefEditor.putString(AppUtils.PREF_CREATED_AT, "null_value");
            } else {
                //Save Created At
                prefEditor.putString(AppUtils.PREF_CREATED_AT, account.getCreatedAt());
            }


            if (account.getUpdatedAt() == null) {
                //Save Updated At
                prefEditor.putString(AppUtils.PREF_UPDATED_AT, "null_value");
            } else {
                //Save Updated At
                prefEditor.putString(AppUtils.PREF_UPDATED_AT, account.getUpdatedAt());
            }


            if (account.getSocket() != null) {
                //Save Api Key
                prefEditor.putString(AppUtils.PREF_SOCKET_API_KEY, account.getSocket().getApiKey());

                //Save App Id
                prefEditor.putLong(AppUtils.PREF_SOCKET_APP_ID, account.getSocket().getAppId());

                //Save Auth Url
                prefEditor.putString(AppUtils.PREF_SOCKET_AUTH_URL, account.getSocket().getAuthUrl());

                //Save Channel: accounts
                prefEditor.putString(AppUtils.PREF_CHANNEL_ITEMS, account.getSocket().getChannels().getItems());

            }

            prefEditor.commit();

            Log.i(TAG, "Account has been updated in SharedPreferences.");
            AppUtils.getEventBus().post(new AccountUpdateEvent(getAccountDetails()));
        }
        else
            Log.i(TAG, "Account details are null");

    }

    public static CloudAppAccount getAccountDetails() {

        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(mContext);

        AccountModel accountModel = new AccountModel();

        accountModel.setEmail(prefs.getString(AppUtils.PREF_EMAIL, "null_value"));
        accountModel.setId(prefs.getLong(AppUtils.PREF_ID, -1));
        accountModel.setDomain(prefs.getString(AppUtils.PREF_DOMAIN, "null_value"));
        accountModel.setDomainHomePage(prefs.getString(AppUtils.PREF_DOMAIN_HOMEPAGE, "null_value"));
        accountModel.setActivatedAt(prefs.getString(AppUtils.PREF_ACTIVATED_AT, "null_value"));
        accountModel.setCreatedAt(prefs.getString(AppUtils.PREF_CREATED_AT, "null_value"));
        accountModel.setUpdatedAt(prefs.getString(AppUtils.PREF_UPDATED_AT, "null_value"));
        accountModel.setPrivateItems(prefs.getBoolean(AppUtils.PREF_PRIVATE_ITEMS, false));
        accountModel.setSubscribed(prefs.getBoolean(AppUtils.PREF_SUBSCRIBED, false));


        AccountModel.Socket socket = new AccountModel.Socket();
        AccountModel.Channels channels = new AccountModel.Channels();

        accountModel.setSocket(socket);

        accountModel.getSocket().setChannels(channels);

        socket.setApiKey(prefs.getString(AppUtils.PREF_SOCKET_API_KEY, "null_value"));
        socket.setAuthUrl(prefs.getString(AppUtils.PREF_SOCKET_AUTH_URL, "null_value"));
        socket.setAppId(prefs.getLong(AppUtils.PREF_SOCKET_APP_ID, -1));

        socket.getChannels().setItems(prefs.getString(AppUtils.PREF_CHANNEL_ITEMS, "null_value"));

        return accountModel;
    }

    public static void updateFilesInserted(int numOfFiles) {
        AppUtils.mPref.edit().putInt(AppUtils.FILES_INSERTED, getInsertedFilesCount() + numOfFiles).commit();
    }
    public static int getInsertedFilesCount() {
        return AppUtils.mPref.getInt(AppUtils.FILES_INSERTED, 0);
    }
}
