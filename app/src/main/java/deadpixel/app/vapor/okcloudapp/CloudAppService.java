package deadpixel.app.vapor.okcloudapp;

import java.util.Map;

import deadpixel.app.vapor.cloudapp.impl.AccountStatsImpl;
import deadpixel.app.vapor.okcloudapp.model.AccountModel;
import deadpixel.app.vapor.okcloudapp.model.ItemModel;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import rx.Observable;

public interface CloudAppService {
    @GET("/items")
    Observable<ItemModel> listItems(@QueryMap Map<String, String> options);

    @GET("/account")
    Observable<AccountModel> getAccount();

    @GET("/account/stats")
    Observable<AccountStatsImpl> getAccountStats();

    @DELETE("items/{item-id}")
    Observable<ItemModel> deleteItem(@Path("item-id") String itemId);
}
