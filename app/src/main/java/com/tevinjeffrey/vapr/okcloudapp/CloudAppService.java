package com.tevinjeffrey.vapr.okcloudapp;

import com.tevinjeffrey.vapr.okcloudapp.model.AccountModel;
import com.tevinjeffrey.vapr.okcloudapp.model.AccountStatsModel;
import com.tevinjeffrey.vapr.okcloudapp.model.ItemModel;
import com.tevinjeffrey.vapr.okcloudapp.model.UploadModel;

import java.util.List;
import java.util.Map;

import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import rx.Observable;

public interface CloudAppService {
    @Headers("Accept: application/json")
    @GET("/items")
    Observable<List<ItemModel>> listItems(@QueryMap Map<String, String> options);

    @Headers("Accept: application/json")
    @GET("/account")
    Observable<AccountModel> getAccount();

    @Headers("Accept: application/json")
    @GET("/account/stats")
    Observable<AccountStatsModel> getAccountStats();

    @Headers("Accept: application/json")
    @GET("/items/new")
    Observable<UploadModel> newUpload();

    @Headers("Accept: application/json")
    @DELETE("items/{item-id}")
    Observable<ItemModel> deleteItem(@Path("item-id") String itemId);
}
