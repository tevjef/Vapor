package deadpixel.app.vapor.cloudapp.impl.model;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;

/**
 * Created by Tevin on 6/8/14.
 */
public class AccountStatsResponseModel implements CloudAppAccountStats {

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
