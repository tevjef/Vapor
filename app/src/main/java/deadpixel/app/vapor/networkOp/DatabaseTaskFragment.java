package deadpixel.app.vapor.networkOp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.android.volley.Request;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import deadpixel.app.vapor.MainActivity;
import deadpixel.app.vapor.callbacks.AccountResponseEvent;
import deadpixel.app.vapor.callbacks.AccountStatsResponseEvent;
import deadpixel.app.vapor.callbacks.AccountStatsUpdateEvent;
import deadpixel.app.vapor.callbacks.AccountUpdateEvent;
import deadpixel.app.vapor.callbacks.DatabaseUpdateEvent;
import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ItemResponseEvent;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.database.DatabaseManager;
import deadpixel.app.vapor.database.DatabaseUpdate;
import deadpixel.app.vapor.database.FilesManager;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/27/2014.
 */
public class DatabaseTaskFragment extends SherlockFragment {

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    public static interface TaskCallbacks {
        void onDatabaseUpdate(ArrayList<DatabaseItem> items);
        void onServerResponse(ResponseEvent event);
        void onErrorEvent(ErrorEvent event);
    }

    public TaskCallbacks mCallbacks;
    public MainActivity parentActivity;

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
        parentActivity = (MainActivity)activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppUtils.getEventBus().register(ItemResponseHandler);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);

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
        parentActivity = null;
    }

    public DatabaseUpdate newDatabaseUpdate() {
        return new DatabaseUpdate();
    }

    public class DatabaseUpdate extends AsyncTask<Object, Void, ArrayList<DatabaseItem>>{
        public void start(Object...params) {
            DatabaseUpdate.this.execute(params[0]);
        }
        @Override
        protected ArrayList<DatabaseItem> doInBackground(Object... params) {
            ArrayList<DatabaseItem> items = DatabaseManager.updateDatabase((ArrayList<CloudAppItem>) params[0]);
            return items;
        }

        @Override
        protected void onPostExecute(ArrayList<DatabaseItem> databaseItems) {
            super.onPostExecute(databaseItems);

            AppUtils.getEventBus().post(new DatabaseUpdateEvent(databaseItems));
        }
    }

    private Object ItemResponseHandler = new Object() {
        @Subscribe
        public void onItemResponse(ItemResponseEvent event) {

        }
        @Subscribe
        public void onDatabaseUpdate(DatabaseUpdateEvent event) {
            if(mCallbacks != null) {
                mCallbacks.onDatabaseUpdate(event.getItems());
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
