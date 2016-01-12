package com.tevinjeffrey.vapor.okcloudapp.model

import com.google.gson.annotations.SerializedName

class UploadModel {

    var uploadsRemaining: Long? = null
    var maxUploadSize: Long = 0
    var url: String? = null
    var params: Parameters? = null

    inner class Parameters {
        @SerializedName("AWSAccessKeyId")
        var AWSAccessKeyId: String? = null
        var key: String? = null
        var acl: String? = null
        var successActionRedirect: String? = null
        var signature: String? = null
        var policy: String? = null
    }
}
