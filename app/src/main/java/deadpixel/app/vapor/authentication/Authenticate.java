package deadpixel.app.vapor.authentication;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import deadpixel.app.vapor.AuthenticationActivity;
import deadpixel.app.vapor.cloudapp.impl.AccountImpl;
import deadpixel.app.vapor.networkOp.RequestHandler;

/**
 * Created by Tevin on 1/16/14.
 */
public class Authenticate extends AuthenticationActivity {

    public static void isValid() {
        startProgress();

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, AccountImpl.ACCOUNT_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject serverResponse) {
                        try {
                            progress.dismiss();
                            Log.i("Response: Email: ", serverResponse.getString("email"));

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
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };

        RequestHandler.addToRequestQueue(req);
    }
    public static void createAcc() {
        startProgress();

        JSONObject json = new JSONObject();
        try {

            JSONObject user = new JSONObject();
            user.put("email", getEmail());
            user.put("password", getPass());
            user.put("accept_tos", true);
            json.put("user", user);


        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AccountImpl.REGISTER_URL, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject serverResponse) {
                        try {
                            progress.dismiss();
                            Log.i("Response: Email: ", serverResponse.getString("email"));

                            Log.i("Response: acocunt created ", serverResponse.toString(4));
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
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };

        RequestHandler.addToRequestQueue(req);

        } catch (JSONException e) {
            Log.e("JSONException", "Something went wrong creating new account JSON");
        }
    }
}
