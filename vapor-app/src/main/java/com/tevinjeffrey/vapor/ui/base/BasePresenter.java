package com.tevinjeffrey.vapor.ui.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

//Responsible for attaching and detaching the view to the presentor
public abstract class BasePresenter<V extends View> implements Presenter<V> {

    @Nullable
    //I admit this was a bit premature. The WeakReference holds the view to avoid leaking a
    // reference to it.
    private WeakReference<V> mBaseView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    }

    @Override
    public void onDestroyView(boolean retainedState) {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Nullable
    public V getView() {
        if (mBaseView != null)
            return mBaseView.get();
        return null;
    }

    public V attachView(V view) {
        mBaseView = new WeakReference<>(view);
        return mBaseView.get();
    }

    public void detachView() {
        if (mBaseView != null) {
            mBaseView.clear();
        }
    }

    @Override
    public String toString() {
        return "BasePresenter";
    }
}
