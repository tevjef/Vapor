package com.tevinjeffrey.vapr.okcloudapp;

import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CloudAppUtils {

    public static final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final DateFormat formatBis = new SimpleDateFormat(
            "yyyy-MM-dd");
    private static final String TAG = "CloudAppUtils";

    public static Date formatDate(String date) {
        try {
            if (date != null) {
                return format.parse(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date");
        }
        return null;
    }

    public static long getTime(@Nullable Date date) {
        long time = -1;
        if (date != null) {
            time = date.getTime();
        }
        return time;
    }
}
