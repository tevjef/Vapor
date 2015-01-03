package deadpixel.app.vapor.database;

import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccountStats;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/27/2014.
 */
public class DatabaseManager  {

    private static final String TAG = "DatabaseManager: ";

    private static ItemsDaoImpl itemsDao;

    public static CloudAppAccount getCloudAppAccount() {
        return PreferenceHandler.getAccountDetails();
    }
    public static CloudAppAccountStats getCloudAppAccountStats() {
        return PreferenceHandler.getAccountStats();
    }
    public synchronized static void startDatabaseConnection(){
        if(itemsDao == null) {
            itemsDao = new ItemsDaoImpl(AppUtils.getInstance().getApplicationContext());
            itemsDao.open();
        }
    }

    public synchronized static void closeDatabaseConnection(){

    }
    public static void updateDatabase(CloudAppAccount account) {
        try {
            PreferenceHandler.saveAccountDetails(account);
        } catch (CloudAppException e) {
            Log.e(TAG, "There was an error attempting to save account details");
        } catch (ParseException e) {
            Log.e(TAG, "There was an error attempting to save account details");
        }
    }

    public static void updateDatabase(CloudAppAccountStats stats) {
            PreferenceHandler.saveAccountStats(stats);
    }


    public synchronized static ArrayList<DatabaseItem> updateDatabase(ArrayList<? extends CloudAppItem> items) {
        startDatabaseConnection();

        ArrayList<DatabaseItem> dbItems = prepareDatabaseItems(items);

        dbItems = itemsDao.insertItems(dbItems);

        PreferenceHandler.updateFilesInserted(dbItems.size());

        closeDatabaseConnection();
        return dbItems;
    }



    public synchronized static ArrayList<DatabaseItem> prepareDatabaseItems(ArrayList<? extends CloudAppItem> items) {

        ArrayList<DatabaseItem> dbItems = new ArrayList<DatabaseItem>();

        for(CloudAppItem item : items) {
            DatabaseItem dbItem = prepareDatabaseItem(item);

            if(dbItem != null){
                dbItems.add(dbItem);
            }
        }

        return dbItems;
    }

    public synchronized static DatabaseItem prepareDatabaseItem(CloudAppItem item) {


        DatabaseItem newDbItem = DatabaseItem.toDatabaseItem(item);
        DatabaseItem existingItem = itemsDao.getItem(item);

        if(existingItem == null && newDbItem.getDeletedAt() == null) {
            newDbItem.setSyncedToDisk(0);
            newDbItem.setUri(null);
            return newDbItem;
        } else if(newDbItem.getDeletedAt() != null) {
            if(existingItem != null) {
                deleteItem(existingItem);
            }
            return null;
        }
        else {
            updateItem(existingItem);
            return null;
        }
    }

    private static void updateItem(DatabaseItem item) {

        startDatabaseConnection();

        itemsDao.updateItem(item);
        Log.e(TAG, "Updating existing item" + item.getName() + " " + item.getItemType()
                +" found while preparing List<DatabaseItem> for insertion");
        closeDatabaseConnection();
    }

    public static ArrayList<DatabaseItem> getItems(CloudAppItem.Type type, int limit) {

        startDatabaseConnection();

        ArrayList<DatabaseItem> dbItems = itemsDao.getItems(type, limit);

        Log.i(TAG, "Got " + dbItems.size() + " from the cursor");

        closeDatabaseConnection();
        return dbItems;
    }

    public static ArrayList<DatabaseItem> getItemsByName(String name) {

        startDatabaseConnection();

        ArrayList<DatabaseItem> dbItems = itemsDao.getItemsByName(name);

        Log.i(TAG, "Got " + dbItems.size() + " from the cursor");

        closeDatabaseConnection();
        return dbItems;
    }


    public static boolean deleteItem(DatabaseItem item) {

        startDatabaseConnection();

        int rowsAffected = itemsDao.deleteItem(item);

        boolean b = false;

        if(rowsAffected > 0) {
            b = true;
        }

        closeDatabaseConnection();
        return b;
    }

    public static int getDbSize() {

        startDatabaseConnection();

        int i = itemsDao.getItems(CloudAppItem.Type.ALL, 0).size();

        closeDatabaseConnection();
        return i;
    }

}
