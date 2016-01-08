package com.tevinjeffrey.vapor.ui.files;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.ui.base.BasePresenter;

public class FilesActivityPresenterImpl extends BasePresenter<FilesActivityView> implements FilesActivityPresenter {

    Bus bus;
    DataManager dataManager;
    NavContext navContext = NavContext.ALL;
    String title;

    public FilesActivityPresenterImpl(DataManager dataManager, Bus bus) {
        this.dataManager = dataManager;
        this.bus = bus;
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        loadEmail();
    }

    @Override
    public void loadEmail() {
        if (getView() != null) {
            getView().setEmailInHeader(UserManager.getUserName());
        }
    }

    public NavContext getNavContext() {
        return navContext;
    }

    public void setNavContext(NavContext navContext) {
        this.navContext = navContext;
    }

}
