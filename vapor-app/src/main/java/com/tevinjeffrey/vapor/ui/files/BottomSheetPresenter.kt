package com.tevinjeffrey.vapor.ui.files

interface BottomSheetPresenter {
    fun downloadFile()
    fun renameFile(newName: String)
    fun deleteFile()
}
