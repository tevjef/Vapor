package com.tevinjeffrey.vapor.events

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem

class RenameEvent(item: CloudAppItem) : DataSetUpdateEvent(item)
