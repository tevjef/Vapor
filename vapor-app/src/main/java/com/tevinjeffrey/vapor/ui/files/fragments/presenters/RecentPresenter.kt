package com.tevinjeffrey.vapor.ui.files.fragments.presenters

import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent
import com.tevinjeffrey.vapor.events.RefreshEvent
import com.tevinjeffrey.vapor.events.UploadEvent
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem

import javax.inject.Inject

class RecentPresenter : BaseFilesPresenterImpl() {
    init {
        itemType = CloudAppItem.ItemType.ALL
    }

    @Subscribe
    fun dbUpdate(event: DatabaseUpdateEvent) {
        loadData(false, false, true)
    }

    @Subscribe
    fun onUploadEvent(event: UploadEvent) {
        loadData(false, true, false)
    }

    @Subscribe
    fun onRefreshEvent(event: RefreshEvent) {
        if (view != null && view!!.isVisibleInPager) {
            loadData(true, true, false)
        }
    }

    override fun onPause() {
        super.onPause()
        bus.unregister(this)
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

}
