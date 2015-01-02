package deadpixel.app.vapor.callbacks;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;

/**
 * Created by Tevin on 7/22/2014.
 */
public class AccountStatsUpdateEvent extends Event {
    public CloudAppAccountStats getAccountStats() {
        return accountStats;
    }

    public void setAccountStats(CloudAppAccountStats accountStats) {
        this.accountStats = accountStats;
    }

    CloudAppAccountStats accountStats;
    public AccountStatsUpdateEvent(CloudAppAccountStats accountStats) {
        super(null);
        this.accountStats = accountStats;
    }
}
