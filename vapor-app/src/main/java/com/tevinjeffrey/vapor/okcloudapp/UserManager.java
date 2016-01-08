package com.tevinjeffrey.vapor.okcloudapp;

import android.os.Handler;
import android.os.Looper;

import com.orhanobut.hawk.Hawk;
import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.events.LogoutEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.AccountModel;
import com.tevinjeffrey.vapor.ui.login.LoginException;
import com.tevinjeffrey.vapor.utils.RxUtils;

import org.apache.http.auth.UsernamePasswordCredentials;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit.HttpException;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class UserManager {
    private static final String IS_LOGGED_IN = "IS_LOGGED_IN";
    private static final String EMAIL_ADDRESS = "EMAIL";
    private static final String PASSWORD = "PASS";

    private final DigestAuthenticator digestAuthenticator;
    private final CloudAppService cloudAppService;
    private final Bus bus;

    public UserManager(CloudAppService cloudAppService, DigestAuthenticator digestAuthenticator, Bus bus) {
        this.digestAuthenticator = digestAuthenticator;
        this.cloudAppService = cloudAppService;
        this.bus = bus;
        bus.register(this);
    }
    
    public boolean isLoggedIn() {
        return Hawk.get(IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean bool) {
        Hawk.put(IS_LOGGED_IN, bool);
    }

    public Observable<Boolean> loginWith(final String userName, final String password) {
        digestAuthenticator.setCredentials(new UsernamePasswordCredentials(userName, password));
        
        return cloudAppService.getAccount()
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends AccountModel>>() {
                    @Override
                    public Observable<? extends AccountModel> call(Throwable throwable) {
                        if (throwable instanceof HttpException) {
                            HttpException e = (HttpException) throwable;
                            LoginException exception = new LoginException();
                            if (e.code() > 500) {
                                exception = new LoginException("Service Unavailable – We’re temporarily offline for maintenance. Please try again later.", e);
                                exception.setCode(e.code());
                            } else if (e.code() > 400) {
                                exception = new LoginException("Digest authentication failed");
                                exception.setCode(e.code());
                            }
                            return Observable.error(exception);
                        } else {
                            return Observable.error(throwable);
                        }
                    }
                })
                .delaySubscription(1000, TimeUnit.MILLISECONDS)
                .map(new Func1<AccountModel, Boolean>() {
                    @Override
                    public Boolean call(AccountModel accountModel) {
                        if (accountModel != null && Objects.equals(accountModel.getEmail(), userName)) {
                            putPassword(password);
                            putUserName(userName);
                            setLoggedIn(true);
                            return true;
                        }
                        setLoggedIn(false);
                        return false;
                    }
                })
                .subscribeOn(Schedulers.io());
                
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

    public void logout() {
        Hawk.clear();
        bus.post(new LogoutEvent());
    }
}
