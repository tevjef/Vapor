package com.tevinjeffrey.vapor.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.customviews.TouchImageView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends Activity {


    @Bind(R.id.imageView)
    ImageView imageView;
    PhotoViewAttacher mAttacher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        final MaterialDialog progress = new MaterialDialog.Builder(this).content("Please wait...").progress(true, 0).show();

        Glide.with(getApplicationContext()).load(getIntent().getData())
                .placeholder(MaterialDrawableBuilder.with(this)
                        .setIcon(MaterialDrawableBuilder.IconValue.FILE_IMAGE)
                        .setColor(android.R.color.white)
                        .build())
                .listener(new RequestListener<Uri, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progress.dismiss();
                        imageView.setImageDrawable(resource);
                        mAttacher = new PhotoViewAttacher(imageView);
                        return false;
                    }
                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
