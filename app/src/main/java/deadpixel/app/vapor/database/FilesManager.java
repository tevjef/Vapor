package deadpixel.app.vapor.database;

import com.android.volley.Request;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import deadpixel.app.vapor.callbacks.DatabaseUpdateEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.database.PreferenceHandler;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/27/2014.
 */
public class FilesManager {

    static long ITEMS_IN_DATABASE;

    static long TOTAL_ITEMS;

    static long INSERTED_ITEMS;

    static int DEFAULT_FILE_REQUEST_SIZE;

    static int SUCCESSFUL_PAGES;

    static int NEW_PAGE;

    static boolean isNewFilesRequest= false;

/*
    public static void initialize() {
        AppUtils.getEventBus().register(UpdateHandler);
    }
*/


    public static void requestMoreFiles(CloudAppItem.Type type) {
        updateMetrics();
            AppUtils.addToRequestQueue(createRequest(type));
            isNewFilesRequest = true;
    }

    private static Request createRequest(CloudAppItem.Type type) {

        boolean isFullySynced = AppUtils.mPref.getBoolean(AppUtils.FULLY_SYNCED, false);

        if(!isFullySynced) {
            if (TOTAL_ITEMS > DEFAULT_FILE_REQUEST_SIZE * SUCCESSFUL_PAGES) {
                try {
                    return AppUtils.api.getItems(
                            NEW_PAGE,
                            DEFAULT_FILE_REQUEST_SIZE, type, null);
                } catch (CloudAppException e) {
                    e.printStackTrace();
                }
            } else if(TOTAL_ITEMS == 0) {
                try {
                    return AppUtils.api.getItems(
                            1,
                            DEFAULT_FILE_REQUEST_SIZE, type, null);
                } catch (CloudAppException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                return AppUtils.api.getItems(
                        1,
                        DEFAULT_FILE_REQUEST_SIZE, type, null);
            } catch (CloudAppException e) {
                e.printStackTrace();
            }
        }


        return null;
    }

    public static ArrayList<DatabaseItem> getFiles(CloudAppItem.Type type) {
        return DatabaseManager.getItems(type, 0);
    }

    public static void deleteItem(DatabaseItem item) {

        try {
           AppUtils.addToRequestQueue(AppUtils.api.delete(item));
        } catch (CloudAppException e) {
            e.printStackTrace();
        }

    }

    private static void updateMetrics() {
        TOTAL_ITEMS = PreferenceHandler.getAccountStats().getItems();

        INSERTED_ITEMS = PreferenceHandler.getInsertedFilesCount();

        DEFAULT_FILE_REQUEST_SIZE = AppUtils.DEFAULT_FILE_REQUEST_SIZE;

        SUCCESSFUL_PAGES = (int) Math.floor(INSERTED_ITEMS/DEFAULT_FILE_REQUEST_SIZE);

        NEW_PAGE = SUCCESSFUL_PAGES + 1;

    }

    private static void databaseSizeHandler(long size) {
        boolean fullySynced = AppUtils.mPref.getBoolean(AppUtils.FULLY_SYNCED, false);
        int dbSize = DatabaseManager.getDbSize();

        if(size < AppUtils.DEFAULT_FILE_REQUEST_SIZE && !isNewFilesRequest) {
                isNewFilesRequest = false;
                AppUtils.mPref.edit()
                        .putBoolean(AppUtils.FULLY_SYNCED, true)
                        .putLong(AppUtils.CALCULATED_TOTAL_ITEMS, dbSize)
                        .commit();

        }
        if(!fullySynced) {
            long calculatedSize = AppUtils.mPref.getLong(AppUtils.CALCULATED_TOTAL_ITEMS, 0);
            AppUtils.mPref.edit()
                    .putLong(AppUtils.CALCULATED_TOTAL_ITEMS, calculatedSize + size)
                    .commit();
        }
    }

    public static void refreshFiles() {
        updateMetrics();
        try {
            AppUtils.addToRequestQueue(AppUtils.api.getItems(
                    1,
                    DEFAULT_FILE_REQUEST_SIZE, CloudAppItem.Type.ALL, null));
        } catch (CloudAppException e) {
            e.printStackTrace();
        }
    }





/*    private static Object UpdateHandler = new Object() {
        @Subscribe
        public void onDatabaseUpdate(DatabaseUpdateEvent event) {
            databaseSizeHandler(event.getItems());
        }
    };*/

    public static void increaseDbSize(long size) {
        databaseSizeHandler(size);
    }

    public static void decreaseDbSize(long size) {
        databaseSizeHandler(size);
    }
}
