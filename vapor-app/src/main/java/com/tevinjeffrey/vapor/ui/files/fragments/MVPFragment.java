package com.tevinjeffrey.vapor.ui.files.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.tevinjeffrey.vapor.ui.base.Presenter;
import com.tevinjeffrey.vapor.ui.base.View;
import com.tevinjeffrey.vapor.ui.files.FilesActivity;

import butterknife.ButterKnife;
import icepick.Icepick;

public abstract class MVPFragment extends Fragment implements View {

    Presenter<View> mPresenter;

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


    FilesActivity getParentActivity() {
        return (FilesActivity) getActivity();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
