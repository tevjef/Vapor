package deadpixel.app.vapor.okcloudapp;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CloudAppUtils {
    protected static final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    protected static final DateFormat formatBis = new SimpleDateFormat(
            "yyyy-MM-dd");
    private static final String TAG = "CloudAppModel";

    public static Date formatDate(String date) {
        try {
            return format.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date");
        }
        return null;
    }
}
