package com.tevinjeffrey.vapor.ui.files.fragments;

import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.ui.base.View;

import java.util.List;

public interface FilesFragmentView extends View {
    void showLoading(boolean pullToRefresh);

    void setData(List<CloudAppItem> data);

    void appendData(List<CloudAppItem> data);

    void showError(Throwable e);

    void showLayout(LayoutType type);

    void initRecyclerView();

    void initSwipeLayout();

    DataManager.DataCursor getCursor();
}
