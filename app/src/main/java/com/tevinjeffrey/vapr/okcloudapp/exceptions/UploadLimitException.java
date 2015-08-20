package com.tevinjeffrey.vapr.okcloudapp.exceptions;

public class UploadLimitException extends CloudAppException {
    public UploadLimitException(String detailMessage) {
        super(detailMessage);
    }
}
