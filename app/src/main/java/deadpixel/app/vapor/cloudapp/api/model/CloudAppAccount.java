package deadpixel.app.vapor.cloudapp.api.model;

import java.text.ParseException;
import java.util.Date;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;

public interface CloudAppAccount {

    enum DefaultSecurity {
        PRIVATE, PUBLIC
    }

    /**
     * @return Your ID.
     */
    public long getId() throws CloudAppException;

    /**
     * @return Your email address.
     */
    public String getEmail() throws CloudAppException;

    /**
     * @return Your domain.
     */
    public String getDomain() throws CloudAppException;

    /**
     * @return Your domain homepage.
     */
    public String getDomainHomePage() throws CloudAppException;

    /**
     * @return Your default security for new files.
     */
    public DefaultSecurity getDefaultSecurity() throws CloudAppException;

    /**
     * @return
     */
    public boolean isSubscribed() throws CloudAppException;

    public boolean isAlpha() throws CloudAppException;

    /**
     * @return the date you signed up
     */
    public Date getCreatedAt() throws CloudAppException, ParseException;

    /**
     * @return The date you last updated your account.
     */
    public Date getUpdatedAt() throws CloudAppException, ParseException;

    /**
     * @return The date you activated your account.
     */
    public Date getActivatedAt() throws CloudAppException, ParseException;

    /**
     * @return The date you subscription expires if any
     */
    public Date getSubscriptionExpiresAt() throws CloudAppException, ParseException;

}
