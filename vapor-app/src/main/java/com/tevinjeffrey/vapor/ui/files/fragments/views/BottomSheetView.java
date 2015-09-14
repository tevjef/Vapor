package com.tevinjeffrey.vapor.ui.files.fragments.views;

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

public interface BottomSheetView {
    void showLoading(boolean isLoading);
    void showError(String message);
    void rename(CloudAppItem cloudAppItem);
    void deleteItem(CloudAppItem cloudAppItem);
    void hideSheet();
}
