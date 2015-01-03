package deadpixel.app.vapor.database;

import android.os.AsyncTask;

import java.util.ArrayList;

import deadpixel.app.vapor.callbacks.DatabaseUpdateEvent;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 8/31/2014.
 */
public class DatabaseUpdate extends AsyncTask<ArrayList<? extends CloudAppItem>, Void, ArrayList<DatabaseItem>> {
    public void start(ArrayList<? extends CloudAppItem>...params) {
        DatabaseUpdate.this.execute(params);
    }
    @Override
    protected ArrayList<DatabaseItem> doInBackground(ArrayList<? extends CloudAppItem>... params) {
        ArrayList<DatabaseItem> items = DatabaseManager.updateDatabase(params[0]);
        return items;
    }

    @Override
    protected void onPostExecute(ArrayList<DatabaseItem> databaseItems) {
        super.onPostExecute(databaseItems);
        AppUtils.getEventBus().post(new DatabaseUpdateEvent(databaseItems));
    }
}
