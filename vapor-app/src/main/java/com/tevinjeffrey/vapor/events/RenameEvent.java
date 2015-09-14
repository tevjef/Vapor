package com.tevinjeffrey.vapor.events;

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

public class RenameEvent extends DataSetUpdateEvent {
    public RenameEvent(CloudAppItem item) {
        super(item);
    }
}
