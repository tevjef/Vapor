package deadpixel.app.vapor.callbacks;

import com.android.volley.VolleyError;

import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/8/2014.
 */
public class ErrorEvent {
    String response;
    int statusCode;
    VolleyError error;

    public ErrorEvent(String response) {
        this.response = response;
    }

    public ErrorEvent(VolleyError e, String response) {
        this.response = response;
        error = e;
        if (e != null && e.networkResponse != null) {
            this.statusCode = e.networkResponse.statusCode;
        }
        if(!AppUtils.getInstance().isNetworkConnected()) {
            this.response = AppUtils.NO_CONNECTION;
        }

    }

    public ErrorEvent(VolleyError e, String response, int statusCode) {
        this(e, response);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorDescription() {
        return response;
    }

    public void setErrorDescription(String response) {
        this.response = response;
    }

    public VolleyError getError() {
        return error;
    }

    public void setError(VolleyError error) {
        this.error = error;
    }

}