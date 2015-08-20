package com.tevinjeffrey.vapr.okcloudapp;

import com.tevinjeffrey.vapr.okcloudapp.model.CloudAppItem;
import com.tevinjeffrey.vapr.okcloudapp.utils.ProgressiveTypedFile;

import java.util.Map;

import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
import rx.Observable;

/**
 * Created by Tevin on 8/18/2015.
 */
public interface AmazonUploadService {
    @Headers("Accept: application/json")
    @Multipart
    @POST("/")
    Observable<CloudAppItem> postFile(@PartMap Map<String, String> options, @Part("file") ProgressiveTypedFile filePart);
}
