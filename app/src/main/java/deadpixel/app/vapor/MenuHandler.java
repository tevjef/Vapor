package deadpixel.app.vapor;

import android.preference.PreferenceManager;

import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 8/28/2014.
 */
public class MenuHandler {

    public static String getLink(DatabaseItem item) {

        final DatabaseItem dbItem = item;

        String link;

        String s = PreferenceManager.getDefaultSharedPreferences(AppUtils.getInstance()
                .getApplicationContext()).getString(AppUtils.getInstance().getApplicationContext()
                .getResources().getString(R.string.pref_key_sharable_url), "0");

        int i = Integer.valueOf(s);

        switch (i) {
            case 0:
                link = dbItem.getContentUrl();
                break;
            case 1:
                link = dbItem.getDownloadUrl();
                break;
            case 2:
                link = dbItem.getUrl();
                break;
            default:
                link = dbItem.getUrl();
                break;
        }

        return link;

    }
}
