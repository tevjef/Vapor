package deadpixel.app.vapor.cloudapp.impl;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount.DefaultSecurity;
import deadpixel.app.vapor.cloudapp.impl.model.AccountModel;
import deadpixel.app.vapor.database.DatabaseManager;
import deadpixel.app.vapor.database.PreferenceHandler;
import deadpixel.app.vapor.networkOp.RequestExecutor;
import deadpixel.app.vapor.utils.AppUtils;

public class AccountImpl {

    private final String TAG = "AccountImpl";

    private Context mContext;
    //private static final Logger LOGGER = LoggerFactory.getLogger(AccountImpl.class);

    public static final String MY_CL_LY = "http://my.cl.ly";
    public static final String REGISTER_URL = MY_CL_LY+ "/register";
    public static final String ACCOUNT_URL = MY_CL_LY + "/account";
    public static final String ACCOUNT_STATS_URL = ACCOUNT_URL + "/stats";
    public static final String RESET_URL = MY_CL_LY + "/reset";

    private RequestExecutor executor = new RequestExecutor();

    protected AccountImpl(Context context) {

        mContext = context;

        executor.setListener(new RequestExecutor.RequestResponseListener() {
            @Override
            public void OnSuccessResponse(String response) {


                try {
                    AppUtils.addToRequestQueue(AppUtils.api.requestAccountStats());
                } catch (CloudAppException e) {
                    e.printStackTrace();
                }


                CloudAppAccount account =  gson.fromJson(response, AccountModel.class);
                DatabaseManager.updateDatabase(account);

            }
            @Override
            public void OnErrorResponse(VolleyError errorResponse) {
                Log.e(TAG, "Error response from server, account not updated.");
            }
        });
    }



    Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    String response;

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setDefaultSecurity(deadpixel.app.vapor.cloudapp.api.CloudAppAccount.DefaultSecurity)
     */
    public Request setDefaultSecurity(DefaultSecurity security)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("private_items", (security == DefaultSecurity.PRIVATE));
            json.put("user", user);

            return executor.executePut(ACCOUNT_URL, json.toString(), 200);

        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setEmail(java.lang.String, java.lang.String)
     */
    public Request setEmail(String newEmail, String currentPassword)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", newEmail);
            user.put("current_password", currentPassword);
            json.put("user", user);

           return executor.executePut(ACCOUNT_URL, json.toString(), 200);

        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setPassword(java.lang.String, java.lang.String)
     */
    public Request setPassword(String newPassword, String currentPassword)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("password", newPassword);
            user.put("current_password", currentPassword);
            json.put("user", user);

            return executor.executePut(ACCOUNT_URL, json.toString(), 200);

        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount resetPassword(java.lang.String)
     */
    public Request resetPassword(String email) throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", email);
            json.put("user", user);

            return executor.executePost(RESET_URL, json.toString(), 200);

        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount createAccount(java.lang.String,
     *      java.lang.String, boolean)
     */
    public Request createAccount(String email, String password, boolean acceptTOS)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", email);
            user.put("password", password);
            user.put("accept_tos", acceptTOS);
            json.put("user", user);

            return executor.executePost(REGISTER_URL, json.toString(), 201);

        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setCustomDomain(java.lang.String,
     *      java.lang.String)
     */
    public Request setCustomDomain(String domain, String domainHomePage)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("domain", domain);
            user.put("domain_home_page", domainHomePage);
            json.put("user", user);

            return executor.executePut(ACCOUNT_URL, json.toString(), 200);

        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountDetails()
     */


    //Puts in a request for for all
    public Request requestAccountDetails() throws CloudAppException {
        return executor.executeGet(ACCOUNT_URL, 200);
    }

    /**
     *
     * {@inheritDoc
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountStats()
     */

    public CloudAppAccount getAccountDetails() {
        return PreferenceHandler.getAccountDetails();
    }
}