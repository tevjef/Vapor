package com.tevinjeffrey.vapor.okcloudapp.model

import com.google.gson.annotations.SerializedName

class ItemModel {
    var id: Long = 0
    var href: String? = null
    var name: String? = null
    @SerializedName("private")
    var isPrivate: Boolean = false
    @SerializedName("subscribed")
    var isSubscribed: Boolean = false
    var contentUrl: String? = null
    var itemType: String? = null
    var viewCounter: Long = 0
    var icon: String? = null
    var url: String? = null
    var remoteUrl: String? = null
    var thumbnailUrl: String? = null
    var downloadUrl: String? = null
    var source: String? = null
    var favorite: Boolean = false
    var ownerId: String? = null
    var contentLength: Long = 0
    var createdAt: String? = null
    var updatedAt: String? = null
    var deletedAt: String? = null
    var lastViewedAt: String? = null
}
