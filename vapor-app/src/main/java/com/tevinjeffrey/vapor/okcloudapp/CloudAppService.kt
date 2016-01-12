package com.tevinjeffrey.vapor.okcloudapp

import com.squareup.okhttp.RequestBody
import com.tevinjeffrey.vapor.okcloudapp.model.AccountModel
import com.tevinjeffrey.vapor.okcloudapp.model.AccountStatsModel
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppJsonAccount
import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppJsonItem
import com.tevinjeffrey.vapor.okcloudapp.model.ItemModel
import com.tevinjeffrey.vapor.okcloudapp.model.UploadModel

import retrofit.http.Body
import retrofit.http.DELETE
import retrofit.http.GET
import retrofit.http.Headers
import retrofit.http.Multipart
import retrofit.http.POST
import retrofit.http.PUT
import retrofit.http.PartMap
import retrofit.http.Path
import retrofit.http.QueryMap
import rx.Observable

interface CloudAppService {
    @Headers("Accept: application/json")
    @GET("/{item-id}")
    fun getItem(@Path("item-id") itemId: String): Observable<ItemModel>

    @Headers("Accept: application/json")
    @GET("/items")
    fun listItems(@QueryMap options: Map<String, String>): Observable<List<ItemModel>>

    @Headers("Accept: application/json")
    @GET("/items/new")
    fun newUpload(@QueryMap parts: Map<String, String>): Observable<UploadModel>

    @Headers("Accept: application/json")
    @DELETE("/items/{item-id}")
    fun deleteItem(@Path("item-id") itemId: String): Observable<ItemModel>

    @Headers("Accept: application/json")
    @PUT("/items/{item-id}")
    fun renameItem(@Path("item-id") itemId: String, @Body item: CloudAppJsonItem): Observable<ItemModel>

    @Headers("Accept: application/json")
    @PUT("/items/{item-id}")
    fun setItemSecurity(@Path("item-id") itemId: String, @Body item: CloudAppJsonItem): Observable<ItemModel>

    @Headers("Accept: application/json")
    @POST("/items")
    fun bookmarkLink(@Body item: CloudAppJsonItem): Observable<ItemModel>

    @Headers("Accept: application/json")
    @GET("/account")
    fun getAccount(): Observable<AccountModel>

    @Headers("Accept: application/json")
    @PUT("/account")
    fun changeDefaultSecurity(@Body body: CloudAppJsonAccount): Observable<AccountModel>

    @Headers("Accept: application/json")
    @PUT("/account")
    fun changeEmail(@Body body: CloudAppJsonAccount): Observable<AccountModel>

    @Headers("Accept: application/json")
    @POST("/account")
    fun changePassword(@Body body: CloudAppJsonAccount): Observable<AccountModel>

    @Headers("Accept: application/json")
    @POST("/account")
    fun setCustomDomain(@Body body: CloudAppJsonAccount): Observable<AccountModel>

    @Headers("Accept: application/json")
    @POST("/reset")
    fun resetPassword(@Body body: CloudAppJsonAccount): Observable<AccountModel>

    @Headers("Accept: application/json")
    @POST("/register")
    fun registerAccount(@Body body: CloudAppJsonAccount): Observable<AccountModel>

    @Headers("Accept: application/json")
    @GET("/account/stats")
    fun getAccountStats(): Observable<AccountStatsModel>

    @Multipart
    @Headers("Accept: application/json")
    @POST("http://s3.amazonaws.com/f.cl.ly")
    fun uploadFile(@PartMap parts: Map<String, RequestBody>): Observable<ItemModel>
}
