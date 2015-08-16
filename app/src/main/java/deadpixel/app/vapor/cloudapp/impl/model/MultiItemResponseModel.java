package deadpixel.app.vapor.cloudapp.impl.model;

import java.util.ArrayList;
import java.util.List;

import deadpixel.app.vapor.okcloudapp.model.ItemModel;

/**
 * Created by Tevin on 6/10/2014.
 */
public class MultiItemResponseModel {

    public List<ItemModel> getList() {
        return list;
    }

    public void setList(List<ItemModel> list) {
        this.list = list;
    }

    List<ItemModel> list = new ArrayList<ItemModel>();
}
