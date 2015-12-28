package com.tevinjeffrey.vapor.ui.files.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tevinjeffrey.vapor.R;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

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
        final View parent = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_list_item, viewGroup, false);
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
    public long getItemId(int position) {
        return cloudAppItems.get(position).getItemId();
    }

    @Override
    public int getItemCount() {
        return cloudAppItems.size();
    }
}
