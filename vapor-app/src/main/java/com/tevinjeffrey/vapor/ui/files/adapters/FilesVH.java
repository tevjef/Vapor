package com.tevinjeffrey.vapor.ui.files.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaprApp;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder.IconValue;
import net.steamcrafted.materialiconlib.MaterialIconView;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.*;

public class FilesVH extends RecyclerView.ViewHolder {

    final TextView fileTitle;
    final ImageView fileImage;
    final View parent;
    final TextView fileTimeAgo;
    final MaterialIconView fileLink;

    @Inject
    ClipboardManager clipboardManager;

    public FilesVH(View parent, ImageView imageView, TextView fileTitle, TextView fileTimeAgo, MaterialIconView fileLink) {
        super(parent);
        this.parent = parent;
        this.fileTitle = fileTitle;
        this.fileImage = imageView;
        this.fileTimeAgo = fileTimeAgo;
        this.fileLink = fileLink;
        VaprApp.objectGraph(parent.getContext()).inject(this);
    }

    static FilesVH newInstance(View parent) {
        ImageView imageView = ButterKnife.findById(parent, R.id.files_list_image);
        TextView fileTitle = ButterKnife.findById(parent, R.id.files_list_title);
        TextView fileTimeAgo = ButterKnife.findById(parent, R.id.files_list_time_ago);
        MaterialIconView fileLink = ButterKnife.findById(parent, R.id.files_list_link);

        return new FilesVH(parent, imageView, fileTitle, fileTimeAgo, fileLink);
    }

    public void setTitle(CloudAppItem cloudAppItem) {
        fileTitle.setText(cloudAppItem.getName());
    }

    public void setTimeAgo(CloudAppItem cloudAppItem) {
        fileTimeAgo.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(cloudAppItem.getCreatedAt()), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));
    }

    public void setLink(final CloudAppItem cloudAppItem) {
        fileLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("Uploaded item url", cloudAppItem.getUrl());
                clipboardManager.setPrimaryClip(clip);
                Toast.makeText(parent.getContext(), "Copied: " + cloudAppItem.getUrl(), Toast.LENGTH_SHORT).show();
            }
        });
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


    public void setItem(CloudAppItem item) {
        setImage(item);
        setTitle(item);
        setTimeAgo(item);
        setLink(item);
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
