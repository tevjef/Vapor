package com.tevinjeffrey.vapor.services;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Patterns;
import android.widget.Toast;

import com.tevinjeffrey.vapor.VaporApp;
import com.tevinjeffrey.vapor.okcloudapp.RefCountManager;
import com.tevinjeffrey.vapor.okcloudapp.UserManager;
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils;
import com.tevinjeffrey.vapor.ui.files.FilesActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class IntentBridge extends Activity {
    public static final String AWAITING_UPLOAD = "com.tevinjeffrey.vapor.services.ACTION_TYPE";
    public final static String FILE_SELECT = "com.tevinjeffrey.vapor.services.FileSelect";
    public final static String FILE_TYPE = "com.tevinjeffrey.vapor.services.FILE_TYPE";

    public final static String FILE_BOOKMARK = "com.tevinjeffrey.vapor.services.FILE_BOOKMARK";
    public final static String FILE_TEXT = "com.tevinjeffrey.vapor.services.FILE_TEXT";
    public final static String FILE_OTHER = "com.tevinjeffrey.vapor.services.FILE_OTHER";

    private static final int FILE_SELECT_CODE = 42;

    @Inject
    RefCountManager refCountManager;
    @Inject
    UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VaporApp.uiComponent(getApplicationContext()).inject(this);

        // If the user is not logged in, but tries to upload a file anyway. This should catch that
        // intent and start the login process instead.
        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, FilesActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Please log in first!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //Check if the required permissions are granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, FilesActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Please enable the required permissions!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent incomingIntent = getIntent();
        // True when the user attempts to select a file from withing the app.
        if (incomingIntent.getAction().equals(FILE_SELECT)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, FILE_SELECT_CODE);
        // True when an external app attempts to SEND data to the app
        } else if(incomingIntent.getAction().equals(Intent.ACTION_SEND)) {
            handleIntent(incomingIntent);
        }

    }

    private void handleIntent(Intent incomingIntent) {
        final Intent outgoingIntent = new Intent(this, UploadService.class);
        // Prepare the Intent to be sent to the UploadService. ACTION_UPLOAD to tell the service we
        // attempting to upload a file.
        outgoingIntent.putExtra(UploadService.ACTION_TYPE, UploadService.ACTION_UPLOAD);

        // Get the type of the file to determine what to do. Text files are treated differently from
        // regular audio, video and other binaries. Text files could be a URL or a UTF-8 string.
        String type = incomingIntent.getType();
        if (type == null) {
            type = FileUtils.getMimeType(this, incomingIntent.getData());
        }
        if (type.equals("text/plain")) {
            outgoingIntent.putExtra(FILE_TYPE, FILE_TEXT);
            CharSequence text = incomingIntent.getStringExtra(Intent.EXTRA_TEXT);
            if (text == null) {
                Timber.e("Error text_extra was null");
                return;
            }
            if (Patterns.WEB_URL.matcher(text).matches()) {
                outgoingIntent.putExtra(FILE_TYPE, FILE_BOOKMARK);
            }
            outgoingIntent.putExtra(Intent.EXTRA_TEXT, text);
            startService(outgoingIntent);
            finish();
        } else {

            // Get the file from either data or EXTRA_STREAM. Depends on entirely on who's sending.
            final Uri fileUri = incomingIntent.getData() == null ? (Uri) incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM) :
                    incomingIntent.getData();
            outgoingIntent.putExtra(FILE_TYPE, FILE_OTHER);

            // Google photos is serious about privacy and does not allow URI's to be passed around
            // component within the app. So we must first write the file publicly to the disk. Then
            // retrieve the URI for that location. We pass the uri to the UploadService, which will
            // alert use when the file a this URI has been successfully uploaded. After the upload
            // complete's we delete the file.
            if (FileUtils.isGooglePhotosUri(fileUri)) {
                Observable.defer(new Func0<Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call() {
                        return Observable.just(getImageUrlWithAuthority(fileUri));
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<Uri>() {
                            @Override
                            public void call(Uri uri) {
                                refCountManager.addUri(uri);
                                outgoingIntent.setData(uri)
                                    .putExtra(Intent.EXTRA_STREAM, uri);
                                startService(outgoingIntent);
                                finish();

                            }
                        });
            // Check to see if the is local to the device as some apps may attempt to serve a file
            // that's on a remote server.
            } else if (FileUtils.isLocal(fileUri.toString())) {
                outgoingIntent.setData(fileUri)
                    .putExtra(Intent.EXTRA_STREAM, fileUri);
                startService(outgoingIntent);
                finish();
            } else {
                Toast.makeText(this, "Vapor cannot upload that file", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE) {
            // User selects a file to upload from the document viewer.
            if (resultCode == RESULT_OK) {
                handleIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    /* http://stackoverflow.com/questions/30527045/choosing-photo-using-new-google-photos-app-is-broken */
    public Uri getImageUrlWithAuthority(Uri uri) {
        InputStream is = null;
        if (uri.getAuthority() != null) {
            try {
                is = this.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                Uri tempFileUri = writeToTempImageAndGetPathUri(bmp);
                return tempFileUri;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Uri writeToTempImageAndGetPathUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
