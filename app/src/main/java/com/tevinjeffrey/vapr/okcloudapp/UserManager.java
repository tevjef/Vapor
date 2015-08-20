package com.tevinjeffrey.vapr.okcloudapp;

import com.orhanobut.hawk.Hawk;
import com.tevinjeffrey.vapr.okcloudapp.model.AccountModel;
import com.tevinjeffrey.vapr.utils.RxUtils;

import org.apache.http.auth.UsernamePasswordCredentials;

import java.util.Objects;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class UserManager {
    private static final String IS_LOGGED_IN = "IS_LOGGED_IN";
    private static final String EMAIL_ADDRESS = "EMAIL";
    private static final String PASSWORD = "PASS";

    private final DigestAuthenticator digestAuthenticator;
    private final CloudAppService cloudAppService;

    public UserManager(CloudAppService cloudAppService, DigestAuthenticator digestAuthenticator) {
        this.digestAuthenticator = digestAuthenticator;
        this.cloudAppService = cloudAppService;
    }
    
    public boolean isLoggedIn() {
        return Hawk.get(IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean bool) {
        Hawk.put(IS_LOGGED_IN, bool);
    }

    public Observable<Boolean> loginWith(String userName, String password) {
        putPassword(password);
        putUserName(userName);

        digestAuthenticator.setCredentials(new UsernamePasswordCredentials(getUserName(), getPassword()));
        
        return cloudAppService.getAccount()
                .retryWhen(new RxUtils.RetryWithDelay(2, 2000))
                .observeOn(Schedulers.io())
                .map(new Func1<AccountModel, Boolean>() {
                    @Override
                    public Boolean call(AccountModel accountModel) {
                        if (accountModel != null && Objects.equals(accountModel.getEmail(), getUserName())) {
                            setLoggedIn(true);
                            return true;
                        }
                        setLoggedIn(false);
                        return false;
                    }
                });
                
    }
    
    private void putUserName(String userName) {
        Hawk.put(EMAIL_ADDRESS, userName);
    }
    
    private void putPassword(String password) {
        Hawk.put(PASSWORD, password);
    }

    public static String getUserName() {
        return Hawk.get(EMAIL_ADDRESS);
    }

    public static String getPassword() {
        return Hawk.get(PASSWORD);
    }
}
