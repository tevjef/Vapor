package com.tevinjeffrey.vapor.okcloudapp

import com.orhanobut.hawk.Hawk
import com.squareup.otto.Bus
import com.tevinjeffrey.vapor.events.LogoutEvent
import com.tevinjeffrey.vapor.okcloudapp.model.AccountModel
import com.tevinjeffrey.vapor.ui.login.LoginException

import org.apache.http.auth.UsernamePasswordCredentials

import java.util.concurrent.TimeUnit

import retrofit.HttpException
import rx.Observable
import rx.functions.Func1
import rx.schedulers.Schedulers

class UserManager(private val cloudAppService: CloudAppService, private val digestAuthenticator: DigestAuthenticator, private val bus: Bus) {

    init {
        bus.register(this)
    }

    var isLoggedIn: Boolean
        get() = Hawk.get(IS_LOGGED_IN, false)
        private set(bool) {
            Hawk.put(IS_LOGGED_IN, bool)
        }

    fun loginWith(userName: String, password: String): Observable<Boolean> {
        digestAuthenticator.setCredentials(UsernamePasswordCredentials(userName, password))

        return cloudAppService.getAccount().onErrorResumeNext { throwable ->
            if (throwable is HttpException) {
                var exception = LoginException()
                if (throwable.code() > 500) {
                    exception = LoginException("Service Unavailable – We’re temporarily offline for maintenance. Please try again later.", throwable)
                    exception.code = throwable.code()
                } else if (throwable.code() > 400) {
                    exception = LoginException("Digest authentication failed")
                    exception.code = throwable.code()
                }
                Observable.error<AccountModel>(exception)
            } else {
                Observable.error<AccountModel>(throwable)
            }
        }.delaySubscription(1000, TimeUnit.MILLISECONDS)
                .map(Func1<AccountModel, Boolean> { accountModel ->
                    if (accountModel != null && accountModel.email == userName) {
                        putPassword(password)
                        putUserName(userName)
                        isLoggedIn = true
                        return@Func1 true
                    }
                    isLoggedIn = false
                    false
        }).subscribeOn(Schedulers.io())

    }

    private fun putUserName(userName: String) {
        Hawk.put(EMAIL_ADDRESS, userName)
    }

    private fun putPassword(password: String) {
        Hawk.put(PASSWORD, password)
    }

    fun logout() {
        Hawk.clear()
        bus.post(LogoutEvent())
    }

    companion object {
        private val IS_LOGGED_IN = "IS_LOGGED_IN"
        private val EMAIL_ADDRESS = "EMAIL"
        private val PASSWORD = "PASS"

        val userName: String
            get() = Hawk.get<String>(EMAIL_ADDRESS)?:""

        val password: String
            get() = Hawk.get<String>(PASSWORD)?:""
    }
}
