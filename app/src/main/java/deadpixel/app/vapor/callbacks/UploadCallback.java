package deadpixel.app.vapor.callbacks;

import com.android.volley.VolleyError;

import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/8/2014.
 */
public interface UploadCallback {
    void onComplete();
    void onError(ErrorEvent event);

}