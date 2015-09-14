package com.tevinjeffrey.vapor.ui.login;

import com.tevinjeffrey.vapor.ui.base.View;

public interface LoginView extends View {

    void showLoading(boolean pullToRefresh);

    void loginSuccessful(boolean isSuccessful);

    void showError(Throwable e);
}
