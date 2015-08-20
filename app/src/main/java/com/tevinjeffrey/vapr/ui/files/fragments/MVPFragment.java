package com.tevinjeffrey.vapr.ui.files.fragments;
import android.os.Bundle;
import android.support.v4.app.Fragment;


import com.tevinjeffrey.vapr.ui.base.BasePresenter;
import com.tevinjeffrey.vapr.ui.base.Presenter;
import com.tevinjeffrey.vapr.ui.base.View;
import com.tevinjeffrey.vapr.ui.files.FilesActivity;

import butterknife.ButterKnife;
import dagger.ObjectGraph;
import icepick.Icepick;
import timber.log.Timber;

public abstract class MVPFragment extends Fragment implements View {

    public boolean mIsInitialLoad = true;

    public Presenter<View> mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter != null) {
            mPresenter.onResume();
        }
        mIsInitialLoad = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mPresenter != null) {
            mPresenter.onActivityCreated(savedInstanceState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPresenter != null) {
            mPresenter.onPause();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if (mPresenter != null) {
            mPresenter.detachView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }


    public FilesActivity getParentActivity() {
        return (FilesActivity) getActivity();
    }

    private Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
