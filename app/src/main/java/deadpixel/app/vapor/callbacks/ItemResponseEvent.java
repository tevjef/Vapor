package deadpixel.app.vapor.callbacks;

import java.util.ArrayList;
import java.util.List;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppAccount;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.cloudapp.impl.model.ItemModel;

/**
 * Created by Tevin on 6/16/2014.
 */
public class ItemResponseEvent extends Event {

    ArrayList<ItemModel> items;

    public ArrayList<ItemModel> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemModel> items) {
        this.items = items;
    }


    public ItemResponseEvent(ArrayList<ItemModel> items) {
        super(null);
        this.items = items;
    }
}