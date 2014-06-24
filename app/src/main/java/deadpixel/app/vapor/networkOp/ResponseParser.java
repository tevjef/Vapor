package deadpixel.app.vapor.networkOp;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tevin on 6/8/14.
 */

//I created this class for code reduction
public class ResponseParser extends JsonRequest<NetworkResponse> {

    public ResponseParser(int method, String url, String requestBody, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        return headers;
    }

    //parses the network response to the get the the network response code.
    //The response code and the the encoded jsonstring is saved to a object for future use.
    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse networkResponse) {

        return Response.success(networkResponse, HttpHeaderParser.parseCacheHeaders(networkResponse));
    }

    public static String getJson(NetworkResponse networkResponse) {

        String jsonString = null;
        try {
            jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    @Override
    public Priority getPriority() {
        return Priority.IMMEDIATE;
    }

    public static String getError(NetworkResponse networkResponse) {
        String errorDescription;
        switch(networkResponse.statusCode) {
            case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                errorDescription = "Invalid email and/or password";
                break;
            case HttpStatus.SC_UNAUTHORIZED:
                errorDescription = "Unauthorized";
                break;
            case HttpStatus.SC_NOT_ACCEPTABLE:
                errorDescription = "Account already exists";
                break;
            default:
                errorDescription = "An error occurred";
        }
        return errorDescription;
    }
}
