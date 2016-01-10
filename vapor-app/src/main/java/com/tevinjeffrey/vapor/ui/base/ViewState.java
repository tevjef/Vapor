package com.tevinjeffrey.vapor.ui.base;

import android.os.Parcelable;

/**
An interface that defines a class that interacts with a view to restore it's state.
ViewState holds the data and gets parcels it into a bundle whenever the the state is saved
 and needs to be restored.
 */
public interface ViewState<V extends View> extends Parcelable {
    void apply(V view, boolean retainedState);
}
