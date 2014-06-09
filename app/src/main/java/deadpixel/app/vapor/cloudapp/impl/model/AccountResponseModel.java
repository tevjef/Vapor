package deadpixel.app.vapor.cloudapp.impl.model;

import java.util.Date;
import java.text.ParseException;

import deadpixel.app.vapor.cloudapp.api.CloudApp;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;

/**
 * Created by Tevin on 6/7/14.
 */
public class AccountResponseModel extends CloudAppModel implements CloudAppAccount {

    private long id;
    private String email;
    private String domain;
    private String domain_home_page;
    private boolean private_items;
    private boolean subscribed;
    private String subscription_expires_at;
    private boolean alpha;
    private String created_at;
    private String updated_at;
    private String activated_at;
    private Socket socket;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getDomainHomePage() {
        return domain_home_page;
    }
    public void setDomainHomePage(String domain_home_page) {
        this.domain_home_page = domain_home_page;
    }
    public boolean isPrivateItems() {
        return private_items;
    }

    public DefaultSecurity getDefaultSecurity() {
        return isPrivateItems()?DefaultSecurity.PRIVATE:DefaultSecurity.PUBLIC;
    }
    public void setPrivateItems(boolean private_items) {
        this.private_items = private_items;
    }
    public boolean isSubscribed() {
        return subscribed;
    }
    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
    public Date getSubscriptionExpiresAt() throws CloudAppException {
        try {
            return formatBis.parse(subscription_expires_at);
        } catch (ParseException e) {
            throw new CloudAppException(500, "Error parsing Subscription Expire Date", e);
        }
    }
    public void setSubscriptionExpiresAt(String subscription_expires_at) {
        this.subscription_expires_at = subscription_expires_at;
    }
    public boolean isAlpha() {
        return alpha;
    }
    public void setAlpha(boolean alpha) {
        this.alpha = alpha;
    }
    public Date getCreatedAt() throws CloudAppException {
        try {
            return format.parse(created_at);
        } catch (ParseException e) {
            throw new CloudAppException(500, "Error parsing account created_at date.", e);
        }
    }
    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }
    public Date getUpdatedAt() throws CloudAppException {
        try {
            return format.parse(updated_at);
        } catch (ParseException e) {
            throw new CloudAppException(500, "Error parsing account updated_at date", e);
        }
    }
    public void setUpdatedAt(String updated_at) {
        this.updated_at = updated_at;
    }
    public Date getActivatedAt() throws CloudAppException {
        try {
            return format.parse(activated_at);
        } catch (ParseException e) {
            throw new CloudAppException(500, "Error parsing acocunt activated_at date", e);
        }
    }
    public void setActivatedAt(String activated_at) {
        this.activated_at = activated_at;
    }
    public Socket getSocket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public class Socket {

        private String auth_url;
        private String api_key;
        private long app_id;
        private Channels channels;

        public String getAuthUrl() {
            return auth_url;
        }

        public void setAuthUrl(String auth_url) {
            this.auth_url = auth_url;
        }

        public String getApiKey() {
            return api_key;
        }

        public void setApiKey(String api_key) {
            this.api_key = api_key;
        }

        public long getAppId() {
            return app_id;
        }

        public void setAppId(long app_id) {
            this.app_id = app_id;
        }


        public Channels getChannels() {
            return channels;
        }

        public void setChannels(Channels channels) {
            this.channels = channels;
        }
    }

    public class Channels {

        private String items;

        public String getItems() {
            return items;
        }

        public void setItems(String items) {
            this.items = items;
        }
    }
}
