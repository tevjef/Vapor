package com.tevinjeffrey.vapr.ui.files.fragments.presenters;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapr.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapr.ui.files.fragments.FilesView;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.base.BaseFilesPresenterImpl;

import javax.inject.Inject;

public class TextPresenter<V extends FilesView> extends BaseFilesPresenterImpl<V> {
    public TextPresenter() {
        itemType = ItemType.TEXT;
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
