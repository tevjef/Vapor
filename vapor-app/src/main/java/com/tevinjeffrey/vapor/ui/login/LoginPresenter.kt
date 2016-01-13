package com.tevinjeffrey.vapor.ui.login

import com.tevinjeffrey.vapor.ui.base.Presenter
import com.tevinjeffrey.vapor.ui.base.StatefulPresenter

interface LoginPresenter : Presenter<LoginView>, StatefulPresenter {
    //Coupled showing the loading animation with loading the View's data.
    fun tryLogin(userName: String, password: String)

    //I little utility method to determine if the Presenter is doing any work.
    val isLoading: Boolean
}
