package com.tevinjeffrey.vapor.customviews;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tevinjeffrey.vapor.R;

/*https://github.com/chiuki/android-recyclerview/blob/master/app/src/main/java/com/sqisland/android/recyclerview/MarginDecoration.java*/
public class MarginDecoration extends RecyclerView.ItemDecoration {
    private final int margin;

    public MarginDecoration(Context context) {
        margin = context.getResources().getDimensionPixelSize(R.dimen.item_margin);
    }


    @Override
    public void getItemOffsets(
            Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(margin, margin, margin, margin);
    }
}