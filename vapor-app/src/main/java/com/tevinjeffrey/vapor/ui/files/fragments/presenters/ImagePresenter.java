package com.tevinjeffrey.vapor.ui.files.fragments.presenters;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;

import javax.inject.Inject;

public class ImagePresenter extends BaseFilesPresenterImpl {
    public ImagePresenter() {
        itemType = ItemType.IMAGE;
    }

    @Inject
    Bus bus;

    @Subscribe
    public void dbUpdate(DatabaseUpdateEvent event) {
        loadData(false, false, false);
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
