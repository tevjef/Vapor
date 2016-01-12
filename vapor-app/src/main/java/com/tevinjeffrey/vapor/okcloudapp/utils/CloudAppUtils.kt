package com.tevinjeffrey.vapor.okcloudapp.utils

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

object CloudAppUtils {

    val format: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val formatBis: DateFormat = SimpleDateFormat(
            "yyyy-MM-dd")
    private val TAG = "CloudAppUtils"

    fun formatDate(date: String?): Long {
        if (date != null) {
            val time = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(date)
            return time.millis
        }
        return -1
    }

    fun getTime(date: Date?): Long {
        var time: Long = -1
        if (date != null) {
            time = date.time
        }
        return time
    }
}
