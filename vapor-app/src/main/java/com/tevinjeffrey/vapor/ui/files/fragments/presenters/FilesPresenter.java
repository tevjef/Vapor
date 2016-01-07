package com.tevinjeffrey.vapor.ui.files.fragments.presenters;

import com.tevinjeffrey.vapor.ui.base.Presenter;
import com.tevinjeffrey.vapor.ui.base.View;

public interface FilesPresenter<V extends View> extends Presenter<V> {
    void loadData(boolean pullToRefresh, boolean refreshData, boolean useCursor);
    boolean isLoading();
}
