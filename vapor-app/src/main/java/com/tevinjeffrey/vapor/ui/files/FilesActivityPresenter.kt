package com.tevinjeffrey.vapor.ui.files

import com.tevinjeffrey.vapor.ui.base.Presenter

interface FilesActivityPresenter : Presenter<FilesActivityView> {
    fun loadEmail()
    var navContext: NavContext
    fun refreshClicked()

    enum class NavContext private constructor(private val title: String) {
        ALL("All Recent Files"), POPULAR("Popular"), FAVORITE("Favorites"), TRASH("Trash");

        override fun toString(): String {
            return title
        }
    }
}