package deadpixel.app.vapor.callbacks;

/**
 * Created by Tevin on 7/8/2014.
 */
public class ResponseEvent extends Event{
    String response;

    public ResponseEvent(String response) {
        super(response);
        this.response = response;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
}
