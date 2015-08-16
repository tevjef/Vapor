package deadpixel.app.vapor.okcloudapp.model;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;

/**
 * Created by Tevin on 6/8/14.
 */
public class AccountStatsModel implements CloudAppAccountStats {

    private long items;
    private long views;

    public long getItems() {
        return items;
    }

    public void setItems(long items) {
        this.items = items;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }
}
