package com.tevinjeffrey.vapor.events

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem

class DeleteEvent(item: CloudAppItem) : DataSetUpdateEvent(item)
