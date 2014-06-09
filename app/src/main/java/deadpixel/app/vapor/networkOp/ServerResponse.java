package deadpixel.app.vapor.networkOp;

/**
 * Created by Tevin on 6/8/14.
 */
public class ServerResponse {
    public ServerResponse(int networkResponseCode, String string) {
        this.networkResponseCode = networkResponseCode;
        this.jsonString = string;
    }

    public int getNetworkResponseCode() {
        return networkResponseCode;
    }

    public void setNetworkResponseCode(int networkResponseCode) {
        this.networkResponseCode = networkResponseCode;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    private int networkResponseCode;
    private String jsonString;

}