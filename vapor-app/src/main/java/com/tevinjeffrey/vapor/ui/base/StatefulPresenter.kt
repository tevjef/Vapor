package com.tevinjeffrey.vapor.ui.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup

interface StatefulPresenter {
    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * [android.app.Fragment.setRetainInstance] to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after [android.app.Fragment.onCreateView]
     * and before [android.app.Fragment.onViewStateRestored].

     * @param savedInstanceState If the fragment is being re-created from
     * *                           a previous saved state, this is the state.
     */
    fun onActivityCreated(savedInstanceState: Bundle)

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to [Activity.onPause] of the containing
     * Activity's lifecycle.
     */
    fun onPause()

    fun onResume()

    fun onDestroyView(retainedState: Boolean)

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to [android.app.Fragment.onCreate],
     * [android.app.Fragment.onCreateView], and
     * [.onActivityCreated].
     *
     *
     *
     * This corresponds to [ Activity.onSaveInstanceState(Bundle)][Activity.onSaveInstanceState] and most of the discussion there
     * applies here as well.  Note however: *this method may be called
     * at any time before [android.app.Fragment.onDestroy]*.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.

     * @param bundle Bundle in which to place your saved state.
     */
    fun onSaveInstanceState(bundle: Bundle)
}