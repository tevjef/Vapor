package deadpixel.app.vapor.callbacks;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;

/**
 * Created by Tevin on 6/16/2014.
 */
public class AccountResponseEvent extends Event {

    public CloudAppAccount getAccount() {
        return account;
    }

    public void setAccount(CloudAppAccount account) {
        this.account = account;
    }

    CloudAppAccount account;
    public AccountResponseEvent(CloudAppAccount account) {
        super(null);
        this.account = account;
    }
}