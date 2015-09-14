package com.tevinjeffrey.vapor.ui.files.fragments.views;

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.ui.base.View;

import java.util.List;

public interface FilesView extends View {
    void showLoading(boolean pullToRefresh);

    void setData(List<CloudAppItem> data);

    void showError(Throwable e);

    void showLayout(LayoutType type);
}
