package deadpixel.app.vapor.callbacks;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;

/**
 * Created by Tevin on 6/16/2014.
 */
public interface AccountUpdateCallback {
    void onAccountUpdate() throws CloudAppException;
}