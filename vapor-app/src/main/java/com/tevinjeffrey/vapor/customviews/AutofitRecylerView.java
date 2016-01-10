package com.tevinjeffrey.vapor.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/*https://github.com/chiuki/android-recyclerview/blob/master/app/src/main/java/com/sqisland/android/recyclerview/AutofitRecyclerView.java*/
public class AutofitRecylerView extends RecyclerView {
    private GridLayoutManager manager;
    private int columnWidth = -1;


    public AutofitRecylerView(Context context) {
        super(context);
        init(context, null);
    }


    public AutofitRecylerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public AutofitRecylerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            int[] attrsArray = {
                    android.R.attr.columnWidth
            };
            TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
            columnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }


        manager = new GridLayoutManager(getContext(), 1);
        setLayoutManager(manager);
    }


    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (columnWidth > 0) {
            int spanCount = Math.max(2, getMeasuredWidth() / columnWidth);
            manager.setSpanCount(spanCount);
        }
    }
}