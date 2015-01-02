package deadpixel.app.vapor.callbacks;

import java.util.ArrayList;
import java.util.List;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.database.model.DatabaseItem;

/**
 * Created by Tevin on 6/16/2014.
 */
public class DatabaseUpdateEvent extends Event {

    public ArrayList<DatabaseItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<DatabaseItem> items) {
        this.items = items;
    }

    ArrayList<DatabaseItem> items;
    public DatabaseUpdateEvent(ArrayList<DatabaseItem> items) {
        super(null);
        this.items = items;
    }
}