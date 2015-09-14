package com.tevinjeffrey.vapor.okcloudapp.model;

import com.google.gson.annotations.SerializedName;

public class CloudAppJsonItem {

    public Item item;
    public boolean deleted;

    public static class Item {
        public String name;
        @SerializedName("private")
        public boolean isPrivate;
        public String deletedAt;
        public String redirectUrl;
    }
}
