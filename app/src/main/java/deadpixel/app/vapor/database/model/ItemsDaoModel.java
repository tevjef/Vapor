package deadpixel.app.vapor.database.model;

import java.util.List;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;

/**
 * Created by Tevin on 7/27/2014.
 */
public interface ItemsDaoModel {

    public List<DatabaseItem> insertItems(List<DatabaseItem> items);

    public DatabaseItem getRow(long rowId);

    public DatabaseItem getItem(CloudAppItem item);

    public List<DatabaseItem> getItemsByType(CloudAppItem.Type type);

    public List<DatabaseItem> getItems(CloudAppItem.Type type, int limit);

    public int updateItem(DatabaseItem item);

    public int deleteItem(DatabaseItem item);

    public int deleteAllRows();
}
