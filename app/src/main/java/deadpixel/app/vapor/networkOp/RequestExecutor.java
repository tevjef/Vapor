package deadpixel.app.vapor.networkOp;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ObserverCollection;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.utils.AppUtils;

/**
 * Created by Tevin on 6/7/14.
 */
public class RequestExecutor implements  Response.Listener<NetworkResponse>, Response.ErrorListener{

    private static final String TAG = "RequestExecutor" ;
    private RequestResponseListener l;
    int expectedCode;

    public RequestExecutor() {
    }
    private Request executeRequest(int method, String body, String URL, final int expectedCode) throws CloudAppException {

        this.expectedCode = expectedCode;

        Request req = new ResponseParser(method, URL, body, this, this);

        return req;

    }

    public Request executeGet(String URL, int expectedCode) throws CloudAppException {
        return executeRequest(Request.Method.GET, null, URL, expectedCode);
    }

    public Request executeDelete(String URL, int expectedCode) throws CloudAppException {
        return executeRequest(Request.Method.DELETE, null, URL, expectedCode);
    }

    public Request executePost(String URL, String body, int expectedCode)
            throws CloudAppException {
        return executeRequest(Request.Method.POST, body, URL, expectedCode);
    }

    public Request executePut(String URL, String body, int expectedCode)
            throws CloudAppException {

        return executeRequest(Request.Method.PUT, body, URL, expectedCode);
    }

 //   public class AsyncUpload extends AsyncTask<S3Upload, Progress, >

    public void setListener(RequestResponseListener l) {
        this.l = l;
    }

    @Override
    public void onResponse(NetworkResponse response) {
        //Checks the network response code against the expected code.
        // If it fails an exception is thrown
        int statusCode = response.statusCode;
        String stringResponse = ResponseParser.getJson(response);

        if (statusCode == expectedCode || statusCode == HttpStatus.SC_NOT_MODIFIED) {



            if(statusCode == HttpStatus.SC_NOT_MODIFIED) {
                Log.e(TAG, "Response taken from cache");
            }

            if(l!=null)
                l.OnSuccessResponse(ResponseParser.getJson(response));


            AppUtils.getEventBus().post(new ResponseEvent(stringResponse));

            Log.i(TAG + "Response: ",stringResponse);
        } else {


            Log.e("RequestExecutor: ", "Unexpected status code " + Integer.toString(statusCode) + stringResponse);
            AppUtils.getEventBus().post(new ErrorEvent(new VolleyError(response), "Unexpected status code", statusCode));

        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {

        Log.e("RequestExecutor: ", "Error response: " + volleyError.toString());

        if( l != null)
            l.OnErrorResponse(volleyError);

        AppUtils.getEventBus().post(new ErrorEvent(volleyError, volleyError.toString()));
    }

    public interface RequestResponseListener {

        public void OnSuccessResponse(String response);

        public void OnErrorResponse(VolleyError errorResponse);
    }



}
