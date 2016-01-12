package com.tevinjeffrey.vapor.okcloudapp

/**
 * Listener for progress
 */
interface ProgressListener {

    /**
     * When progress is reported
     * @param current The current progress
     * *
     * @param max The max progress amount
     */
    fun onProgress(current: Long, max: Long)
}