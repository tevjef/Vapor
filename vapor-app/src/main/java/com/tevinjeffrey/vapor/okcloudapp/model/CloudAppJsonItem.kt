package com.tevinjeffrey.vapor.okcloudapp.model

import com.google.gson.annotations.SerializedName

class CloudAppJsonItem {

    var item: Item? = null
    var deleted: Boolean = false

    class Item(name: String? = null, isPrivate: Boolean = false, deleteAt: String? = null, redirectUrl: String?  = null) {
        var name: String? = null
        @SerializedName("private")
        var isPrivate: Boolean = false
        var deletedAt: String? = null
        var redirectUrl: String? = null
    }
}
