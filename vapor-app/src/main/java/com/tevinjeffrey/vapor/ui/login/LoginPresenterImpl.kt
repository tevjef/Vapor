package com.tevinjeffrey.vapor.ui.login

import com.squareup.otto.Bus
import com.tevinjeffrey.vapor.events.LoginEvent
import com.tevinjeffrey.vapor.okcloudapp.DataManager
import com.tevinjeffrey.vapor.okcloudapp.UserManager
import com.tevinjeffrey.vapor.ui.base.BasePresenter

import javax.inject.Inject

import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers

class LoginPresenterImpl : BasePresenter<LoginView>(), LoginPresenter {
    @Inject
    lateinit var userManager: UserManager
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var dataManager: DataManager

    override val isLoading: Boolean
        get() = throw UnsupportedOperationException()

    override fun tryLogin(userName: String, password: String) {
        userManager.loginWith(userName, password).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Subscriber<Boolean>() {
            override fun onCompleted() {

            }

            override fun onError(e: Throwable) {
                if (view != null) {
                    view!!.showError(e)
                    view!!.showLoading(false)
                }
            }

            override fun onNext(isLoggedIn: Boolean?) {
                if (userManager.isLoggedIn) {
                    if (view != null) {
                        bus.post(LoginEvent())
                        dataManager.syncAllItems(true)
                        view!!.loginSuccessful()
                    }
                }
            }
        })
    }
}
