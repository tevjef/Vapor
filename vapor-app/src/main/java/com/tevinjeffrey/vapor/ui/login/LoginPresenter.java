package com.tevinjeffrey.vapor.ui.login;

import com.tevinjeffrey.vapor.ui.base.Presenter;
import com.tevinjeffrey.vapor.ui.base.StatefulPresenter;
import com.tevinjeffrey.vapor.ui.base.View;

public interface LoginPresenter<V extends View> extends Presenter<V>, StatefulPresenter {
    //Coupled showing the loading animation with loading the View's data.
    void tryLogin(String userName, String password);
    //I little utility method to determine if the Presenter is doing any work.
    boolean isLoading();
}
