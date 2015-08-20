package com.tevinjeffrey.vapr.ui.files.fragments;

import com.tevinjeffrey.vapr.okcloudapp.model.ItemModel;
import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem.ItemType;
import com.tevinjeffrey.vapr.ui.base.Presenter;
import com.tevinjeffrey.vapr.ui.base.View;

public interface FilesPresenter<V extends View> extends Presenter<V> {

    void loadData(boolean pullToRefresh, boolean refreshData);

    boolean isLoading();
}
