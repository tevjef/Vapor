package com.tevinjeffrey.vapor.ui.files

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem

interface BottomSheetView {
    fun showLoading(isLoading: Boolean)
    fun showError(message: String)
    fun rename(cloudAppItem: CloudAppItem)
    fun deleteItem(cloudAppItem: CloudAppItem)
    fun hideSheet()
}
