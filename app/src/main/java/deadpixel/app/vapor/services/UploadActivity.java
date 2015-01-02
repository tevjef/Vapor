package deadpixel.app.vapor.services;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;

import java.io.File;

import deadpixel.app.vapor.utils.AppUtils;


/**
 * Created by Tevin on 8/29/2014.
 */
public class UploadActivity extends SherlockActivity{

    private static final String TAG = "UploadActivity ";
    Intent mIntent;
    String mAction;
    String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "UploadActivity started");

        mIntent = getIntent();
        mAction = mIntent.getAction();
        mType = mIntent.getType();

        if(mAction.equals(Intent.ACTION_SEND) && mType != null) {
            if("text/plain".equals(mType)) {
                handleSendPlainText(mIntent);
            } else {
                handleSendFile(mIntent);
            }
        }
    }

    private void handleSendFile(Intent intent) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        Intent fileIntent = new Intent(UploadActivity.this, UploadService.class);
        fileIntent.putExtra(AppUtils.EXTRA_FILE_URI, uri);
        fileIntent.putExtra(AppUtils.EXTRA_FILE_NAME, uri);



        File file = new File(uri.getPath());
        String name = file.getName();
        String size = String.valueOf(file.length());

        fileIntent.putExtra(AppUtils.EXTRA_FILE_NAME, name);
        fileIntent.putExtra(AppUtils.EXTRA_FILE_SIZE, size);


        Log.i(TAG, "Filename: " + name + " File size: " + size);

        startService(fileIntent);
        finish();
    }

    private void handleSendPlainText(Intent intent) {

        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if(uri!= null) {
            handleSendFile(intent);
        } else {
            String name = intent.getStringExtra(AppUtils.EXTRA_BOOKMARK_NAME);
            String url = intent.getStringExtra(Intent.EXTRA_TEXT);

            Intent bookMarkIntent = new Intent(UploadActivity.this, UploadService.class);
            bookMarkIntent.putExtra(AppUtils.EXTRA_BOOKMARK_NAME, name);
            bookMarkIntent.putExtra(AppUtils.EXTRA_BOOKMARK_URL, url);

            Log.i(TAG, "Handling plain text");

            startService(bookMarkIntent);

            finish();
        }

    }
}
