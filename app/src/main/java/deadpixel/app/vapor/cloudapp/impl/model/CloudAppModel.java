package deadpixel.app.vapor.cloudapp.impl.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;

public abstract class CloudAppModel {
    protected JSONObject json;
    protected static final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    protected static final DateFormat formatBis = new SimpleDateFormat(
            "yyyy-MM-dd");

    public Date SubscriptionExpiresAt() throws CloudAppException {
        // TODO Auto-generated method stub
        return null;
    }
}
