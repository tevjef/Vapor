package deadpixel.app.vapor.cloudapp.impl;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount.DefaultSecurity;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.impl.model.CloudAppAccountImpl;
import deadpixel.app.vapor.cloudapp.impl.model.CloudAppAccountStatsImpl;

public class AccountImpl extends CloudAppBase {

    public static final String REGISTER_URL = MY_CL_LY + "/register";
    public static final String ACCOUNT_URL = MY_CL_LY + "/account";
    public static final String ACCOUNT_STATS_URL = ACCOUNT_URL + "/stats";
    public static final String RESET_URL = MY_CL_LY + "/reset";

    private final String TAG = "deadpixel.app.vapor.cloudapp.impl.AccountImpl";
    //private static final Logger LOGGER = LoggerFactory.getLogger(AccountImpl.class);

    protected AccountImpl() {
        super();

    }

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

            json = (JSONObject) executePut(ACCOUNT_URL, json, 200);
            return new CloudAppAccountImpl(json);

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

            json = (JSONObject) executePut(ACCOUNT_URL, json, 200);
            return new CloudAppAccountImpl(json);

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

            json = (JSONObject) executePut(ACCOUNT_URL, json, 200);
            return new CloudAppAccountImpl(json);

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
            json = (JSONObject) executePost(REGISTER_URL, json, 201);
            return new CloudAppAccountImpl(json);
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

            json = (JSONObject) executePut(ACCOUNT_URL, json, 200);
            return new CloudAppAccountImpl(json);

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
        JSONObject json = (JSONObject) executeGet(ACCOUNT_URL);
        return new CloudAppAccountImpl(json);
    }

    /**
     *
     * {@inheritDoc
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountStats()
     */
    public CloudAppAccountStats getAccountStats() throws CloudAppException {
        try {
            JSONObject json = (JSONObject) executeGet(ACCOUNT_STATS_URL);
            return new CloudAppAccountStatsImpl(json.getLong("items"), json.getLong("views"));
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }
}