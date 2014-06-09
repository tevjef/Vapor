package deadpixel.app.vapor.cloudapp.impl;

import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount.DefaultSecurity;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.impl.model.AccountResponseModel;
import deadpixel.app.vapor.cloudapp.impl.model.AccountStatsResponseModel;
import deadpixel.app.vapor.cloudapp.impl.model.CloudAppAccountImpl;
import deadpixel.app.vapor.cloudapp.impl.model.CloudAppAccountStatsImpl;
import deadpixel.app.vapor.networkOp.RequestExecuters;

public class AccountImpl extends CloudAppBase implements RequestExecuters.ResponseCallbacks {

    public static final String REGISTER_URL = MY_CL_LY + "/register";
    public static final String ACCOUNT_URL = MY_CL_LY + "/account";
    public static final String ACCOUNT_STATS_URL = ACCOUNT_URL + "/stats";
    public static final String RESET_URL = MY_CL_LY + "/reset";

    private final String TAG = "deadpixel.app.vapor.cloudapp.impl.AccountImpl";
    //private static final Logger LOGGER = LoggerFactory.getLogger(AccountImpl.class);

    protected AccountImpl() {
        super();

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
    public CloudAppAccount setDefaultSecurity(DefaultSecurity security)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("private_items", (security == DefaultSecurity.PRIVATE));
            json.put("user", user);

            executePut(ACCOUNT_URL, json, 200);
            return gson.fromJson(response, AccountResponseModel.class);

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
    public CloudAppAccount setEmail(String newEmail, String currentPassword)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", newEmail);
            user.put("current_password", currentPassword);
            json.put("user", user);

            executePut(ACCOUNT_URL, json, 200);
            return gson.fromJson(response, AccountResponseModel.class);

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
    public CloudAppAccount setPassword(String newPassword, String currentPassword)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("password", newPassword);
            user.put("current_password", currentPassword);
            json.put("user", user);

            executePut(ACCOUNT_URL, json, 200);
            return gson.fromJson(response, AccountResponseModel.class);

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
    public void resetPassword(String email) throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", email);
            json.put("user", user);

            executePost(RESET_URL, json, 200);

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
    public CloudAppAccount createAccount(String email, String password, boolean acceptTOS)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", email);
            user.put("password", password);
            user.put("accept_tos", acceptTOS);
            json.put("user", user);

            executePost(REGISTER_URL, json, 201);

            return gson.fromJson(response, AccountResponseModel.class);
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
    public CloudAppAccount setCustomDomain(String domain, String domainHomePage)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("domain", domain);
            user.put("domain_home_page", domainHomePage);
            json.put("user", user);

            executePut(ACCOUNT_URL, json, 200);
            return gson.fromJson(response, AccountResponseModel.class);

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
    public CloudAppAccount getAccountDetails() throws CloudAppException {
        executeGet(ACCOUNT_URL);
        return gson.fromJson(response, AccountResponseModel.class);
    }

    /**
     *
     * {@inheritDoc
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountStats()
     */
    public CloudAppAccountStats getAccountStats() throws CloudAppException {
            executeGet(ACCOUNT_STATS_URL);
            return gson.fromJson(response, AccountStatsResponseModel.class);
    }

    @Override
    public void serverResponse(String response) {
        this.response = response;
    }

    @Override
    public void serverErrorResponse(VolleyError error) {

    }
}