package com.tevinjeffrey.vapor.ui.files.fragments

import android.os.Bundle
import android.support.v4.app.Fragment

import com.tevinjeffrey.vapor.ui.base.Presenter
import com.tevinjeffrey.vapor.ui.base.View
import com.tevinjeffrey.vapor.ui.files.FilesActivity

import butterknife.ButterKnife
import com.tevinjeffrey.vapor.ui.base.BasePresenter
import com.tevinjeffrey.vapor.ui.files.FilesActivityPresenterImpl
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.FilesPresenter
import icepick.Icepick

abstract class MVPFragment : Fragment(), View {

    var mPresenter: FilesPresenter<FilesFragmentView>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Icepick.restoreInstanceState(this, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (mPresenter != null) {
            mPresenter!!.onResume()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle) {
        super.onActivityCreated(savedInstanceState)
        if (mPresenter != null) {
            mPresenter!!.onActivityCreated(savedInstanceState)
        }
    }

    override fun onPause() {
        super.onPause()
        if (mPresenter != null) {
            mPresenter!!.onPause()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        ButterKnife.unbind(this)
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }


    val parentActivity: FilesActivity
        get() = activity as FilesActivity

    override fun toString(): String {
        return this.javaClass.simpleName
    }

}
