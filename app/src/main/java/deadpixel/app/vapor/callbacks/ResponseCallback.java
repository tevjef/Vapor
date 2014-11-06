package deadpixel.app.vapor.callbacks;

import com.android.volley.VolleyError;

/**
 * Created by Tevin on 6/16/2014.
 */
public interface ResponseCallback extends Observer{
    @Override
    public void onServerResponse(String str);

    @Override
    public void onServerError(VolleyError e, String errorDescription);
}