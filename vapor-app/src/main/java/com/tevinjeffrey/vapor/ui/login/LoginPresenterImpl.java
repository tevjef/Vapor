package com.tevinjeffrey.vapor.ui.login;

import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.ui.base.BasePresenter;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class LoginPresenterImpl extends BasePresenter<LoginView> implements LoginPresenter {

    @Inject
    UserManager userManager;
    @Inject
    Bus bus;
    @Inject
    DataManager dataManager;

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
                                bus.post(new LoginEvent());
                                dataManager.syncAllItems(true);
                                getView().loginSuccessful();
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
