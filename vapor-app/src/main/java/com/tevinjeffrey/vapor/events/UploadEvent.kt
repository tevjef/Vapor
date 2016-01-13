package com.tevinjeffrey.vapor.events

import android.net.Uri

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem

class UploadEvent(val uri: Uri? = null, val item: CloudAppItem)
