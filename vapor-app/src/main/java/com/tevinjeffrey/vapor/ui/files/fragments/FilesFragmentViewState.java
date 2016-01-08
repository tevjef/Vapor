package com.tevinjeffrey.vapor.ui.files.fragments;

import android.os.Parcel;

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.ui.base.BaseViewState;
import com.tevinjeffrey.vapor.ui.base.View;

import java.util.ArrayList;
import java.util.List;

public class FilesFragmentViewState extends BaseViewState<FilesFragmentView> {
    public boolean isRefreshing = false;
    public View.LayoutType layoutType = View.LayoutType.LIST;
    public List<CloudAppItem> data = new ArrayList<>(100);
    public String errorMessage;
    @Override
    public void apply(FilesFragmentView view, boolean retainedState) {
        view.initRecyclerView();
        view.initSwipeLayout();
        if (retainedState) {
            view.showLoading(isRefreshing);
            view.showLayout(layoutType);
            if (data.size() != 0) {
                view.setData(data);
            }
            if ((layoutType == View.LayoutType.ERROR)
                    && errorMessage != null)
                view.showError(new Exception(errorMessage));

        }
    }

    public FilesFragmentViewState() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(isRefreshing ? (byte) 1 : (byte) 0);
        dest.writeInt(this.layoutType == null ? -1 : this.layoutType.ordinal());
        dest.writeTypedList(data);
        dest.writeString(this.errorMessage);
    }

    protected FilesFragmentViewState(Parcel in) {
        this.isRefreshing = in.readByte() != 0;
        int tmpLayoutType = in.readInt();
        this.layoutType = tmpLayoutType == -1 ? null : View.LayoutType.values()[tmpLayoutType];
        this.data = in.createTypedArrayList(CloudAppItem.CREATOR);
        this.errorMessage = in.readString();
    }

    public static final Creator<FilesFragmentViewState> CREATOR = new Creator<FilesFragmentViewState>() {
        public FilesFragmentViewState createFromParcel(Parcel source) {
            return new FilesFragmentViewState(source);
        }

        public FilesFragmentViewState[] newArray(int size) {
            return new FilesFragmentViewState[size];
        }
    };
}
