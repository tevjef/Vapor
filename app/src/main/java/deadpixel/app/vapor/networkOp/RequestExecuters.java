package deadpixel.app.vapor.networkOp;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.impl.model.ItemResponseModel;

/**
 * Created by Tevin on 6/7/14.
 */
public class RequestExecuters extends RequestHandler {
    private final String TAG = "deadpixel.app.vapor.cloudapp.impl.CloudAppBase";

    protected static final String MY_CL_LY = "http://my.cl.ly";

    public static ResponseCallbacks rCallbacks;

    /**
     * Executes a GET to a url with a certain body.
     *
     * @param url
     * @return
     * @throws deadpixel.app.vapor.cloudapp.api.CloudAppException
     */


    protected void executeGet(String URL, final int expectedCode) throws CloudAppException {
        //RESTful GET Json request to the server.
        Request req = new ResponseParser(Request.Method.GET, URL, null,
                new Response.Listener<ServerResponse>() {

                    @Override
                    public void onResponse(ServerResponse response) {
                        //Checks the network response code against the expected code.
                        // If it fails an exception is thrown
                        if(response.getNetworkResponseCode() == expectedCode) {

                            try {
                                //Grabs the Json string from the ServerResponse object
                                // and sends it to the callback function
                                rCallbacks.serverResponse(response.getJsonString());
                                Log.i("Response: ", response.getJsonString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                throw new CloudAppException(500, "Unexpected response code");
                            } catch (CloudAppException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }
                rCallbacks.serverErrorResponse(volleyError);
            }
        });
        RequestHandler.addToRequestQueue(req);
    }
    protected void executeDelete(String URL, final int expectedCode) throws CloudAppException {
        //RESTful GET Json request to the server.
        Request req = new ResponseParser(Request.Method.DELETE, URL, null,
                new Response.Listener<ServerResponse>() {

                    @Override
                    public void onResponse(ServerResponse response) {
                        //Checks the network response code against the expected code.
                        // If it fails an exception is thrown
                        if(response.getNetworkResponseCode() == expectedCode) {

                            try {
                                //Grabs the Json string from the ServerResponse object
                                // and sends it to the callback function
                                rCallbacks.serverResponse(response.getJsonString());
                                Log.i("Response: ", response.getJsonString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                throw new CloudAppException(500, "Unexpected response code");
                            } catch (CloudAppException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }
                rCallbacks.serverErrorResponse(volleyError);
            }
        });
        RequestHandler.addToRequestQueue(req);
    }
    /**
     * Executes a POST to a url with a certain body.
     *
     *
     * @param body
     * @return
     * @throws CloudAppException
     */
    protected void executePost(String URL, String body, final int expectedCode)
            throws CloudAppException {

        Request req = new ResponseParser(Request.Method.POST, URL, body,
                new Response.Listener<ServerResponse>() {

                    @Override
                    public void onResponse(ServerResponse response) {
                        //Checks the network response code against the expected code.
                        // If it fails an exception is thrown
                        if(response.getNetworkResponseCode() == expectedCode) {

                            try {
                                //Grabs the Json string from the ServerResponse object
                                // and sends it to the callback function
                                rCallbacks.serverResponse(response.getJsonString());
                                Log.i("Response: ", response.getJsonString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                throw new CloudAppException(500, "Unexpected response code");
                            } catch (CloudAppException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }
                rCallbacks.serverErrorResponse(volleyError);
            }
        });
        RequestHandler.addToRequestQueue(req);
    }
    /**
     * Executes a PUT request to a URL for a given body.
     *
     *
     * @param url
     *          The url to do the PUT request too.
     * @param body
     *          The body of the request.
     * @return A JSONObject or JSONArray constructed from the body of the CloudApp API's
     *         response.
     * @throws CloudAppException
     */
    protected void executePut(String URL, String body, final int expectedCode)
            throws CloudAppException {

        Request req = new ResponseParser(Request.Method.PUT, URL, body,
                new Response.Listener<ServerResponse>() {

                    @Override
                    public void onResponse(ServerResponse response) {
                        //Checks the network response code against the expected code.
                        // If it fails an exception is thrown
                        if(response.getNetworkResponseCode() == expectedCode) {

                            try {
                                //Grabs the Json string from the ServerResponse object
                                // and sends it to the callback function
                                rCallbacks.serverResponse(response.getJsonString());
                                Log.i("Response: ", response.getJsonString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                throw new CloudAppException(500, "Unexpected response code");
                            } catch (CloudAppException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                    VolleyError error = new VolleyError(new String(volleyError.networkResponse.data));
                    volleyError = error;
                }
                rCallbacks.serverErrorResponse(volleyError);
            }
        });
        RequestHandler.addToRequestQueue(req);
    }

    public static interface ResponseCallbacks {
        void serverResponse(String response);
        void serverErrorResponse(VolleyError error);
    }


}
