package com.tevinjeffrey.vapor.ui.files.fragments.presenters;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapor.ui.files.fragments.views.FilesView;

import javax.inject.Inject;

public class AudioPresenter<V extends FilesView> extends BaseFilesPresenterImpl<V> {
    public AudioPresenter() {
        itemType = ItemType.AUDIO;
    }
    @Inject
    Bus bus;

    @Subscribe
    public void dbUpdate(DatabaseUpdateEvent event) {
        loadData(false, false);
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
