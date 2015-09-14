package com.tevinjeffrey.vapor.okcloudapp.exceptions;

public class FileToLargeException extends CloudAppException {
    public FileToLargeException(String detailMessage) {
        super(detailMessage);
    }
}
