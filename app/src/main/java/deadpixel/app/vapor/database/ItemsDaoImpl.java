package deadpixel.app.vapor.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.database.model.ItemsDaoModel;

/**
 * Created by Tevin on 7/27/2014.
 */
public class ItemsDaoImpl implements ItemsDaoModel{

    private static final String TAG = "ItemsDaoImpl ";


    private SQLiteDatabase database;
    private SQLiteOpenHelper dbHelper;

    public ItemsDaoImpl(Context context) {
        dbHelper = new ItemsDatabaseHelper(context);
    }

    public void open() {
        if(database == null) {
            database = dbHelper.getWritableDatabase();
        }

    }

    public  void close() {
        if(database.isOpen())
            dbHelper.close();
    }

    private ContentValues putValues(DatabaseItem item)  {
        ContentValues values = new ContentValues();
        values.put(ItemsDatabaseHelper.KEY_COL_HREF, item.getHref());
        values.put(ItemsDatabaseHelper.KEY_COL_NAME, item.getName());
        values.put(ItemsDatabaseHelper.KEY_COL_ITEM_ID, item.getId());
        values.put(ItemsDatabaseHelper.KEY_COL_PRIVATE, item.getPrivacy());
        values.put(ItemsDatabaseHelper.KEY_COL_SUBSCRIBED, item.getSubscribed());
        values.put(ItemsDatabaseHelper.KEY_COL_CONTENT_URL, item.getContentUrl());
        values.put(ItemsDatabaseHelper.KEY_COL_ITEM_TYPE, item.getItemType().toString().toUpperCase());
        values.put(ItemsDatabaseHelper.KEY_COL_VIEW_COUNTER, item.getViewCounter());
        values.put(ItemsDatabaseHelper.KEY_COL_ICON, item.getIcon());
        values.put(ItemsDatabaseHelper.KEY_COL_URL, item.getUrl());
        values.put(ItemsDatabaseHelper.KEY_COL_THUMBNAIL_URL, item.getThumbnailUrl());
        values.put(ItemsDatabaseHelper.KEY_COL_REMOTE_URL, item.getRemoteUrl());
        values.put(ItemsDatabaseHelper.KEY_COL_DOWNLOAD_URL, item.getDownloadUrl());
        values.put(ItemsDatabaseHelper.KEY_COL_SOURCE, item.getSource());
        values.put(ItemsDatabaseHelper.KEY_COL_CREATED_AT, item.getCreatedAt());
        values.put(ItemsDatabaseHelper.KEY_COL_UPDATED_AT, item.getUpdatedAt());
        values.put(ItemsDatabaseHelper.KEY_COL_DELETED_AT, item.getDeletedAt());
        values.put(ItemsDatabaseHelper.KEY_COL_LAST_VIEWED_AT, item.getLastViewedAt());

        values.put(ItemsDatabaseHelper.KEY_COL_URI, item.getUri());
        values.put(ItemsDatabaseHelper.KEY_COL_SYNCED_TO_DISK, item.isSyncedToDisk());

        return values;
    }

    public long getCount() {
        return getItems(CloudAppItem.Type.ALL, 0).size();
    }

