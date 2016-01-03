package com.tevinjeffrey.vapor.okcloudapp;

import android.os.Parcel;
import android.os.Parcelable;

public class DataCursor implements Parcelable {
    public final int ITEM_LIST_LIMIT = 100;
    int offset = 0;

    public int getLimit() {
        return ITEM_LIST_LIMIT;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.offset);
    }

    public DataCursor() {
    }

    protected DataCursor(Parcel in) {
        this.offset = in.readInt();
    }

    public static final Parcelable.Creator<DataCursor> CREATOR = new Parcelable.Creator<DataCursor>() {
        public DataCursor createFromParcel(Parcel source) {
            return new DataCursor(source);
        }

        public DataCursor[] newArray(int size) {
            return new DataCursor[size];
        }
    };
}
