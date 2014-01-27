package deadpixel.app.vapor.cloudapp.impl;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.networkOp.RequestHandler;

public class CloudAppBase extends RequestHandler{

    private final String TAG = "deadpixel.app.vapor.cloudapp.impl.CloudAppBase";

    protected static final String MY_CL_LY = "http://my.cl.ly";
    public static JSONObject response;

    public CloudAppBase() {

    }
    public static void getJSON(JSONObject object) {
        response = object;
    }

    /**
     * Executes a GET to a url with a certain body.
     *
     * @param url
     * @return
     * @throws CloudAppException
     */
    protected Object executeGet(String URL) throws CloudAppException {

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject serverResponse) {
                        try {
                        CloudAppBase.getJSON(serverResponse);
                            Log.i("Response: ", serverResponse.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Log.i("Printing stack trace",".....");
                error.printStackTrace();
            }
        }) {
        @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
            RequestHandler.addToRequestQueue(req);

        return response;
    }

    protected Object executeSyncGet(String URL) throws CloudAppException {

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest req = new JsonObjectRequest(URL, null,
                future, future)  {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
            RequestHandler.addToRequestQueue(req);

        JSONObject response = null;
        try {
            response = future.get(); // this will block
        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            // exception handling
        }
        return response;
    }

    /**
     * Executes a DELETE to a url.
     *
     * @param url
     * @return
     * @throws CloudAppException
     */
    protected Object executeDelete(String URL) throws CloudAppException {

        final JSONObject[] response = new JSONObject[1];

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.DELETE, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject serverResponse) {
                        try {
                            response[0] = serverResponse;
                            VolleyLog.v("Response:%n %s", serverResponse.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        return response[0];
    }


    /**
     * Executes a POST to a url with a certain body.
     *
     *
     * @param body
     * @return
     * @throws CloudAppException
     */
    protected Object executePost(String URL, JSONObject body, int expectedCode)
            throws CloudAppException {

        final JSONObject[] response = new JSONObject[1];

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, URL, body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject serverResponse) {
                        try {
                            response[0] = serverResponse;
                            VolleyLog.v("Response:%n %s", serverResponse.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        return response[0];
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
    protected Object executePut(String URL, JSONObject body, int expectedCode)
            throws CloudAppException {
        final JSONObject[] response = new JSONObject[1];

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, URL, body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject serverResponse) {
                        try {
                            response[0] = serverResponse;
                            VolleyLog.v("Response:%n %s", serverResponse.toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        return response[0];
    }
}