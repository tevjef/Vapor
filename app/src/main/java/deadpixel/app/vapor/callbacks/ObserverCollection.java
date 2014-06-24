package deadpixel.app.vapor.callbacks;

import android.util.Log;

import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tevin on 6/17/2014.
 */
public class ObserverCollection {
    public static ObserverCollection instance = new ObserverCollection();
    public static ObserverCollection getInstance() {
        return instance;
    }

    Map<String, Observer> observers = new HashMap<String, Observer>();

    public void registerObserver(String callbackName, Observer observer) {
        if(observer != null) {
            observers.put(callbackName, observer);

        } else {
            Log.i("ObserverCollection", "Could not add collection");
        }
    }

    public void unRegisterObserver(String callbackName, Observer observer) {
        if(observer != null) {
            observers.remove(callbackName);

        } else {
            Log.i("ObserverCollection", "Could not add collection");
        }
    }

    public void notifyObservers(String response) {
        for(Map.Entry<String, Observer> observer : observers.entrySet()) {
            observer.getValue().onServerResponse(response);
        }
    }

    public Observer getObserver(String observer) {
        return observers.get(observer);
    }
    public void notifyServerError(VolleyError e, String errorDescription) {
        for(Map.Entry<String, Observer> observer : observers.entrySet()) {
            observer.getValue().onServerError(e, errorDescription);
        }
    }

}
