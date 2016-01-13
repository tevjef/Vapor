package com.tevinjeffrey.vapor.customviews

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

import com.tevinjeffrey.vapor.R

/*https://github.com/chiuki/android-recyclerview/blob/master/app/src/main/java/com/sqisland/android/recyclerview/MarginDecoration.java*/
class MarginDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val margin: Int

    init {
        margin = context.resources.getDimensionPixelSize(R.dimen.item_margin)
    }


    override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        outRect.set(margin, margin, margin, margin)
    }
}