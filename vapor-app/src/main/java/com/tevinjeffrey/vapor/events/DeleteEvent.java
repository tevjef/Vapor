package com.tevinjeffrey.vapor.events;

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

public class DeleteEvent extends DataSetUpdateEvent {
    public DeleteEvent(CloudAppItem item) {
        super(item);
    }
}
