package com.tevinjeffrey.vapor.okcloudapp.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable

import com.orm.SugarRecord
import com.orm.dsl.Unique

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_TIME
import android.text.format.DateUtils.FORMAT_SHOW_YEAR
import android.text.format.DateUtils.formatDateTime

class CloudAppItem : SugarRecord, Comparable<CloudAppItem>, Parcelable {

    @Unique
    var itemId: Long = 0
        private set
    var href: String? = null
        private set
    var name: String? = null
    var isPrivate: Boolean = false
        private set
    var isSubscribed: Boolean = false
        private set
    var contentUrl: String? = null
        private set
    private var itemType: String? = null
    var viewCounter: Long = 0
        private set
    var icon: String? = null
        private set
    private var url: String? = null
    var remoteUrl: String? = null
        private set
    var thumbnailUrl: String? = null
        private set
    var downloadUrl: String? = null
        private set
    var source: String? = null
        private set
    var isFavorite: Boolean = false
        internal set
    var ownerId: String? = null
        internal set
    var contentLength: Long = 0
        internal set
    var createdAt: String? = null
        private set
    var updatedAt: String? = null
        private set
    var deletedAt: String? = null
    var lastViewedAt: String? = null
        private set

    constructor(itemModel: ItemModel) {
        this.itemId = itemModel.id
        this.href = itemModel.href
        this.name = itemModel.name
        this.isPrivate = itemModel.isPrivate
        this.isSubscribed = itemModel.isSubscribed
        this.contentUrl = itemModel.contentUrl
        this.itemType = itemModel.itemType
        this.viewCounter = itemModel.viewCounter
        this.icon = itemModel.icon
        this.url = itemModel.url
        this.remoteUrl = itemModel.remoteUrl
        this.thumbnailUrl = itemModel.thumbnailUrl
        this.downloadUrl = itemModel.downloadUrl
        this.source = itemModel.source
        this.isFavorite = itemModel.favorite
        this.ownerId = itemModel.ownerId
        this.contentLength = itemModel.contentLength
        this.createdAt = formatDate(itemModel.createdAt)
        this.updatedAt = formatDate(itemModel.updatedAt)
        this.deletedAt = formatDate(itemModel.deletedAt)
        this.lastViewedAt = formatDate(itemModel.lastViewedAt)
    }

    constructor() {
    }

    val isTrashed: Boolean
        get() = deletedAt != (-1).toString()

    fun getUrl(): String {
        return remoteUrl?:url?:href!!
    }


    fun getItemType(): ItemType {
        try {
            return ItemType.valueOf(itemType!!.toUpperCase())
        } catch (e: IllegalArgumentException) {
            return ItemType.UNKNOWN
        }

    }

    fun getFormattedCreatedAt(context: Context): String {

        return formatDateTime(context, java.lang.Long.parseLong(createdAt), FORMAT_SHOW_TIME or FORMAT_SHOW_YEAR or FORMAT_SHOW_DATE)
    }

    override fun compareTo(other: CloudAppItem): Int {
        val lhs = this
            if (lhs == other) return 0
            if (lhs.itemId < other.itemId) return 1
            if (lhs.itemId < other.itemId) return -1
        return -1
    }

    enum class ItemType {
        ALL, DELETED, AUDIO, BOOKMARK, IMAGE, UNKNOWN, VIDEO, ARCHIVE, TEXT
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val item = other as CloudAppItem

        return itemId == item.itemId

    }

    fun hash(): Int {
        var result = href!!.hashCode()
        result = 31 * result + createdAt!!.hashCode()
        result = 31 * result + updatedAt!!.hashCode()
        return result
    }

    override fun toString(): String {
        return "CloudAppItem{name='$name', href='$href', itemId=$itemId}"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.itemId)
        dest.writeString(this.href)
        dest.writeString(this.name)
        dest.writeByte(if (isPrivate) 1.toByte() else 0.toByte())
        dest.writeByte(if (isSubscribed) 1.toByte() else 0.toByte())
        dest.writeString(this.contentUrl)
        dest.writeString(this.itemType)
        dest.writeLong(this.viewCounter)
        dest.writeString(this.icon)
        dest.writeString(this.url)
        dest.writeString(this.remoteUrl)
        dest.writeString(this.thumbnailUrl)
        dest.writeString(this.downloadUrl)
        dest.writeString(this.source)
        dest.writeByte(if (isFavorite) 1.toByte() else 0.toByte())
        dest.writeString(this.ownerId)
        dest.writeLong(this.contentLength)
        dest.writeString(this.createdAt)
        dest.writeString(this.updatedAt)
        dest.writeString(this.deletedAt)
        dest.writeString(this.lastViewedAt)
    }

    protected constructor(`in`: Parcel) {
        this.itemId = `in`.readLong()
        this.href = `in`.readString()
        this.name = `in`.readString()
        this.isPrivate = `in`.readByte().toInt() != 0
        this.isSubscribed = `in`.readByte().toInt() != 0
        this.contentUrl = `in`.readString()
        this.itemType = `in`.readString()
        this.viewCounter = `in`.readLong()
        this.icon = `in`.readString()
        this.url = `in`.readString()
        this.remoteUrl = `in`.readString()
        this.thumbnailUrl = `in`.readString()
        this.downloadUrl = `in`.readString()
        this.source = `in`.readString()
        this.isFavorite = `in`.readByte().toInt() != 0
        this.ownerId = `in`.readString()
        this.contentLength = `in`.readLong()
        this.createdAt = `in`.readString()
        this.updatedAt = `in`.readString()
        this.deletedAt = `in`.readString()
        this.lastViewedAt = `in`.readString()
    }

    companion object {

        private fun formatDate(date: String?): String {
            if (date != null) {
                val time = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(date)
                return time.millis.toString()
            }
            return (-1).toString()
        }

        val CREATOR: Parcelable.Creator<CloudAppItem> = object : Parcelable.Creator<CloudAppItem> {
            override fun createFromParcel(source: Parcel): CloudAppItem {
                return CloudAppItem(source)
            }

            override fun newArray(size: Int): Array<CloudAppItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}