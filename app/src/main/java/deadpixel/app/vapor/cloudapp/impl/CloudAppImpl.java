package deadpixel.app.vapor.cloudapp.impl;

import android.app.Activity;

import java.io.File;
import java.util.List;

import deadpixel.app.vapor.cloudapp.api.CloudApp;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount.DefaultSecurity;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppProgressListener;
import deadpixel.app.vapor.networkOp.RequestHandler;

public class CloudAppImpl extends RequestHandler implements CloudApp {

    //private static final Logger LOGGER = LoggerFactory.getLogger(CloudAppImpl.class);

    public static final String MY_CL_LY = "http://my.cl.ly";
    private String TAG = "deadpixel.app.vapor.cloudapp.impl.CloudAppImpl";
    private static AccountImpl mAccount;
    private static CloudAppItemsImpl mItems;
    private static AccountStatsImpl mAccountStats;


    public CloudAppImpl(Activity activity) {
        mAccount = new AccountImpl(activity);
        mItems = new CloudAppItemsImpl();
        mAccountStats = new AccountStatsImpl(activity);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setDefaultSecurity(deadpixel.app.vapor.cloudapp.api.CloudAppAccount.DefaultSecurity)
     */
    public void setDefaultSecurity(DefaultSecurity security)
            throws CloudAppException {
        mAccount.setDefaultSecurity(security);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setEmail(java.lang.String, java.lang.String)
     */
    public void setEmail(String newEmail, String currentPassword)
            throws CloudAppException {
        mAccount.setEmail(newEmail, currentPassword);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setPassword(java.lang.String, java.lang.String)
     */
    public void setPassword(String newPassword, String currentPassword)
            throws CloudAppException {
        mAccount.setPassword(newPassword, currentPassword);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount resetPassword(java.lang.String)
     */
    public void resetPassword(String email) throws CloudAppException {
        mAccount.resetPassword(email);
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
        mAccount.createAccount(email, password, acceptTOS);
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
        mAccount.setCustomDomain(domain, domainHomePage);
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

    public void requestAccountDetails() throws CloudAppException {
        mAccount.requestAccountDetails();
    }

    @Override
    public void requestAccountStats() throws CloudAppException {
        mAccountStats.requestAccountStats();
    }

    @Override
    public void updateAccountDetails(String response)  {
        mAccount.updateAccountDetails(response);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountStats()
     */
    public CloudAppAccountStats getAccountStats() throws CloudAppException {
        return mAccountStats.getAccountStats();
    }


    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem createBookmark(java.lang.String,
     *      java.lang.String)
     */
    public CloudAppItem createBookmark(String name, String url) throws CloudAppException {
        return mItems.createBookmark(name, url);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudApp.createBookmarks(java.lang.String[][])
     */
    public List<CloudAppItem> createBookmarks(String[][] bookmarks)
            throws CloudAppException {
        return mItems.createBookmarks(bookmarks);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#getItem(java.lang.String)
     */
    public CloudAppItem getItem(String url) throws CloudAppException {
        return mItems.getItem(url);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#getItems(int, int,
     *      deadpixel.app.vapor.cloudapp.api.CloudAppItems.Type, boolean, java.lang.String)
     */
    public List<CloudAppItem> getItems(int page, int perPage, CloudAppItem.Type type,
                                       boolean showDeleted, String source) throws CloudAppException {
        return mItems.getItems(page, perPage, type, showDeleted, source);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#upload(java.io.File)
     */
    public CloudAppItem upload(File file) throws CloudAppException {
        return mItems.upload(file);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#upload(java.io.File, deadpixel.app.vapor.cloudapp.api.model.CloudAppProgressListener)
     */
    public CloudAppItem upload(File file, CloudAppProgressListener listener) throws CloudAppException {
        return mItems.upload(file, listener);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#delete(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem)
     */
    public CloudAppItem delete(CloudAppItem item) throws CloudAppException {
        return mItems.delete(item);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#recover(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem)
     */
    public CloudAppItem recover(CloudAppItem item) throws CloudAppException {
        return mItems.recover(item);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#setSecurity(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem,
     *      boolean)
     */
    public CloudAppItem setSecurity(CloudAppItem item, boolean is_private)
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
    public CloudAppItem rename(CloudAppItem item, String name) throws CloudAppException {
        return mItems.rename(item, name);
    }

}