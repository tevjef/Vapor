package com.tevinjeffrey.vapor.ui.files

import android.os.Bundle

import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.tevinjeffrey.vapor.events.LoginEvent
import com.tevinjeffrey.vapor.events.RefreshEvent
import com.tevinjeffrey.vapor.okcloudapp.DataManager
import com.tevinjeffrey.vapor.okcloudapp.UserManager
import com.tevinjeffrey.vapor.ui.base.BasePresenter

import java.util.concurrent.TimeUnit

import jonathanfinerty.once.Once

class FilesActivityPresenterImpl(private val dataManager: DataManager, private val bus: Bus) : BasePresenter<FilesActivityView>(), FilesActivityPresenter {

    override var navContext: FilesActivityPresenter.NavContext = FilesActivityPresenter.NavContext.ALL

    override fun loadEmail() {
        if (view != null) {
            view!!.setEmailInHeader(UserManager.userName)
        }
    }

    override fun refreshClicked() {
        bus.post(RefreshEvent())
    }

    override fun onActivityCreated(savedInstanceState: Bundle) {
        super.onActivityCreated(savedInstanceState)
        if (Once.beenDone(TimeUnit.HOURS, 6, DataManager.SYNC_ALL_ITEMS)) {
            dataManager.syncAllItems(false)
        }
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    @Subscribe
    fun onLogin(event: LoginEvent) {
        loadEmail()
    }

}
