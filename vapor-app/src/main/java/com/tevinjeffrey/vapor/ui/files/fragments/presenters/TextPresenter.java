package com.tevinjeffrey.vapor.ui.files.fragments.presenters;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapor.events.RefreshEvent;
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;

import javax.inject.Inject;

public class TextPresenter extends BaseFilesPresenterImpl {
    public TextPresenter() {
        itemType = ItemType.TEXT;
    }
    @Inject
    Bus bus;

    @Subscribe
    public void dbUpdate(DatabaseUpdateEvent event) {
        loadData(false, false, true);
    }

    @Subscribe
    public void onUploadEvent(UploadEvent event) {
        loadData(false, true, false);
    }

    @Subscribe
    public void onRefreshEvent(RefreshEvent event) {
        if (getView() != null && getView().isVisibleInPager()) {
            loadData(true, true, false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }
}
