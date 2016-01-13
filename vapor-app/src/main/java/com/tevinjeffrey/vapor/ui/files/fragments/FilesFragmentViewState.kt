package com.tevinjeffrey.vapor.ui.files.fragments

import android.os.Parcel
import android.os.Parcelable

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem
import com.tevinjeffrey.vapor.ui.base.BaseViewState
import com.tevinjeffrey.vapor.ui.base.View

import java.util.ArrayList

class FilesFragmentViewState : BaseViewState<FilesFragmentView> {
    var isRefreshing = false
    var layoutType: View.LayoutType = View.LayoutType.LIST
    var data: List<CloudAppItem> = ArrayList(100)
    var errorMessage: String? = null
    override fun apply(view: FilesFragmentView, retainedState: Boolean) {
        view.initRecyclerView()
        view.initSwipeLayout()
        if (retainedState) {
            view.showLoading(isRefreshing)
            view.showLayout(layoutType)
            if (data.size != 0) {
                view.setData(data)
            }
            if (layoutType == View.LayoutType.ERROR && errorMessage != null)
                view.showError(Exception(errorMessage))

        }
    }

    constructor() {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (isRefreshing) 1.toByte() else 0.toByte())
        dest.writeInt(this.layoutType!!.ordinal)
        dest.writeTypedList(data)
        dest.writeString(this.errorMessage)
    }

    protected constructor(`in`: Parcel) {
        this.isRefreshing = `in`.readByte().toInt() != 0
        val tmpLayoutType = `in`.readInt()
        this.layoutType = View.LayoutType.values()[tmpLayoutType]
        this.data = `in`.createTypedArrayList(CloudAppItem.CREATOR)
        this.errorMessage = `in`.readString()
    }

    companion object {

        val CREATOR: Parcelable.Creator<FilesFragmentViewState> = object : Parcelable.Creator<FilesFragmentViewState> {
            override fun createFromParcel(source: Parcel): FilesFragmentViewState {
                return FilesFragmentViewState(source)
            }

            override fun newArray(size: Int): Array<FilesFragmentViewState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
