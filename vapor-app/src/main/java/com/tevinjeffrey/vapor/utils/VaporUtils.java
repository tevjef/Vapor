package com.tevinjeffrey.vapor.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import timber.log.Timber;
import uk.co.senab.photoview.PhotoViewAttacher;

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

    public static Drawable getIconDrawable(Context context, MaterialDrawableBuilder.IconValue value) {
        return getIconDrawable(context, value, 24);
    }

    public static Drawable getIconDrawable(Context context, MaterialDrawableBuilder.IconValue value, int size) {
        return MaterialDrawableBuilder.with(context.getApplicationContext())
                .setIcon(value)
                .setSizeDp(size)
                .setColor(Color.WHITE)
                .build();
    }

    public static Drawable getTypedDrawable(Context context, CloudAppItem cloudAppItem, int size) {
        CloudAppItem.ItemType type = cloudAppItem.getItemType();
        Drawable drawable = null;
        if (type == IMAGE) {
            drawable = getIconDrawable(context, MaterialDrawableBuilder.IconValue.FILE_IMAGE, size);
        } else if (type == VIDEO) {
            drawable = getIconDrawable(context, MaterialDrawableBuilder.IconValue.FILE_VIDEO, size);
        } else if (type == ARCHIVE) {
            drawable = getIconDrawable(context, MaterialDrawableBuilder.IconValue.ZIP_BOX, size);
        } else if (type == BOOKMARK) {
            drawable = getIconDrawable(context, MaterialDrawableBuilder.IconValue.BOOKMARK, size);
        } else if (type == AUDIO) {
            drawable = getIconDrawable(context, MaterialDrawableBuilder.IconValue.FILE_MUSIC, size);
        } else if (type == TEXT) {
            drawable = getIconDrawable(context, MaterialDrawableBuilder.IconValue.FILE_DOCUMENT, size);
        } else if (type == UNKNOWN) {
            drawable = getIconDrawable(context, MaterialDrawableBuilder.IconValue.FILE_CLOUD, size);
        }
        return drawable;
    }

    public static void setTypedImageView(CloudAppItem cloudAppItem, final ImageView imageView, Drawable placeholder, boolean thumbnail, int size) {
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
            Drawable drawable = getTypedDrawable(imageView.getContext(), cloudAppItem, size);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageDrawable(drawable);
        }
    }

    public static void expand(final View v) {
        float density = v.getContext().getResources().getDisplayMetrics().density;
        //v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        final int target = (int) (v.getContext().getApplicationContext().getResources().getDisplayMetrics().heightPixels /density);
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        //v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height =  (int)(target * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(target / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}
