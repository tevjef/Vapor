package deadpixel.app.vapor.cloudapp.impl.model;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;

public class CloudAppAccountStatsImpl implements CloudAppAccountStats {

    private long items;
    private long views;

    public CloudAppAccountStatsImpl(long items, long views) {
        this.items = items;
        this.views = views;
    }

    public long getItems() {
        return items;
    }

    public long getViews() {
        return views;
    }

}
