package com.tevinjeffrey.vapor.ui.base

import android.os.Bundle

import java.lang.ref.WeakReference

//Responsible for attaching and detaching the view to the presenter
abstract class BasePresenter<V : View> : Presenter<V> {

    private //I admit this was a bit premature. The WeakReference holds the view to avoid leaking a
            // reference to it.
    var mBaseView: WeakReference<V>? = null

    override var view: V? = null
        get() {
            return mBaseView!!.get()
        }

    override fun onActivityCreated(savedInstanceState: Bundle) {
    }

    override fun onDestroyView(retainedState: Boolean) {
    }

    override fun onSaveInstanceState(bundle: Bundle) {
    }

    override fun onPause() {
    }

    override fun onResume() {
    }

    override fun attachView(view: V): V {
        mBaseView = WeakReference(view)
        return mBaseView!!.get()
    }

    override fun detachView() {
        if (mBaseView != null) {
            mBaseView!!.clear()
        }
    }

    override fun toString(): String {
        return "BasePresenter"
    }
}
