package com.tevinjeffrey.vapor.ui.base

interface Presenter<V : View> : StatefulPresenter {
    fun attachView(view: V): V
    fun detachView()
    val view: V?
}
