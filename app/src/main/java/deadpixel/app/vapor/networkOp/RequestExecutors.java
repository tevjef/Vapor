package deadpixel.app.vapor.networkOp;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Map;

import deadpixel.app.vapor.callbacks.ObserverCollection;
import deadpixel.app.vapor.callbacks.ResponseCallback;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;

import org.apache.http.HttpStatus;

/**
 * Created by Tevin on 6/7/14.
 */
public class RequestExecutors extends RequestHandler {

    public static final String MY_CL_LY = "http://my.cl.ly";
    public static final String REGISTER_URL = MY_CL_LY+ "/register";
    public static final String ACCOUNT_URL = MY_CL_LY + "/account";
    public static final String ACCOUNT_STATS_URL = ACCOUNT_URL + "/stats";
    public static final String RESET_URL = MY_CL_LY + "/reset";

    public static ObserverCollection collection = ObserverCollection.getInstance();

    public RequestExecutors() {
    }

    protected void executeRequest(int method, String body, String URL, final int expectedCode) throws CloudAppException {

        Request req = new ResponseParser(method, URL, body,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        //Checks the network response code against the expected code.
                        // If it fails an exception is thrown
                        if (response.statusCode == expectedCode) {

                            try {
                                //Grabs the Json string from the ServerResponse object
                                // and sends it to the callback function

                                collection.notifyObservers(ResponseParser.getJson(response));
                                Log.i("Response: ", ResponseParser.getJson(response));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                throw new CloudAppException(500, "Unexpected response code: "
                                        + Integer.toString(response.statusCode));
                            } catch (CloudAppException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                Log.e("RequestExecutor: ", "Error response: " + volleyError.toString());

                collection.notifyServerError(volleyError, ResponseParser.getError(volleyError.networkResponse));
            }
        }
        );

        RequestHandler.addToRequestQueue(req);
    }

    protected void executeGet(String URL, int expectedCode) throws CloudAppException {
        executeRequest(Request.Method.GET, null, URL, expectedCode);
    }

    protected void executeDelete(String URL, int expectedCode) throws CloudAppException {
        executeRequest(Request.Method.DELETE, null, URL, expectedCode);
    }

    protected void executePost(String URL, String body, int expectedCode)
            throws CloudAppException {
        executeRequest(Request.Method.POST, body, URL, expectedCode);
    }

    protected void executePut(String URL, String body, int expectedCode)
            throws CloudAppException {

        executeRequest(Request.Method.PUT, body, URL, expectedCode);
    }

 //   public class AsyncUpload extends AsyncTask<S3Upload, Progress, >




}
