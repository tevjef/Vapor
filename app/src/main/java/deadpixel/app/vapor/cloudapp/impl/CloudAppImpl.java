package deadpixel.app.vapor.cloudapp.impl;

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

    private String TAG = "deadpixel.app.vapor.cloudapp.impl.CloudAppImpl";
    private static AccountImpl account;
    private CloudAppItemsImpl items;

    public CloudAppImpl() {
        account = new AccountImpl();
        items = new CloudAppItemsImpl();
    }


    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setDefaultSecurity(deadpixel.app.vapor.cloudapp.api.CloudAppAccount.DefaultSecurity)
     */
    public CloudAppAccount setDefaultSecurity(DefaultSecurity security)
            throws CloudAppException {
        return account.setDefaultSecurity(security);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setEmail(java.lang.String, java.lang.String)
     */
    public CloudAppAccount setEmail(String newEmail, String currentPassword)
            throws CloudAppException {
        return account.setEmail(newEmail, currentPassword);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount setPassword(java.lang.String, java.lang.String)
     */
    public CloudAppAccount setPassword(String newPassword, String currentPassword)
            throws CloudAppException {
        return account.setPassword(newPassword, currentPassword);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount resetPassword(java.lang.String)
     */
    public void resetPassword(String email) throws CloudAppException {
        account.resetPassword(email);
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
        return account.createAccount(email, password, acceptTOS);
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
        return account.setCustomDomain(domain, domainHomePage);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountDetails()
     */
    public CloudAppAccount getAccountDetails() throws CloudAppException {
        return account.getAccountDetails();
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount getAccountStats()
     */
    public CloudAppAccountStats getAccountStats() throws CloudAppException {
        return account.getAccountStats();
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem createBookmark(java.lang.String,
     *      java.lang.String)
     */
    public CloudAppItem createBookmark(String name, String url) throws CloudAppException {
        return items.createBookmark(name, url);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudApp.createBookmarks(java.lang.String[][])
     */
    public List<CloudAppItem> createBookmarks(String[][] bookmarks)
            throws CloudAppException {
        return items.createBookmarks(bookmarks);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#getItem(java.lang.String)
     */
    public CloudAppItem getItem(String url) throws CloudAppException {
        return items.getItem(url);
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
        return items.getItems(page, perPage, type, showDeleted, source);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#upload(java.io.File)
     */
    public CloudAppItem upload(File file) throws CloudAppException {
        return items.upload(file);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#upload(java.io.File, deadpixel.app.vapor.cloudapp.api.model.CloudAppProgressListener)
     */
    public CloudAppItem upload(File file, CloudAppProgressListener listener) throws CloudAppException {
        return items.upload(file, listener);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#delete(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem)
     */
    public CloudAppItem delete(CloudAppItem item) throws CloudAppException {
        return items.delete(item);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#recover(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem)
     */
    public CloudAppItem recover(CloudAppItem item) throws CloudAppException {
        return items.recover(item);
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
        return items.setSecurity(item, is_private);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.CloudAppItems#rename(deadpixel.app.vapor.cloudapp.api.model.CloudAppItem,
     *      java.lang.String)
     */
    public CloudAppItem rename(CloudAppItem item, String name) throws CloudAppException {
        return items.rename(item, name);
    }

}