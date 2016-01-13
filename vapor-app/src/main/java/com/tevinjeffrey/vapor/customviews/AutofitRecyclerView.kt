package com.tevinjeffrey.vapor.customviews

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

/*https://github.com/chiuki/android-recyclerview/blob/master/app/src/main/java/com/sqisland/android/recyclerview/AutofitRecyclerView.java*/
class AutofitRecyclerView : RecyclerView {
    private var manager: GridLayoutManager? = null
    private var columnWidth = -1


    constructor(context: Context) : super(context) {
        init(context, null)
    }


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val attrsArray = intArrayOf(android.R.attr.columnWidth)
            val array = context.obtainStyledAttributes(attrs, attrsArray)
            columnWidth = array.getDimensionPixelSize(0, -1)
            array.recycle()
        }


        manager = GridLayoutManager(getContext(), 1)
        layoutManager = manager
    }


    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        if (columnWidth > 0) {
            val spanCount = Math.max(2, measuredWidth / columnWidth)
            manager!!.spanCount = spanCount
        }
    }
}