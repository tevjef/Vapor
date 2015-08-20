package com.tevinjeffrey.vapr.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tevinjeffrey.vapr.R;
import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapr.okcloudapp.model.ItemModel;

import java.util.List;

public class FilesFragmentAdapter extends RecyclerView.Adapter<FilesVH> {

    private final List<CloudAppItem> cloudAppItems;
    private final ItemClickListener<CloudAppItem, View> itemClickListener;

    public FilesFragmentAdapter(List<CloudAppItem> cloudAppItems, @NonNull ItemClickListener<CloudAppItem, View> listener) {
        this.cloudAppItems = cloudAppItems;
        this.itemClickListener = listener;
        setHasStableIds(true);
    }

    @Override
    public FilesVH onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final Context context = viewGroup.getContext();
        final View parent = LayoutInflater.from(context).inflate(R.layout.file_list_item, viewGroup, false);

        return FilesVH.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(final FilesVH holder, int position) {

        final CloudAppItem cloudAppItem
                = cloudAppItems.get(position);

        holder.setTitle(cloudAppItem);
        holder.setImage(cloudAppItem);

       holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClicked(cloudAppItem, v);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return cloudAppItems.get(position).getItemId();
    }

    @Override
    public int getItemCount() {
        return cloudAppItems.size();
    }
}
