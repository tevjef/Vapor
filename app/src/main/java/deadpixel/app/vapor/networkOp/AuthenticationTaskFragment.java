package deadpixel.app.vapor.networkOp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.squareup.otto.Subscribe;

import deadpixel.app.vapor.callbacks.AccountStatsUpdateEvent;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.database.DatabaseManager;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/27/2014.
 */
public class AuthenticationTaskFragment extends Fragment {

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    public static interface TaskCallbacks {
        void onAccountUpdate(CloudAppAccount account);
        void onServerResponse(ResponseEvent event);
        void onErrorEvent(ErrorEvent event);
    }

    public TaskCallbacks mCallbacks;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        AppUtils.getEventBus().register(ResponseHandler);
        // Create and execute the background task.
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    private Object ResponseHandler = new Object() {

        @Subscribe
        public void onAccountStatsUpdate(AccountStatsUpdateEvent event) {
            if(mCallbacks != null) {
                mCallbacks.onAccountUpdate(DatabaseManager.getCloudAppAccount());
            }
        }
        @Subscribe
        public void onErrorEvent(ErrorEvent event) {
            if(mCallbacks != null) {
                mCallbacks.onErrorEvent(event);
            }
        }
        @Subscribe
        public void onResponseEvent(ResponseEvent event) {
            if(mCallbacks != null) {
                mCallbacks.onServerResponse(event);
            }
        }
    };

}
