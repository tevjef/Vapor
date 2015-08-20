package com.tevinjeffrey.vapr.ui.login;

import com.tevinjeffrey.vapr.okcloudapp.UserManager;
import com.tevinjeffrey.vapr.ui.base.BasePresenter;
import com.tevinjeffrey.vapr.ui.base.View;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class LoginPresenterImpl<V extends LoginView> extends BasePresenter<V> implements LoginPresenter<V> {

    @Inject
    UserManager userManager;

    @Override
    public void tryLogin(String userName, String password) {
        userManager.loginWith(userName, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (getView() != null) {
                            getView().showError(e);
                            getView().showLoading(false);
                        }
                    }

                    @Override
                    public void onNext(Boolean isLoggedIn) {
                        if (userManager.isLoggedIn()) {
                            if (getView() != null) {
                                getView().loginSuccessful(true);
                            }
                        }
                    }
                });
    }

    @Override
    public boolean isLoading() {
        return false;
    }
}
