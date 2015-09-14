package com.tevinjeffrey.vapor.okcloudapp.model;

import android.content.Context;

import com.orm.SugarRecord;
import com.tevinjeffrey.vapor.okcloudapp.CloudAppUtils;

import java.text.DateFormat;
import java.text.ParseException;

import static android.text.format.DateUtils.*;
import static com.tevinjeffrey.vapor.okcloudapp.CloudAppUtils.formatDate;

public class CloudAppItem extends SugarRecord<CloudAppItem> {

    private long itemId;
    private String href;
    private String name;
    private boolean isPrivate;
    private boolean isSubscribed;
    private String contentUrl;
    private String itemType;
    private long viewCounter;
    private String icon;
    private String url;
    private String remoteUrl;
    private String thumbnailUrl;
    private String downloadUrl;
    private String source;
    boolean favorite;
    String ownerId;
    long contentLength;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private String lastViewedAt;

    public CloudAppItem(ItemModel itemModel) {
        this.itemId = itemModel.id;
        this.href = itemModel.href;
        this.name = itemModel.name;
        this.isPrivate = itemModel.isPrivate;
        this.isSubscribed = itemModel.isSubscribed;
        this.contentUrl = itemModel.contentUrl;
        this.itemType = itemModel.itemType;
        this.viewCounter = itemModel.viewCounter;
        this.icon = itemModel.icon;
        this.url = itemModel.url;
        this.remoteUrl = itemModel.remoteUrl;
        this.thumbnailUrl = itemModel.thumbnailUrl;
        this.downloadUrl = itemModel.downloadUrl;
        this.source = itemModel.source;
        this.favorite = itemModel.favorite;
        this.ownerId = itemModel.ownerId;
        this.contentLength = itemModel.contentLength;
        this.createdAt = String.valueOf(formatDate(itemModel.createdAt));
        this.updatedAt = String.valueOf(formatDate(itemModel.updatedAt));
        this.deletedAt = String.valueOf(formatDate(itemModel.deletedAt));
        this.lastViewedAt = String.valueOf(formatDate(itemModel.lastViewedAt));
    }

    public CloudAppItem() {
    }

    public long getItemId() {
        return itemId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getHref() {
        return href;
    }

    
    public String getName() {
        return name;
    }

    
    public boolean isPrivate() {
        return isPrivate;
    }

    
    public boolean isSubscribed() {
        return isSubscribed;
    }

    public boolean isTrashed() {
        return deletedAt == null;
    }

    public String getUrl() {
        return url == null? remoteUrl : url;
    }

    
    public String getContentUrl() {
        return contentUrl;
    }

    
    public ItemType getItemType() {
        try {
            return ItemType.valueOf(itemType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ItemType.UNKNOWN;
        }
    }

    
    public long getViewCounter() {
        return viewCounter;
    }

    
    public String getIcon() {
        return icon;
    }

    
    public String getRemoteUrl() {
        return remoteUrl;
    }

    
    public String getSource() {
        return source;
    }

    
    public String getCreatedAt() {
        return createdAt;
    }

    
    public String getUpdatedAt() {
        return updatedAt;
    }

    
    public String getDeletedAt() {
        return deletedAt;
    }

    
    public String getLastViewedAt() {
        return lastViewedAt;
    }

    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getFormattedCreatedAt(Context context)  {

        String date = formatDateTime(context, Long.parseLong(createdAt), FORMAT_SHOW_TIME|FORMAT_SHOW_YEAR|FORMAT_SHOW_DATE);

        return date;
    }

    
    public String getFormattedUpdatedAt()  {
        try {
            return updatedAt ==null?null:DateFormat.getDateInstance()
                    .format(CloudAppUtils.format.parse(updatedAt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public String getFormattedDeletedAt() {
        try {
            return deletedAt ==null?null:DateFormat.getDateInstance()
                    .format(CloudAppUtils.format.parse(deletedAt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public enum ItemType {
        ALL, DELETED, AUDIO, BOOKMARK, IMAGE, UNKNOWN, VIDEO, ARCHIVE, TEXT
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "CloudAppItem{" +
                "name='" + name + '\'' +
                ", href='" + href + '\'' +
                ", itemId=" + itemId +
                '}';
    }

}
