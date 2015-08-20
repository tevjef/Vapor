package com.tevinjeffrey.vapr.ui.login;

import com.tevinjeffrey.vapr.ui.base.View;

import java.util.List;

public interface LoginView extends View {

    void showLoading(boolean pullToRefresh);

    void loginSuccessful(boolean isSuccessful);

    void showError(Throwable e);
}
