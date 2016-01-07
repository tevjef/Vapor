package com.tevinjeffrey.vapor.ui.login;

import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.ui.base.BasePresenter;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class LoginPresenterImpl extends BasePresenter<LoginView> implements LoginPresenter {

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
