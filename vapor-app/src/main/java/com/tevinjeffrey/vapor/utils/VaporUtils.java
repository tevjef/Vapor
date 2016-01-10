package com.tevinjeffrey.vapor.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

import timber.log.Timber;

import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ARCHIVE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.AUDIO;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.BOOKMARK;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.IMAGE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.TEXT;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.UNKNOWN;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.VIDEO;

public class VaporUtils {
    public static boolean isValidWebAddress(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.DOMAIN_NAME.matcher(target).matches();
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void openLink(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }

    public static Drawable getTypedDrawable(Context context, CloudAppItem cloudAppItem) {
        CloudAppItem.ItemType type = cloudAppItem.getItemType();
        Drawable drawable = null;
        if (type == IMAGE) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_photo);
        } else if (type == VIDEO) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_file_video);
        } else if (type == ARCHIVE) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_zip_box);
        } else if (type == BOOKMARK) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_bookmark);
        } else if (type == AUDIO) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_headset);
        } else if (type == TEXT) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_file_document);
        } else if (type == UNKNOWN) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_file_cloud);
        }
        return drawable;
    }

    public static void setTypedImageView(CloudAppItem cloudAppItem, final ImageView imageView, Drawable placeholder, boolean thumbnail) {
        if (cloudAppItem.getItemType() == IMAGE) {
            Glide.with(imageView.getContext().getApplicationContext())
                    .load(thumbnail?cloudAppItem.getThumbnailUrl():cloudAppItem.getRemoteUrl())
                    .crossFade()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.IMMEDIATE)
                    .placeholder(placeholder)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            if (e != null) {
                                Timber.e(e, "Failed to load image");
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                            return false;
                        }
                    })
                    .into(imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            Drawable drawable = getTypedDrawable(imageView.getContext(), cloudAppItem);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageDrawable(drawable);
        }
    }
}
