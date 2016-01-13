package com.tevinjeffrey.vapor.ui.login

import com.tevinjeffrey.vapor.ui.base.View

interface LoginView : View {

    fun showLoading(pullToRefresh: Boolean)

    fun loginSuccessful()

    fun showError(e: Throwable)
}
