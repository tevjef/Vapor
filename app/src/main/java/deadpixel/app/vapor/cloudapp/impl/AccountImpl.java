package deadpixel.app.vapor.cloudapp.impl;

import android.app.Activity;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import deadpixel.app.vapor.AuthenticationActivity;
import deadpixel.app.vapor.callbacks.AccountUpdateCallback;
import deadpixel.app.vapor.callbacks.ResponseCallback;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount.DefaultSecurity;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.impl.model.AccountResponseModel;
import deadpixel.app.vapor.cloudapp.impl.model.AccountStatsResponseModel;
import deadpixel.app.vapor.networkOp.RequestExecutors;

public class AccountImpl extends RequestExecutors {

    private final String TAG = "deadpixel.app.vapor.cloudapp.impl.AccountImpl";
    //private static final Logger LOGGER = LoggerFactory.getLogger(AccountImpl.class);

    private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

    AccountResponseModel accountResponseModel;

    Activity activity;
    protected AccountImpl(Activity activity) {
        super();
        this.activity = activity;
    }

    private AccountUpdateCallback aCallback;

    Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();


    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    String response;

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setDefaultSecurity(deadpixel.app.vapor.cloudapp.api.CloudAppAccount.DefaultSecurity)
     */
    public void setDefaultSecurity(DefaultSecurity security)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("private_items", (security == DefaultSecurity.PRIVATE));
            json.put("user", user);

            executePut(ACCOUNT_URL, json.toString(), 200);

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
    public void setEmail(String newEmail, String currentPassword)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", newEmail);
            user.put("current_password", currentPassword);
            json.put("user", user);

            executePut(ACCOUNT_URL, json.toString(), 200);

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
    public void setPassword(String newPassword, String currentPassword)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("password", newPassword);
            user.put("current_password", currentPassword);
            json.put("user", user);

            executePut(ACCOUNT_URL, json.toString() , 200);

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

            executePost(RESET_URL, json.toString(), 200);

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
    public void createAccount(String email, String password, boolean acceptTOS)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("email", email);
            user.put("password", password);
            user.put("accept_tos", acceptTOS);
            json.put("user", user);

            executePost(REGISTER_URL, json.toString() , 201);

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
    public void setCustomDomain(String domain, String domainHomePage)
            throws CloudAppException {
        try {
            JSONObject json = new JSONObject();
            JSONObject user = new JSONObject();
            user.put("domain", domain);
            user.put("domain_home_page", domainHomePage);
            json.put("user", user);

            executePut(ACCOUNT_URL, json.toString() , 200);

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
    public CloudAppAccount getAccountDetails()  {
        return accountResponseModel;
    }

    public void requestAccountDetails() throws CloudAppException {
        executeGet(ACCOUNT_URL, 200);
    }

    /**
     *
     * {@inheritDoc
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountStats()
     */

    public void updateAccountDetails(String response) {

        this.response = response;
        accountResponseModel = gson.fromJson(response, AccountResponseModel.class);
    }
}