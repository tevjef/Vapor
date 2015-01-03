package deadpixel.app.vapor.networkOp;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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

        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement el = parser.parse(jsonString);
        return gson.toJson(el);
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    public static String getError(NetworkResponse networkResponse) {
        String errorDescription = "An error occurred";

        if(networkResponse == null) {
            errorDescription = "Unable to contact CloudApp servers. Try again later";
        }
        return errorDescription;
    }
}
