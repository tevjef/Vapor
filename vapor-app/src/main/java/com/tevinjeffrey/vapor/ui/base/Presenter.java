package com.tevinjeffrey.vapor.ui.base;

public interface Presenter<V extends View> extends StatefulPresenter {
    V attachView(V view);
    void detachView();
    V getView();
}
