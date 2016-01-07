package com.tevinjeffrey.vapor.services;

import android.app.Activity;
import android.content.Intent;
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

    private static final int LOGIN_CODE = 43;
    private static final int FILE_SELECT_CODE = 42;

    @Inject
    RefCountManager refCountManager;
    @Inject
    UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VaporApp.uiComponent(getApplicationContext()).inject(this);

        if (!userManager.isLoggedIn()) {
            Intent intent = new Intent(this, FilesActivity.class);
            intent.putExtra(AWAITING_UPLOAD, intent);
            startActivityForResult(intent, LOGIN_CODE);
            finish();
        }

        Intent incomingIntent = getIntent();
        if (incomingIntent.getAction().equals(FILE_SELECT)) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 42);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, FILE_SELECT_CODE);
        } else if(incomingIntent.getAction().equals(Intent.ACTION_SEND)) {
            handleIntent(incomingIntent);
        }

    }

    private void handleIntent(Intent incomingIntent) {
        final Intent outgoingIntent = new Intent(this, UploadService.class);
        outgoingIntent.putExtra(UploadService.ACTION_TYPE, UploadService.ACTION_UPLOAD);

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
            dispatchIntent(outgoingIntent);
        } else {

            final Uri fileUri = incomingIntent.getData() == null ? (Uri) incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM) :
                    incomingIntent.getData();
            outgoingIntent.putExtra(FILE_TYPE, FILE_OTHER);

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
                                outgoingIntent.setData(uri);
                                dispatchIntent(outgoingIntent, uri);
                            }
                        });
            } else if (FileUtils.isLocal(fileUri.toString())) {
                outgoingIntent.setData(fileUri);
                dispatchIntent(outgoingIntent, fileUri);
            } else {
                Toast.makeText(this, "Vapor cannot upload that file", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void dispatchIntent(Intent intent) {
        dispatchIntent(intent, null);
    }

    private void dispatchIntent(Intent intent, Uri uri) {
        if (uri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        startService(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE || requestCode == LOGIN_CODE) {
            if (resultCode == RESULT_OK) {
                handleIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

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

    /* https://code.google.com/p/android-developer-preview/issues/detail?id=2353 */
/*
    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }
*/

    public Uri writeToTempImageAndGetPathUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
