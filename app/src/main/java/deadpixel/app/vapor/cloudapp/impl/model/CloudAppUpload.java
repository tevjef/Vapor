package deadpixel.app.vapor.cloudapp.impl.model;

import com.koushikdutta.ion.ProgressCallback;

import java.io.File;

import deadpixel.app.vapor.callbacks.UploadCallback;

/**
 * Created by Tevin on 9/4/2014.
 */
public class CloudAppUpload {
    public File getmFile() {
        return mFile;
    }

    public ProgressCallback getmProgressCallback() {
        return mProgressCallback;
    }

    public UploadCallback getmUploadCallback() {
        return mUploadCallback;
    }

    public int getNotificationId() {
        return notificationId;
    }

    File mFile;
    ProgressCallback mProgressCallback;
    UploadCallback mUploadCallback;

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    int notificationId;

    public CloudAppUpload(File file, ProgressCallback progressCallback, UploadCallback uploadCallback, int uploadNum) {
        mFile = file;
        mProgressCallback = progressCallback;
        mUploadCallback = uploadCallback;
        notificationId = uploadNum;

    }
}
