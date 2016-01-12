package com.tevinjeffrey.vapor.okcloudapp

import com.squareup.okhttp.RequestBody

abstract class CloudAppRequestBody : RequestBody() {
    abstract val fileName: String
    abstract override fun contentLength(): Long
    val fileSize: String
        get() = contentLength().toString()
}
