package deadpixel.app.vapor.callbacks;

/**
 * Created by Tevin on 7/8/2014.
 */
public class Event {
    String response;
    public Event(String response) {
        this.response = response;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
}

