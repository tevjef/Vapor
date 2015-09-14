package com.tevinjeffrey.vapor.okcloudapp;

import com.tevinjeffrey.vapor.okcloudapp.model.CloudAppItem;

import java.util.Map;

import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.mime.TypedFile;
import rx.Observable;

public interface AmazonUploadService {
    @Headers("Accept: application/json")
    @Multipart
    @POST("/")
    Observable<CloudAppItem> postFile(@PartMap Map<String, String> options, @Part("file") TypedFile filePart);
}
