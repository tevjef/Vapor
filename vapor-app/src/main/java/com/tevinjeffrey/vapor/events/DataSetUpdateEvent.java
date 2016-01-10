package com.tevinjeffrey.vapor.events;

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

class DataSetUpdateEvent {
    final CloudAppItem item;

    public DataSetUpdateEvent(CloudAppItem item) {
        this.item = item;
    }

    public CloudAppItem getItem() {
        return item;
    }

}