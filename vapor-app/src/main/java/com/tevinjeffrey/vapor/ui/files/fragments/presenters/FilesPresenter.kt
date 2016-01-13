package com.tevinjeffrey.vapor.ui.files.fragments.presenters

import com.tevinjeffrey.vapor.ui.base.Presenter
import com.tevinjeffrey.vapor.ui.base.View

interface FilesPresenter<V : View> : Presenter<V> {
    fun loadData(pullToRefresh: Boolean, refreshData: Boolean, useCursor: Boolean)
    val isNotLoading: Boolean
    fun shouldShowEmpty(): Boolean
}
