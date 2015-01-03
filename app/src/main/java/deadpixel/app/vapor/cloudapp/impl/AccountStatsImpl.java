package deadpixel.app.vapor.cloudapp.impl;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.impl.model.AccountStatsModel;
import deadpixel.app.vapor.database.DatabaseManager;
import deadpixel.app.vapor.networkOp.RequestExecutor;

/**
 * Created by Tevin on 6/16/2014.
 */
public class AccountStatsImpl {


    AccountStatsModel accountStatsModel;
    String response;

    private static final String TAG = "AccountStatsImpl";
    public static final String MY_CL_LY = "http://my.cl.ly";
    public static final String REGISTER_URL = MY_CL_LY+ "/register";
    public static final String ACCOUNT_URL = MY_CL_LY + "/account";
    public static final String ACCOUNT_STATS_URL = ACCOUNT_URL + "/stats";
    public static final String RESET_URL = MY_CL_LY + "/reset";

    private RequestExecutor executor = new RequestExecutor();

    Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public AccountStatsImpl() {
        executor.setListener(new RequestExecutor.RequestResponseListener() {
            @Override
            public void OnSuccessResponse(String response) {
                if(response != null) {
                    CloudAppAccountStats stats =  gson.fromJson(response, AccountStatsModel.class);
                    DatabaseManager.updateDatabase(stats);
                } else {
                    Log.e(TAG, "Response was null, account stats not updated.");
                }
            }
            @Override
            public void OnErrorResponse(VolleyError errorResponse) {

                Log.e(TAG, "Error response from server, account stats not updated.");
            }
        });

    }

    public Request requestAccountStats() throws CloudAppException{
        return executor.executeGet(ACCOUNT_STATS_URL, 200);
    }

    public CloudAppAccountStats getAccountStats() {
        return accountStatsModel;
    }
}
