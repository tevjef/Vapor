package com.tevinjeffrey.vapor.events;

import android.net.Uri;

public class UploadFailedEvent {
    private final Uri uri;

    public UploadFailedEvent(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }
}
