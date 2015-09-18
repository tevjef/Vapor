package com.tevinjeffrey.vapor.okcloudapp;

import com.squareup.okhttp.RequestBody;
import com.tevinjeffrey.vapor.okcloudapp.model.AccountModel;
import com.tevinjeffrey.vapor.okcloudapp.model.AccountStatsModel;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppJsonAccount;
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppJsonItem;
import com.tevinjeffrey.vapor.okcloudapp.model.ItemModel;
import com.tevinjeffrey.vapor.okcloudapp.model.UploadModel;

import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import rx.Observable;

public interface CloudAppService {
    @Headers("Accept: application/json")
    @GET("/{item-id}")
    Observable<ItemModel> getItem(@Path("item-id") String itemId);

    @Headers("Accept: application/json")
    @GET("/items")
    Observable<List<ItemModel>> listItems(@QueryMap Map<String, String> options);

    @Headers("Accept: application/json")
    @GET("/items/new")
    Observable<UploadModel> newUpload();

    @Headers("Accept: application/json")
    @DELETE("/items/{item-id}")
    Observable<ItemModel> deleteItem(@Path("item-id") String itemId);

    @Headers("Accept: application/json")
    @PUT("/items/{item-id}")
    Observable<ItemModel> renameItem(@Path("item-id") String itemId, @Body CloudAppJsonItem item);

    @Headers("Accept: application/json")
    @PUT("/items/{item-id}")
    Observable<ItemModel> setItemSecurity(@Path("item-id") String itemId, @Body CloudAppJsonItem item);

    @Headers("Accept: application/json")
    @POST("/items/{item-id}")
    Observable<ItemModel> bookmarkLink(@Path("item-id") String itemId, @Body CloudAppJsonItem item);

    @Headers("Accept: application/json")
    @GET("/account")
    Observable<AccountModel> getAccount();

    @Headers("Accept: application/json")
    @PUT("/account")
    Observable<AccountModel> changeDefaultSecurity(@Body CloudAppJsonAccount body);

    @Headers("Accept: application/json")
    @PUT("/account")
    Observable<AccountModel> changeEmail(@Body CloudAppJsonAccount body);

    @Headers("Accept: application/json")
    @POST("/account")
    Observable<AccountModel> changePassword(@Body CloudAppJsonAccount body);

    @Headers("Accept: application/json")
    @POST("/account")
    Observable<AccountModel> setCustomDomain(@Body CloudAppJsonAccount body);

    @Headers("Accept: application/json")
    @POST("/reset")
    Observable<AccountModel> resetPassword(@Body CloudAppJsonAccount body);

    @Headers("Accept: application/json")
    @POST("/register")
    Observable<AccountModel> registerAccount(@Body CloudAppJsonAccount body);

    @Headers("Accept: application/json")
    @GET("/account/stats")
    Observable<AccountStatsModel> getAccountStats();

    @Headers("Accept: application/json")
    @Multipart
    @POST("http://f.cl.ly")
    Observable<CloudAppItem> uploadFile(@PartMap Map<String, String> options, @Part("file") RequestBody filePart);
}
