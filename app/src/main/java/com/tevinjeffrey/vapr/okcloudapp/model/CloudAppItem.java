package com.tevinjeffrey.vapr.okcloudapp.model;

import com.orm.SugarRecord;
import com.tevinjeffrey.vapr.okcloudapp.CloudAppUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import static com.tevinjeffrey.vapr.okcloudapp.CloudAppUtils.formatDate;
import static com.tevinjeffrey.vapr.okcloudapp.CloudAppUtils.getTime;

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
        this.contentUrl = itemModel.content_url;
        this.itemType = itemModel.item_type;
        this.viewCounter = itemModel.view_counter;
        this.icon = itemModel.icon;
        this.url = itemModel.url;
        this.remoteUrl = itemModel.remote_url;
        this.thumbnailUrl = itemModel.thumbnail_url;
        this.downloadUrl = itemModel.download_url;
        this.source = itemModel.source;
        this.createdAt = String.valueOf(getTime(formatDate(itemModel.created_at)));
        this.updatedAt = String.valueOf(getTime(formatDate(itemModel.updated_at)));
        this.deletedAt = String.valueOf(getTime(formatDate(itemModel.deleted_at)));
        this.lastViewedAt = String.valueOf(getTime(formatDate(itemModel.lastViewed_at)));
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



    
    public String getFormattedCreatedAt()  {

        Date d = new Date();
        d.setTime(Long.parseLong(createdAt));

        String s = DateFormat.getDateInstance()
                .format(d);

        return s;
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



    public enum ItemType {
        ALL, DELETED, AUDIO, BOOKMARK, IMAGE, UNKNOWN, VIDEO, ARCHIVE, TEXT;
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