    private DatabaseItem getItemFromCursor(Cursor cursor) {

        DatabaseItem item = new DatabaseItem();

        item.setHref(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_HREF)));
        item.setName(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_NAME)));
        item.setId(cursor.getLong(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_ITEM_ID)));
        item.setPrivacy(cursor.getInt(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_PRIVATE)));
        item.setSubscribed(cursor.getInt(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_SUBSCRIBED)));
        item.setContentUrl(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_CONTENT_URL)));
        item.setItemType(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_ITEM_TYPE)));
        item.setViewCounter(cursor.getLong(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_VIEW_COUNTER)));
        item.setIcon(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_ICON)));
        item.setUrl(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_URL)));
        item.setThumbnailUrl(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_THUMBNAIL_URL)));
        item.setRemoteUrl(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_REMOTE_URL)));
        item.setDownloadUrl(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_DOWNLOAD_URL)));
        item.setSource(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_SOURCE)));
        item.setEpochCreatedAt(cursor.getLong(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_CREATED_AT)));
        item.setEpochUpdatedAt(cursor.getLong(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_UPDATED_AT)));
        item.setEpochDeletedAt(cursor.getLong(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_DELETED_AT)));
        item.setEpochLastViewedAt(cursor.getLong(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_LAST_VIEWED_AT)));

        item.setUri(cursor.getString(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_URI)));
        item.setSyncedToDisk(cursor.getInt(cursor.getColumnIndex(ItemsDatabaseHelper.KEY_COL_SYNCED_TO_DISK)));
        return item;

    }

    @Override
    public ArrayList<DatabaseItem>insertItems(List<DatabaseItem> items) {

        ArrayList<DatabaseItem> dbItems = new ArrayList<DatabaseItem>();

        Log.i(TAG, "Inserting "+ items.size() + " new items");
        for(DatabaseItem item : items) {
            ContentValues values = putValues(item);
            //insert items then get them by their row Id. Then add it to dbItem and return it.

            dbItems.add(getRow(database.insert(ItemsDatabaseHelper.TABLE_ITEMS, null, values)));
        }
        if(dbItems.size() > 0) {
            FilesManager.increaseDbSize(dbItems.size());
        }
        return dbItems;

    }

    @Override
    public DatabaseItem getRow(long rowId) {

        String where = ItemsDatabaseHelper.KEY_COL_ROW_ID + "=" + rowId;

        Cursor cursor = database.query(true, ItemsDatabaseHelper.TABLE_ITEMS, ItemsDatabaseHelper.ALL_KEYS, where, null,null,null,null,null);

        DatabaseItem item = null;
        if(cursor != null) {
            cursor.moveToFirst();
            item = getItemFromCursor(cursor);
        } else {
            Log.i(TAG, "Item does not exist yet");
        }

        return item == null? null:item;
    }



    @Override
    public DatabaseItem getItem(CloudAppItem item) {

        String where = ItemsDatabaseHelper.KEY_COL_ITEM_ID + "=" + item.getId();

        Cursor cursor = database.query(ItemsDatabaseHelper.TABLE_ITEMS, ItemsDatabaseHelper.ALL_KEYS, where, null,null,null,null,null);

        DatabaseItem dbItem = null;

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();

            dbItem = getItemFromCursor(cursor);

        } else {
            Log.i(TAG, "Item does not exist yet");
        }



        return dbItem == null? null: dbItem;
    }



    @Override
    public ArrayList<DatabaseItem> getItemsByType(CloudAppItem.Type type) {
        return getItems(type, 0);
    }

    public ArrayList<DatabaseItem> getItemsByName(String name) {

        String where = ItemsDatabaseHelper.KEY_COL_NAME + " LIKE " + " ? "
                + " AND " + ItemsDatabaseHelper.KEY_COL_DELETED_AT + " IS NULL ";

        String[] selectionArgs = {"%" + name + "%"};
        String order = ItemsDatabaseHelper.KEY_COL_CREATED_AT  + " DESC ";

        Cursor cursor = database.query(true, ItemsDatabaseHelper.TABLE_ITEMS,
                ItemsDatabaseHelper.ALL_KEYS, where , selectionArgs, null, null, order, null);


        ArrayList<DatabaseItem> items = null;

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();

            items = new ArrayList<DatabaseItem>();

            while(!cursor.isAfterLast()) {
                items.add(getItemFromCursor(cursor));
                cursor.moveToNext();
            }

        } else {
            Log.e(TAG, "Cursor is empty, no items in database");
            items = new ArrayList<DatabaseItem>();
        }

        return items;
    }
    @Override
    public ArrayList<DatabaseItem> getItems(CloudAppItem.Type type, int limit) {

        String where;
        String[] whereArgs;

        if(type == CloudAppItem.Type.ALL) {
            where =  ItemsDatabaseHelper.KEY_COL_DELETED_AT + " IS NULL ";
            whereArgs = null;
        } else if (type == CloudAppItem.Type.DELETED) {
            where = ItemsDatabaseHelper.KEY_COL_DELETED_AT + " IS NOT NULL ";
            whereArgs = null;
        } else {
            where = ItemsDatabaseHelper.KEY_COL_ITEM_TYPE + " = " + "'" + type.toString().toUpperCase()
                    + "'" + " AND " + ItemsDatabaseHelper.KEY_COL_DELETED_AT + " IS NULL ";
            whereArgs = new String[] {"ItemsDatabaseHelper.KEY_COL_DELETED_AT" + " IS NOT NULL "};
        }


        Integer l  = limit;

        if(limit < 1) {
            l = null;
        }

        String order = ItemsDatabaseHelper.KEY_COL_CREATED_AT  + " DESC ";

        Cursor cursor = database.query(true, ItemsDatabaseHelper.TABLE_ITEMS,
                ItemsDatabaseHelper.ALL_KEYS, where , null, null, null, order, l == null? null: l.toString());


        ArrayList<DatabaseItem> items = null;

        if(cursor.getCount() != 0) {
            cursor.moveToFirst();

            items = new ArrayList<DatabaseItem>();

            while(!cursor.isAfterLast()) {
                items.add(getItemFromCursor(cursor));
                cursor.moveToNext();
            }

        } else {
            Log.e(TAG, "Cursor is empty, no items in database");
            items = new ArrayList<DatabaseItem>();
        }

        return items;
    }



    @Override
    public int updateItem(DatabaseItem item) {

        String where = ItemsDatabaseHelper.KEY_COL_ITEM_ID + "=" + item.getId();

        int rowsAffected = database.update(ItemsDatabaseHelper.TABLE_ITEMS, putValues(item),where, null);


        return rowsAffected;
    }


    @Override
    public int deleteItem(DatabaseItem item) {

        String where = ItemsDatabaseHelper.KEY_COL_ITEM_ID + "=" + item.getId();

        int rowsAffected = database.update(ItemsDatabaseHelper.TABLE_ITEMS, putValues(item),where, null);
        FilesManager.decreaseDbSize(rowsAffected * -1);

        return rowsAffected;
    }


    @Override
    public int deleteAllRows() {

        int rowsAffected = database.delete(ItemsDatabaseHelper.TABLE_ITEMS, null, null);

        return rowsAffected;
    }
}
