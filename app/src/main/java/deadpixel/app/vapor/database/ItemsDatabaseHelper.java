package deadpixel.app.vapor.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Tevin on 7/27/2014.
 */
public class ItemsDatabaseHelper extends SQLiteOpenHelper{

    public static final String TABLE_ITEMS = "items";

    private static final String DATABASE_NAME = "items.db";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_COL_ROW_ID = "_id";
    public static final String KEY_COL_ITEM_ID = "item_id";
    public static final String KEY_COL_HREF = "href";
    public static final String KEY_COL_NAME = "name";
    public static final String KEY_COL_PRIVATE = "isPrivate";
    public static final String KEY_COL_SUBSCRIBED = "isSubscribed";
    public static final String KEY_COL_CONTENT_URL = "contentURL";
    public static final String KEY_COL_ITEM_TYPE = "item_type";
    public static final String KEY_COL_VIEW_COUNTER = "view_count";
    public static final String KEY_COL_ICON = "icon";
    public static final String KEY_COL_URL = "url";
    public static final String KEY_COL_THUMBNAIL_URL = "thumbnail_url";
    public static final String KEY_COL_REMOTE_URL = "remote_url";
    public static final String KEY_COL_DOWNLOAD_URL = "download_url";
    public static final String KEY_COL_SOURCE = "source";
    public static final String KEY_COL_CREATED_AT = "created_at";
    public static final String KEY_COL_UPDATED_AT = "updated_at";
    public static final String KEY_COL_DELETED_AT = "deleted_at";
    public static final String KEY_COL_LAST_VIEWED_AT = "last_viewed_at";

    public static final String KEY_COL_URI = "uri";
    public static final String KEY_COL_SYNCED_TO_DISK = "is_synced_to_disk";

    public static final String[] ALL_KEYS = {
            KEY_COL_ITEM_ID,
            KEY_COL_DELETED_AT,
            KEY_COL_CONTENT_URL,
            KEY_COL_CREATED_AT,
            KEY_COL_DOWNLOAD_URL,
            KEY_COL_HREF,
            KEY_COL_ICON,
            KEY_COL_ROW_ID,
            KEY_COL_ITEM_TYPE,
            KEY_COL_LAST_VIEWED_AT,
            KEY_COL_NAME,
            KEY_COL_PRIVATE,
            KEY_COL_REMOTE_URL,
            KEY_COL_SOURCE,
            KEY_COL_SUBSCRIBED,
            KEY_COL_SYNCED_TO_DISK,
            KEY_COL_UPDATED_AT,
            KEY_COL_URI,
            KEY_COL_URL,
            KEY_COL_THUMBNAIL_URL,
            KEY_COL_VIEW_COUNTER

    };

    public static final int ID_COL_ID = 0;
    public static final int ID_COL_HREF = 1;
    public static final int ID_COL_NAME = 2;
    public static final int ID_COL_ITEM_ID = 3;
    public static final int ID_COL_PRIVATE = 4;
    public static final int ID_COL_SUBSCRIBED = 5;
    public static final int ID_COL_CONTENT_URL = 6;
    public static final int ID_COL_ITEM_TYPE = 7;
    public static final int ID_COL_VIEW_COUNTER = 8;
    public static final int ID_COL_ICON = 9;
    public static final int ID_COL_URL = 10;
    public static final int ID_COL_REMOTE_URL = 11;
    public static final int ID_COL_DOWNLOAD_URL = 12;
    public static final int ID_COL_SOURCE = 13;
    public static final int ID_COL_CREATED_AT = 14;
    public static final int ID_COL_UPDATED_AT = 15;
    public static final int ID_COL_DELETED_AT = 16;
    public static final int ID_COL_LAST_VIEWED_AT = 17;

    public static final int ID_COL_URI = 19;
    public static final int ID_COL_SYNCED_TO_DISK = 20;




    public ItemsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String DATABASE_CREATE = "create table "
            + TABLE_ITEMS + "(" +
            KEY_COL_ROW_ID + " integer primary key autoincrement, " +
            KEY_COL_HREF + " text, " +
            KEY_COL_NAME + " text, " +
            KEY_COL_ITEM_ID + " integer, " +
            KEY_COL_PRIVATE + " text, " +
            KEY_COL_SUBSCRIBED + " integer, " +
            KEY_COL_ITEM_TYPE + " text, " +
            KEY_COL_VIEW_COUNTER + " integer, " +
            KEY_COL_ICON + " text, " +
            KEY_COL_URL + " text, " +
            KEY_COL_THUMBNAIL_URL + " text, " +
            KEY_COL_REMOTE_URL + " text, " +
            KEY_COL_CONTENT_URL + " text, " +
            KEY_COL_DOWNLOAD_URL + " text, " +
            KEY_COL_SOURCE + " text, " +
            KEY_COL_CREATED_AT + " text, " +
            KEY_COL_UPDATED_AT + " text, " +
            KEY_COL_DELETED_AT + " text, " +
            KEY_COL_LAST_VIEWED_AT + " text, " +

            KEY_COL_URI + " text, " +
            KEY_COL_SYNCED_TO_DISK + " integer not null);";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ItemsDatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }
}
