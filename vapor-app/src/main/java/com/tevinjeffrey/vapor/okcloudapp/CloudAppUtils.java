package com.tevinjeffrey.vapor.okcloudapp;

import android.support.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CloudAppUtils {

    public static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final DateFormat formatBis = new SimpleDateFormat(
            "yyyy-MM-dd");
    private static final String TAG = "CloudAppUtils";

    public static long formatDate(String date) {
        if (date != null) {
            DateTime time = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(date);
            return time.getMillis();
        }
        return -1;
    }

    public static long getTime(@Nullable Date date) {
        long time = -1;
        if (date != null) {
            time = date.getTime();
        }
        return time;
    }
}
