package deadpixel.app.vapor.cloudapp.impl;

import android.content.Context;

import com.android.volley.Request;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;

import deadpixel.app.vapor.cloudapp.api.CloudApp;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount.DefaultSecurity;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.cloudapp.impl.model.CloudAppUpload;

public class CloudAppImpl implements CloudApp {

    //private static final Logger LOGGER = LoggerFactory.getLogger(CloudAppImpl.class);

    public static final String MY_CL_LY = "http://my.cl.ly";
    private String TAG = "deadpixel.app.vapor.cloudapp.impl.CloudAppImpl";
    private static AccountImpl mAccount;
    private static CloudAppItemsImpl mItems;
    private static AccountStatsImpl mAccountStats;


    public CloudAppImpl(Context context) {
        mAccount = new AccountImpl(context);
        mItems = new CloudAppItemsImpl();
        mAccountStats = new AccountStatsImpl();
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setDefaultSecurity(deadpixel.app.vapor.cloudapp.api.CloudAppAccount.DefaultSecurity)
     */
    public Request setDefaultSecurity(DefaultSecurity security)
            throws CloudAppException {
       return  mAccount.setDefaultSecurity(security);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setEmail(java.lang.String, java.lang.String)
     */
    public Request setEmail(String newEmail, String currentPassword)
            throws CloudAppException {
        return mAccount.setEmail(newEmail, currentPassword);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setPassword(java.lang.String, java.lang.String)
     */
    public Request setPassword(String newPassword, String currentPassword)
            throws CloudAppException {
        return mAccount.setPassword(newPassword, currentPassword);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount resetPassword(java.lang.String)
     */
    public Request resetPassword(String email) throws CloudAppException {
        return mAccount.resetPassword(email);
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
        return mAccount.createAccount(email, password, acceptTOS);
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
        return mAccount.setCustomDomain(domain, domainHomePage);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountDetails()
     */
    public CloudAppAccount getAccountDetails() throws CloudAppException {
        return mAccount.getAccountDetails();
    }

    public Request requestAccountDetails() {
        return mAccount.requestAccountDetails();
    }

    @Override
    public Request requestAccountStats() {
        return mAccountStats.requestAccountStats();
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountStats()
     */
    public CloudAppAccountStats getAccountStats() {
        return mAccountStats.getAccountStats();
    }


    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem createBookmark(java.lang.String,
     *      java.lang.String)
     */
    public Request createBookmark(String name, String url) throws CloudAppException {
        return mItems.createBookmark(name, url);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudApp.createBookmarks(java.lang.String[][])
     */
    public Request createBookmarks(String[][] bookmarks)
            throws CloudAppException {
        return mItems.createBookmarks(bookmarks);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#getItem(java.lang.String)
     */
    public Request getItem(String url) throws CloudAppException {
        return mItems.getItem(url);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#getItems(int, int,
     *      deadpixel.app.vapor.cloudapp.api.CloudAppItems.Type, boolean, java.lang.String)
     */
    public Request getItems(int page, int perPage, CloudAppItem.Type type,
                                        String source) throws CloudAppException {
        return mItems.getItems(page, perPage, type, source);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#upload(java.io.File)
     * @param fileUpload
     */
    public void upload(CloudAppUpload fileUpload) throws CloudAppException {
        mItems.upload(fileUpload);
    }


    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#delete(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem)
     */
    public Request delete(CloudAppItem item) throws CloudAppException {
        return mItems.delete(item);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#recover(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem)
     */
    public Request recover(CloudAppItem item) throws CloudAppException {
        return mItems.recover(item);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#setSecurity(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem,
     *      boolean)
     */
    public Request setSecurity(CloudAppItem item, boolean is_private)
            throws CloudAppException {
        return mItems.setSecurity(item, is_private);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#rename(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem,
     *      java.lang.String)
     */
    public Request rename(CloudAppItem item, String name) throws CloudAppException {
        return mItems.rename(item, name);
    }

}