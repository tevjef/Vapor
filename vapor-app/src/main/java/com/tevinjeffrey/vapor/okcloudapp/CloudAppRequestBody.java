package com.tevinjeffrey.vapor.okcloudapp;

import com.squareup.okhttp.RequestBody;

public abstract class CloudAppRequestBody extends RequestBody {
    public abstract String getFileName();
    public abstract long contentLength();
    public String getFileSize() {
        return String.valueOf(contentLength());
    }
}
