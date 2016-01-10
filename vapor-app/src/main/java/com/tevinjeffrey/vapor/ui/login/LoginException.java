package com.tevinjeffrey.vapor.ui.login;

public class LoginException extends Exception {
    private int code;
    public LoginException() {
    }

    public LoginException(String detailMessage) {
        super(detailMessage);
    }

    public LoginException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
