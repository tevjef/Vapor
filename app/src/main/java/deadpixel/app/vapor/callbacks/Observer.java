package deadpixel.app.vapor.callbacks;

import com.android.volley.VolleyError;

/**
 * Created by Tevin on 6/17/2014.
 */
public interface Observer {
    public void onServerError(VolleyError error, String errorDescription);

    public void onServerResponse(String response);


}
