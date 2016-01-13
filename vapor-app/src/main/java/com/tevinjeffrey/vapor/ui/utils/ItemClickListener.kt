package com.tevinjeffrey.vapor.ui.utils

interface ItemClickListener<D, V> {
    fun onItemClicked(data: D, view: V)
}
