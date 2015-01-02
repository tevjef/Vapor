package deadpixel.app.vapor.services;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;

import deadpixel.app.vapor.AuthenticationActivity;
import deadpixel.app.vapor.utils.AppUtils;


/**
 * Created by Tevin on 8/29/2014.
 */
public class UploadActivity extends SherlockActivity{

    final static String EXTRA_TEMP_INTENT = "intent";
    final static int REQUEST_CODE = 17;

    private static final String TAG = "UploadActivity ";
    Intent mIntent;
    String mAction;
    String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "UploadActivity started");

        if(AppUtils.isSignedIn()) {
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
        } else {
            Intent launchAuthActivity = new Intent(this, AuthenticationActivity.class);
            launchAuthActivity.putExtra(UploadActivity.EXTRA_TEMP_INTENT, getIntent());
            startActivity(launchAuthActivity);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


    }

    private void handleSendFile(Intent intent) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        File localFile = FileUtils.getFile(this, uri);

        if(localFile == null) {
            Toast.makeText(getApplicationContext(), "This file is not on your device.",
                    Toast.LENGTH_LONG).show();
        } else {
            uri = Uri.fromFile(localFile);


            Intent fileIntent = new Intent(UploadActivity.this, UploadService.class);
            fileIntent.putExtra(AppUtils.EXTRA_FILE_URI, uri);

            String name = localFile.getName();
            String size = String.valueOf(localFile.length());

            fileIntent.putExtra(AppUtils.EXTRA_FILE_NAME, name);
            fileIntent.putExtra(AppUtils.EXTRA_FILE_SIZE, size);

            Log.i(TAG, "Filename: " + name + " File size: " + FileUtils.getReadableFileSize((int) localFile.length()));

            startService(fileIntent);
        }

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