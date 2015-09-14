package com.tevinjeffrey.vapor.ui.files.fragments.presenters;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.tevinjeffrey.vapor.events.DatabaseUpdateEvent;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.ui.files.fragments.views.FilesView;

import javax.inject.Inject;

public class RecentPresenter<V extends FilesView> extends BaseFilesPresenterImpl<V> {
    public RecentPresenter() {
        itemType = CloudAppItem.ItemType.ALL;
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
