package deadpixel.app.vapor.networkOp;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tevin on 6/8/14.
 */

//I created this class for code reduction
public class ResponseParser extends JsonRequest<ServerResponse>{

    public ResponseParser(int method, String url, String requestBody, Response.Listener<ServerResponse> listener, Response.ErrorListener errorListener) {
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
    protected Response<ServerResponse> parseNetworkResponse(NetworkResponse networkResponse) {
        String jsonString = null;
        try {
            jsonString = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ServerResponse sr = new ServerResponse(networkResponse.statusCode, jsonString);
        return Response.success(sr, HttpHeaderParser.parseCacheHeaders(networkResponse));
    }

}
