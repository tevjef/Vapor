package deadpixel.app.vapor.cloudapp.api.model;

import java.text.ParseException;

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
    public String getCreatedAt() throws CloudAppException, ParseException;

    /**
     * @return The date you last updated your account.
     */
    public String getUpdatedAt() throws CloudAppException, ParseException;

    /**
     * @return The date you activated your account.
     */
    public String getActivatedAt() throws CloudAppException, ParseException;

    /**
     * @return The date you subscription expires if any
     */
    public String getSubscriptionExpiresAt() throws CloudAppException, ParseException;

    public Socket getSocket();

    public interface Socket {

        public String getAuthUrl();

        public String getApiKey();

        public long getAppId();

        public Channels getChannels();

    }

    public interface Channels {
        public String getItems();
    }

}
