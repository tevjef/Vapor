package com.tevinjeffrey.vapor.okcloudapp.model;

import com.google.gson.annotations.SerializedName;

public class ItemModel {
    long id;
    String href;
    String name;
    @SerializedName("private")
    boolean isPrivate;
    @SerializedName("subscribed")
    boolean isSubscribed;
    String contentUrl;
    String itemType;
    long viewCounter;
    String icon;
    String url;
    String remoteUrl;
    String thumbnailUrl;
    String downloadUrl;
    String source;
    boolean favorite;
    String ownerId;
    long contentLength;
    String createdAt;
    String updatedAt;
    String deletedAt;
    String lastViewedAt;

    public ItemModel() {
    }

    @Override
    public String toString() {
        return "ItemModel{" +
                "id=" + id +
                ", href='" + href + '\'' +
                ", name='" + name + '\'' +
                ", created_at='" + createdAt + '\'' +
                '}';
    }
}
