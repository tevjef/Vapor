package deadpixel.app.vapor.callbacks;

/**
 * Created by Tevin on 7/8/2014.
 */
public interface UploadCallback {
    void onComplete();
    void onError(ErrorEvent event);

}