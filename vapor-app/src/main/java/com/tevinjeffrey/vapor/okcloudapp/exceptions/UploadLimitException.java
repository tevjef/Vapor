package com.tevinjeffrey.vapor.okcloudapp.exceptions;

public class UploadLimitException extends CloudAppException {
    public UploadLimitException(String detailMessage) {
        super(detailMessage);
    }
}
