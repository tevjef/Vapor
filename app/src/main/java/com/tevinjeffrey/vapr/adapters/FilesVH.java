package com.tevinjeffrey.vapr.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tevinjeffrey.vapr.R;
import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder.IconValue;

import butterknife.ButterKnife;

import static com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem.ItemType.*;

public class FilesVH extends RecyclerView.ViewHolder {

    final TextView fileTitle;
    final ImageView fileImage;
    final View parent;

    public FilesVH(View parent, ImageView imageView, TextView fileTitle) {
        super(parent);
        this.parent = parent;
        this.fileTitle = fileTitle;
        this.fileImage = imageView;

    }

    static FilesVH newInstance(View parent) {
        ImageView imageView = ButterKnife.findById(parent, R.id.files_list_image);
        TextView fileTitle = ButterKnife.findById(parent, R.id.files_list_title);

        return new FilesVH(parent, imageView, fileTitle);
    }

    public void setTitle(CloudAppItem cloudAppItem) {
        fileTitle.setText(cloudAppItem.getName());
    }

    public void setImage(CloudAppItem cloudAppItem) {
        CloudAppItem.ItemType type = cloudAppItem.getItemType();
        Drawable drawable = null;
        if (type == IMAGE) {
            Glide.with(fileImage.getContext().getApplicationContext())
                    .load(cloudAppItem.getThumbnailUrl())
                    .placeholder(getDrawable(IconValue.FILE_IMAGE))
                    .centerCrop().into(fileImage);
            return;
        } else if (type == VIDEO) {
            drawable = getDrawable(IconValue.FILE_VIDEO);
        } else if (type == ARCHIVE) {
            drawable = getDrawable(IconValue.ZIP_BOX);
        } else if (type == BOOKMARK) {
            drawable = getDrawable(IconValue.BOOKMARK);
        } else if (type == AUDIO) {
            drawable = getDrawable(IconValue.FILE_MUSIC);
        } else if (type == TEXT) {
            drawable = getDrawable(IconValue.FILE_DOCUMENT);
        } else if (type == UNKNOWN) {
            drawable = getDrawable(IconValue.FILE_CLOUD);
        }
        fileImage.setImageDrawable(drawable);
    }

    public Drawable getDrawable(IconValue value) {
        return MaterialDrawableBuilder.with(parent.getContext().getApplicationContext())
                .setIcon(value)
                .setColor(Color.WHITE)
                .build();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        parent.setOnClickListener(listener);
    }
}
