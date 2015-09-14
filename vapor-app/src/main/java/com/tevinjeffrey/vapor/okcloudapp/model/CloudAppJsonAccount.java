package com.tevinjeffrey.vapor.okcloudapp.model;

public class CloudAppJsonAccount {
    User user;

    class User {
        String email;
        String password;
        String currentPassword;
        String domain;
        String domainHomePage;
        boolean acceptTos;
        boolean privateItems;
    }
}
