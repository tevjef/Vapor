package deadpixel.app.vapor.cloudapp.impl;

import android.app.Activity;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import deadpixel.app.vapor.callbacks.AccountUpdateCallback;
import deadpixel.app.vapor.callbacks.ResponseCallback;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.impl.model.AccountResponseModel;
import deadpixel.app.vapor.cloudapp.impl.model.AccountStatsResponseModel;
import deadpixel.app.vapor.networkOp.RequestExecutors;

/**
 * Created by Tevin on 6/16/2014.
 */
public class AccountStatsImpl extends RequestExecutors implements ResponseCallback {

    Activity activity;
    AccountStatsResponseModel accountStatsResponseModel;
    String response;

    private AccountUpdateCallback aCallback;

    Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public AccountStatsImpl(Activity activity) {
        super();
        this.activity = activity;
    }

    public void requestAccountStats() throws CloudAppException{
        executeGet(ACCOUNT_STATS_URL, 200);
    }

    public CloudAppAccountStats getAccountStats() {
        return accountStatsResponseModel;
    }

    private void updateData(String response) {
        try {
            //did this solely to make sure the correct object
            // was getting to the correct object
            accountStatsResponseModel = gson.fromJson(response, AccountStatsResponseModel.class);
            //skips this line if exception is caught.
            this.response = response;
        } catch (JsonSyntaxException e) {
            Log.i("AccountStatsImpl",  "Warning: Json object mismatch...." + e.getMessage());
        }
    }

    @Override
    public void onServerResponse(String response) {
        updateData(response);

    }

    @Override
    public void onServerError(VolleyError e, String errorDescription) {

    }

}
