package com.tevinjeffrey.vapor.events;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

public class UploadEvent {
    Uri uri;
    CloudAppItem item;

    public UploadEvent(Uri uri, CloudAppItem item) {
        this.uri = uri;
        this.item = item;
    }

    public UploadEvent() {
    }

    @Nullable
    public Uri getUri() {
        return uri;
    }

    public CloudAppItem getItem() {
        return item;
    }
}
