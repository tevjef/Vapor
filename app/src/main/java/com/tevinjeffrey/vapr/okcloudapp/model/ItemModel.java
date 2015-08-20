package com.tevinjeffrey.vapr.okcloudapp.model;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.tevinjeffrey.vapr.okcloudapp.CloudAppUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import static com.tevinjeffrey.vapr.okcloudapp.CloudAppUtils.formatDate;

public class ItemModel {
    long id;
    String href;
    String name;
    @SerializedName("private")
    boolean isPrivate;
    @SerializedName("subscribed")
    boolean isSubscribed;
    String content_url;
    String item_type;
    long view_counter;
    String icon;
    String url;
    String remote_url;
    String thumbnail_url;
    String download_url;
    String source;
    boolean favorite;
    String owner_id;
    String created_at;
    String updated_at;
    String deleted_at;
    String lastViewed_at;

    public ItemModel() {
    }

    @Override
    public String toString() {
        return "ItemModel{" +
                "id=" + id +
                ", href='" + href + '\'' +
                ", name='" + name + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}
