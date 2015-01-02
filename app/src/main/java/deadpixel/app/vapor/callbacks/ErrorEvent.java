package deadpixel.app.vapor.callbacks;

import android.content.Intent;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import deadpixel.app.vapor.R;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 7/8/2014.
 */
public class ErrorEvent {
    public String getExplicitError() {
        return mExplicitError;
    }

    public void setExplicitError(String mExplicitError) {
        this.mExplicitError = mExplicitError;
    }

    public Object getImplicitError() {
        return mImplicitError;
    }

    public void setImplicitError(Object mImplicitError) {
        this.mImplicitError = mImplicitError;
    }


    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    String mExplicitError;
    Object mImplicitError;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    Integer statusCode;
    Exception error;

    public ErrorEvent(Exception e, String mExplicitError, Object mImplicitError) {
        this.error = e;
        this.mExplicitError = mExplicitError;
        this.mImplicitError = mImplicitError;

        if (e instanceof VolleyError) {
            VolleyError e1 = (VolleyError) e;
            if (e1.networkResponse != null) {
                this.statusCode = e1.networkResponse.statusCode;
            }
        }

        if (!AppUtils.getInstance().isNetworkConnected()) {
            this.mExplicitError = AppUtils.NO_CONNECTION;
        }

    }


    public ErrorEvent(Exception e, String mExplicitError) {
        this(e, mExplicitError, null);
    }

    public ErrorEvent(Exception e) {
        this(e, null);
    }

    public static String getErrorDescription(ErrorEvent error) {
        String errorDescription = null;

        if (error != null) {
            if (error.getExplicitError().equals(AppUtils.NO_CONNECTION)) {
                errorDescription = AppUtils.getInstance().getApplicationContext().getResources().getString(R.string.check_internet);
            }else {
                errorDescription = "An error occured";
            }
        }
        return errorDescription;
    }
}