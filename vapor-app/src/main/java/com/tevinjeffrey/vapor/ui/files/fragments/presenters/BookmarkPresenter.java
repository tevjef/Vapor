package com.tevinjeffrey.vapor.ui.files.fragments.presenters;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapor.events.UploadEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType;

import javax.inject.Inject;

public class BookmarkPresenter extends BaseFilesPresenterImpl {
    public BookmarkPresenter() {
        itemType = ItemType.BOOKMARK;
    }
    @Inject
    Bus bus;


    @Subscribe
    public void dbUpdate(DatabaseUpdateEvent event) {
        loadData(false, false, true);
    }

    @Subscribe
    public void onUploadEvent(UploadEvent event) {
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
