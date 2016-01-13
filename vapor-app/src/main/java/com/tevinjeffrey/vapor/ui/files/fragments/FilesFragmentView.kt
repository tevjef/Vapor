package com.tevinjeffrey.vapor.ui.files.fragments

import com.tevinjeffrey.vapor.okcloudapp.DataManager
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.ui.base.View

interface FilesFragmentView : View {
    fun showLoading(pullToRefresh: Boolean)

    fun setData(data: List<CloudAppItem>)

    fun appendData(data: List<CloudAppItem>)

    val isVisibleInPager: Boolean

    fun showError(e: Throwable)

    fun showLayout(type: View.LayoutType)

    fun initRecyclerView()

    fun initSwipeLayout()

    val cursor: DataManager.DataCursor
}
