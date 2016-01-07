package com.tevinjeffrey.vapor.ui.files;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.VaporApp;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.ui.utils.ItemClickListener;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.ARCHIVE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.AUDIO;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.BOOKMARK;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.IMAGE;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.TEXT;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.UNKNOWN;
import static com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem.ItemType.VIDEO;

public class FilesFragmentAdapter extends RecyclerView.Adapter<FilesFragmentAdapter.FilesVH> {

    private final List<CloudAppItem> cloudAppItems;
    private final ItemClickListener<CloudAppItem, View> itemClickListener;

    public FilesFragmentAdapter(List<CloudAppItem> cloudAppItems, @NonNull ItemClickListener<CloudAppItem, View> listener) {
        this.cloudAppItems = cloudAppItems;
        this.itemClickListener = listener;
        setHasStableIds(true);
    }

    @Override
    public FilesVH onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View parent = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_grid_list_item, viewGroup, false);
        final FilesVH filesVH = FilesVH.newInstance(parent);
        filesVH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = filesVH.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClicked(cloudAppItems.get(adapterPos), v);
                }
            }
        });
        return filesVH;
    }

    @Override
    public void onBindViewHolder(final FilesVH holder, int position) {
        final CloudAppItem cloudAppItem = cloudAppItems.get(position);
        holder.setItem(cloudAppItem);
    }

    @Override
    public void onViewRecycled(FilesVH holder) {
        super.onViewRecycled(holder);
        Glide.clear(holder.getFileImage());
    }

    @Override
    public long getItemId(int position) {
        return cloudAppItems.get(position).getItemId();
    }

    @Override
    public int getItemCount() {
        return cloudAppItems.size();
    }

    public static class FilesVH extends RecyclerView.ViewHolder {

        final TextView fileTitle;
        final ImageView fileImage;
        final View parent;
        final TextView fileTimeAgo;

        @Inject
        ClipboardManager clipboardManager;

        public FilesVH(View parent, ImageView imageView, TextView fileTitle, TextView fileTimeAgo) {
            super(parent);
            this.parent = parent;
            this.fileTitle = fileTitle;
            this.fileImage = imageView;
            this.fileTimeAgo = fileTimeAgo;
            VaporApp.uiComponent(parent.getContext()).inject(this);
        }

        static FilesVH newInstance(View parent) {
            ImageView imageView = ButterKnife.findById(parent, R.id.files_list_image);
            TextView fileTitle = ButterKnife.findById(parent, R.id.files_list_title);
            TextView fileTimeAgo = ButterKnife.findById(parent, R.id.files_list_time_ago);

            return new FilesVH(parent, imageView, fileTitle, fileTimeAgo);
        }

        public void setTitle(CloudAppItem cloudAppItem) {
            fileTitle.setText(cloudAppItem.getName());
        }

        public void setTimeAgo(CloudAppItem cloudAppItem) {
            fileTimeAgo.setText(DateUtils.getRelativeTimeSpanString(Long.parseLong(cloudAppItem.getCreatedAt()), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));
        }

        public void setImage(CloudAppItem cloudAppItem) {
            CloudAppItem.ItemType type = cloudAppItem.getItemType();
            Drawable drawable = null;

            final View background = ButterKnife.findById(parent, R.id.background_shade);
            final TextView itemName = ButterKnife.findById(parent, R.id.files_list_title);
            final TextView timeSince = ButterKnife.findById(parent, R.id.files_list_time_ago);

            itemName.setTextColor(Color.WHITE);
            timeSince.setTextColor(Color.WHITE);
            background.setBackgroundColor(ContextCompat.getColor(background.getContext(), R.color.primary_dark));

            if (type == IMAGE) {
                Glide.with(fileImage.getContext().getApplicationContext())
                        .load(cloudAppItem.getThumbnailUrl())
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        Palette.Swatch swatch = palette.getDarkVibrantSwatch();
                                        if (swatch != null) {
                                            ObjectAnimator.ofArgb(itemName, "textColor", itemName.getCurrentTextColor(), swatch.getBodyTextColor()).setDuration(500).start();
                                            ObjectAnimator.ofArgb(timeSince, "textColor", timeSince.getCurrentTextColor(), swatch.getTitleTextColor()).setDuration(500).start();
                                            background.setHasTransientState(true);
                                            ObjectAnimator animator3 = ObjectAnimator.ofArgb(background, "backgroundColor", ((ColorDrawable)background.getBackground()).getColor(),
                                                    ColorUtils.setAlphaComponent(swatch.getRgb(), 204));
                                            animator3.addListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationStart(Animator animation) {
                                                    super.onAnimationStart(animation);
                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    super.onAnimationEnd(animation);
                                                    background.setHasTransientState(false);
                                                }
                                            });
                                            animator3.setDuration(250).start();
                                        }

                                    }
                                });

                                return false;
                            }
                        })
                        .into(fileImage);
                return;
            } else if (type == VIDEO) {
                drawable = getDrawable(MaterialDrawableBuilder.IconValue.FILE_VIDEO);
            } else if (type == ARCHIVE) {
                drawable = getDrawable(MaterialDrawableBuilder.IconValue.ZIP_BOX);
            } else if (type == BOOKMARK) {
                drawable = getDrawable(MaterialDrawableBuilder.IconValue.BOOKMARK);
            } else if (type == AUDIO) {
                drawable = getDrawable(MaterialDrawableBuilder.IconValue.FILE_MUSIC);
            } else if (type == TEXT) {
                drawable = getDrawable(MaterialDrawableBuilder.IconValue.FILE_DOCUMENT);
            } else if (type == UNKNOWN) {
                drawable = getDrawable(MaterialDrawableBuilder.IconValue.FILE_CLOUD);
            }

            fileImage.setImageDrawable(drawable);
        }


        public void setItem(CloudAppItem item) {
            setImage(item);
            setTitle(item);
            setTimeAgo(item);
        }

        public Drawable getDrawable(MaterialDrawableBuilder.IconValue value) {
            return MaterialDrawableBuilder.with(parent.getContext().getApplicationContext())
                    .setIcon(value)
                    .setSizeDp(34)
                    .setColor(Color.WHITE)
                    .build();
        }

        public void setOnClickListener(View.OnClickListener listener) {
            parent.setOnClickListener(listener);
        }

        public ImageView getFileImage() {
            return fileImage;
        }
    }
}
