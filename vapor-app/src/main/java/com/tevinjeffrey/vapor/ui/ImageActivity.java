package com.tevinjeffrey.vapor.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.customviews.TouchImageView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageActivity extends Activity {


    @Bind(R.id.imageView)
    TouchImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY, WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        Glide.with(this).load(getIntent().getData())
                .placeholder(MaterialDrawableBuilder.with(this)
                        .setIcon(MaterialDrawableBuilder.IconValue.FILE_IMAGE)
                        .setColor(android.R.color.white)
                        .build())
                .into(imageView);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
