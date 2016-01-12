package com.tevinjeffrey.vapor.ui.files;

import android.os.Bundle;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.LoginEvent;
import com.tevinjeffrey.vapor.events.RefreshEvent;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.ui.base.BasePresenter;

import java.util.concurrent.TimeUnit;

import jonathanfinerty.once.Once;

public class FilesActivityPresenterImpl extends BasePresenter<FilesActivityView> implements FilesActivityPresenter {

    private final Bus bus;
    private final DataManager dataManager;
    NavContext navContext = NavContext.ALL;

    public FilesActivityPresenterImpl(DataManager dataManager, Bus bus) {
        this.dataManager = dataManager;
        this.bus = bus;
    }

    @Override
    public void loadEmail() {
        if (getView() != null) {
            getView().setEmailInHeader(UserManager.Companion.getUserName());
        }
    }

    public NavContext getNavContext() {
        return navContext;
    }

    @Override
    public void refreshClicked() {
        bus.post(new RefreshEvent());
    }

    public void setNavContext(NavContext navContext) {
        this.navContext = navContext;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(Once.beenDone(TimeUnit.HOURS, 6, DataManager.SYNC_ALL_ITEMS)) {
            dataManager.syncAllItems(false);
        }
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

}
