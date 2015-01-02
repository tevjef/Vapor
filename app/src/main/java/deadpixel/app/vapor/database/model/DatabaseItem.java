package deadpixel.app.vapor.database.model;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.cloudapp.impl.model.ItemModel;

/**
 * Created by Tevin on 7/27/2014.
 */
public class DatabaseItem extends ItemModel implements CloudAppItem{

    private boolean isSyncedToDisk;
    private String uri;

    public int getPrivacy() {
       return this.isPrivate()?1:0;
    }
    public int getSubscribed() {
        return this.isSubscribed()?1:0;
    }
    public void setPrivacy(int p) {
        if (p == 1) {
            super.setPrivacy(true);
        }else {
            super.setPrivacy(false);
        }
    }
    public void setSubscribed(int p) {
        if (p == 1) {
            super.setSubscribed(true);
        }else {
            super.setSubscribed(false);
        }
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isSyncedToDisk() {
        return isSyncedToDisk;
    }

    public void setSyncedToDisk(int i) {
        this.isSyncedToDisk = i == 1;
    }

    public static DatabaseItem toDatabaseItem(CloudAppItem cItem) {
        DatabaseItem item = new DatabaseItem();

        item.setId(cItem.getId());
        item.setName(cItem.getName());
        item.setPrivacy(cItem.isPrivate());
        item.setSubscribed(cItem.isSubscribed());
        item.setHref(cItem.getHref());
        item.setItemType(cItem.getItemType().toString());
        item.setViewCounter(cItem.getViewCounter());
        item.setIcon(cItem.getIcon());
        item.setUri(cItem.getUrl());
        item.setRemoteUrl(cItem.getRemoteUrl());
        item.setSource(cItem.getSource());
        item.setDownloadUrl(cItem.getDownloadUrl());
        item.setThumbnailUrl(cItem.getThumbnailUrl());
        item.setContentUrl(cItem.getContentUrl());
        item.setCreatedAt(cItem.getCreatedAt());
        item.setUpdatedAt(cItem.getUpdatedAt());
        if(cItem.getDeletedAt() != null)
            item.setDeletedAt(cItem.getDeletedAt());
        if(cItem.getLastViewedAt() != null)
            item.setLastViewedAt(cItem.getLastViewedAt());


        return item;
    }
/*
    @Override
    public String toString() {
        return this.getName() +
                this.getCreatedAt() +
                this.get
    }*/
}
