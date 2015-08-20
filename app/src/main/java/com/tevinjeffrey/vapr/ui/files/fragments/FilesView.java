package com.tevinjeffrey.vapr.ui.files.fragments;

import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapr.okcloudapp.model.ItemModel;
import com.tevinjeffrey.vapr.ui.base.View;
import com.tevinjeffrey.vapr.ui.base.View.LayoutType;

import java.util.List;

public interface FilesView extends View {
    void showLoading(boolean pullToRefresh);

    void setData(List<CloudAppItem> data);

    void showError(Throwable e);

    void showLayout(LayoutType type);
}
