package deadpixel.app.vapor;

import android.preference.PreferenceManager;
import android.util.Log;

import deadpixel.app.vapor.cloudapp.api.CloudApp;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.database.model.DatabaseItem;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 8/28/2014.
 */
public class MenuHandler {

    private static final String TAG = "MenuHandler";

    public static String getLink(CloudAppItem item) {

        final CloudAppItem cloudAppItem = item;

        String link;

        String s = PreferenceManager.getDefaultSharedPreferences(AppUtils.getInstance()
                .getApplicationContext()).getString(AppUtils.getInstance().getApplicationContext()
                .getResources().getString(R.string.pref_key_sharable_url), "0");

        int i = Integer.valueOf(s);

        switch (i) {
            case 0:
                link = getWebLink(item);
                break;
            case 1:
                link = cloudAppItem.getDownloadUrl();
                break;
            case 2:
                link = cloudAppItem.getUrl();
                break;
            default:
                link = cloudAppItem.getUrl();
                break;
        }

        return link;

    }

    public static String getWebLink(CloudAppItem item) {
        if(item.getItemType() == CloudAppItem.Type.BOOKMARK) {
            return item.getContentUrl();
        } else {
            String cleanedLink = new StringBuilder(item.getContentUrl().trim()).reverse().toString();
            int firstSlashIndex = cleanedLink.indexOf("/");
            String fileNameSubstring = cleanedLink.subSequence(0, firstSlashIndex).toString();

            return  new StringBuilder(cleanedLink.replace(fileNameSubstring, "")).reverse().toString();
        }
    }
}
