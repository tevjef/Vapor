package deadpixel.app.vapor.cloudapp.impl;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.HeadersCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import deadpixel.app.vapor.callbacks.ErrorEvent;
import deadpixel.app.vapor.callbacks.ItemResponseEvent;
import deadpixel.app.vapor.callbacks.ResponseEvent;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppProgressListener;
import deadpixel.app.vapor.cloudapp.impl.model.ItemModel;
import deadpixel.app.vapor.cloudapp.impl.model.MultiItemResponseModel;
import deadpixel.app.vapor.cloudapp.impl.model.UploadResponseModel;
import deadpixel.app.vapor.database.DatabaseUpdate;
import deadpixel.app.vapor.networkOp.RequestExecutor;
import deadpixel.app.vapor.utils.AppUtils;

public class CloudAppItemsImpl {

    public static final String MY_CL_LY = "http://my.cl.ly";
    private static final String TAG = "CLOUDAPPIMPL";
    private static final String ITEMS_URL = MY_CL_LY + "/items";
    private static final String NEW_ITEM_URL = ITEMS_URL + "/new?item[private]=false";

    private RequestExecutor executor = new RequestExecutor();

    public CloudAppItemsImpl() {


        executor.setListener(new RequestExecutor.RequestResponseListener() {
            @Override
            public void OnSuccessResponse(String response) {

                ArrayList<ItemModel> items = new ArrayList<ItemModel>();
                if (response.startsWith("[")) {
                    Type listType = new TypeToken<List<ItemModel>>() {
                    }.getType();

                    items = gson.fromJson(response, listType);
                    new DatabaseUpdate().start(items);
                    AppUtils.getEventBus().post(new ItemResponseEvent(items));
                } else if (response.startsWith("{")) {
                    ItemModel item = gson.fromJson(response, ItemModel.class);
                    items.add(item);
                    new DatabaseUpdate().start(items);
                    AppUtils.getEventBus().post(new ItemResponseEvent(items));
                }


            }

            @Override
            public void OnErrorResponse(VolleyError errorResponse) {

            }
        });

    }

    Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    String response;


    /**
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem createBookmark(java.lang.String,
     * java.lang.String)
     */
    public Request createBookmark(String name, String url) throws CloudAppException {
        try {
            JSONObject json = createBody(new String[]{"name", "redirect_url"}, new String[]{
                    name, url});

            return executor.executePost(ITEMS_URL, json.toString(), 200);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem createBookmarks(java.lang.String[][])
     */
    public Request createBookmarks(String[][] bookmarks)
            throws CloudAppException {
        try {
            JSONArray arr = new JSONArray();
            for (String[] bookmark : bookmarks) {
                arr.put(createJSONBookmark(bookmark[0], bookmark[1]));
            }

            JSONObject json = new JSONObject();
            json.put("items", arr);

            return executor.executePost(ITEMS_URL, json.toString(), 200);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    private JSONObject createJSONBookmark(String name, String url) throws JSONException {
        JSONObject item = new JSONObject();
        item.put("name", name);
        item.put("redirect_url", url);
        return item;
    }

    /**
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem getItems(int, int,
     * deadpixel.app.vapor.cloudapp.api.model.CloudAppItem.Type, boolean, java.lang.String)
     */
    public Request getItems(int page, int perPage, CloudAppItem.Type type,
                            String source) throws CloudAppException {

        if (perPage < 5)
            perPage = 5;
        if (page == 0)
            page = 1;

        List<String> params = new ArrayList<String>();
        params.add("page=" + page);
        params.add("per_page=" + perPage);

        params.add("deleted=" + (type == CloudAppItem.Type.DELETED ? "true" : "false"));

        if (type != null) {
            //If type is not all and  do not add a type parameter
            if (type != CloudAppItem.Type.ALL && type != CloudAppItem.Type.DELETED)
                params.add("type=" + type.toString().toLowerCase());
        }
        if (source != null) {
            params.add("source=" + source);
        }

        String queryString = StringUtils.join(params.iterator(), "&");

        return executor.executeGet(ITEMS_URL + "?" + queryString, 200);

    }

    /**
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem upload(java.io.File)
     */
    public CloudAppItem upload(File file) throws CloudAppException {
        return upload(file);
    }

    public void upload(final File file, final ProgressCallback progressCallback) throws CloudAppException {

        RequestExecutor executor = new RequestExecutor();

        AppUtils.addToRequestQueue(executor.executeGet(NEW_ITEM_URL, 200));
        executor.setListener(new RequestExecutor.RequestResponseListener() {
            @Override
            public void OnSuccessResponse(String response) {
                UploadResponseModel uploadResponse = gson.fromJson(response, UploadResponseModel.class);

                if (uploadResponse.getParams() == null) {
                    // Something went wrong, maybe we crossed the threshold?
                    if (uploadResponse.getUploads_remaining() == 0) {
                        Toast.makeText(AppUtils.getInstance().getApplicationContext(),
                                "Uploads remaining is 0", Toast.LENGTH_LONG).show();

                        Log.e(TAG, "Uploads remaining is 0");

                    }

                    Log.e(TAG, "Params is null");
                }
                try {
                    uploadToAmazon(uploadResponse, file, progressCallback);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (CloudAppException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnErrorResponse(VolleyError errorResponse) {

            }
        });


    }



    /**
     * Uploads a file to S3
     *
     * @param json
     * @param file
     * @return
     * @throws JSONException
     * @throws CloudAppException
     * @throws ParseException
     * @throws IOException
     */
    private void uploadToAmazon(UploadResponseModel uploadResponse, File file, ProgressCallback progressCallback) throws JSONException,
            CloudAppException, ParseException, IOException {


        if(file.length() > uploadResponse.getMax_upload_size()) {
            AppUtils.getEventBus().post(new ErrorEvent(AppUtils.FILE_TOO_LARGE));
        } else if(uploadResponse.getUploads_remaining() == 0) {
            AppUtils.getEventBus().post(new ErrorEvent(AppUtils.UPLOAD_TICKETS_ZERO));
        } else {

            String fileName = makeFileName(file);

            Ion.with(AppUtils.getInstance().getApplicationContext())
                    .load(uploadResponse.getUrl())
                    .uploadProgress(progressCallback)
                    .setLogging("Ion: ", Log.DEBUG)
                    .followRedirect(false)
                    .proxy("10.0.3.2", 8888)
                    .setHeader("Accept", "application/json")
                    .setMultipartParameter("acl", uploadResponse.getParams().getAcl())
                    .setMultipartParameter("AWSAccessKeyId", uploadResponse.getParams().getAWSAccessKeyId())
                    .setMultipartParameter("key", uploadResponse.getParams().getKey())
                    .setMultipartParameter("success_action_redirect", uploadResponse.getParams().getSuccess_action_redirect())
                    .setMultipartParameter("policy", uploadResponse.getParams().getPolicy())
                    .setMultipartParameter("signature", uploadResponse.getParams().getSignature())
                    .setMultipartFile("file", file)
                    .asJsonObject()
                    .withResponse()

                    .setCallback(new FutureCallback<Response<JsonObject>>() {
                        @Override
                        public void onCompleted(Exception e, Response<JsonObject> result) {


                            //Get file after upload. Commmented out because it interupts the build.
                            if (result != null) {
/*
                                String requestString = result.getRequest().getRequestString();
                                RawHeaders header = result.getHeaders();
                                String location = header.get("Location");

                                try {
                                    if (location != null) {
                                        AppUtils.addToRequestQueue(executor.executeGet(location, 200));
                                    }
                                } catch (CloudAppException e1) {
                                    e1.printStackTrace();
                                }*/
                            /*if (result.getResult() != null) {
                                String resultString = result.getResult().toString();


                                Log.i("Request: ", requestString);
                                Log.i("Headers: ", headerString);
                                Log.i("Result: : ", resultString);

                                Log.e(TAG, e.getMessage());

                                ArrayList<ItemModel> items = new ArrayList<ItemModel>();
                                if (result.getResult().toString().startsWith("{")) {
                                    ItemModel item = gson.fromJson(response, ItemModel.class);
                                    items.add(item);
                                    new DatabaseUpdate().start(items);
                                    AppUtils.getEventBus().post(new ItemResponseEvent(items));
                                }
                            }*/
                            }
                        }
                    });
        }


/*        HttpPost uploadRequest = new HttpPost(json.getString("url"));
        uploadRequest.addHeader("Accept", "application/json");
        uploadRequest.setEntity(entity);

        // Perform the actual upload.
        // uploadMethod.setFollowRedirects(true);
        HttpResponse response = client.execute(uploadRequest);
        int status = response.getStatusLine().getStatusCode();
        String body = EntityUtils.toString(response.getEntity());
        if (status == 200) {
            return new CloudAppItemImpl(new JSONObject(body));
        }
        throw new CloudAppException(status, "Was unable to upload the file to amazon:\n"
                + body, null);*/
    }


    private String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date dateobj = new Date();
        System.out.println(df.format(dateobj));
        return df.format(dateobj);
    }

    private String makeFileName(File file) {

        return file.getName().equals("")? getCurrentTime(): file.getName();
    }

    public Request delete(CloudAppItem item) throws CloudAppException {
        return executor.executeDelete(item.getHref(), 200);
    }

    public Request recover(CloudAppItem item) throws CloudAppException {
        try {
            JSONObject json = createBody(new String[] { "deleted_at" },
                    new Object[] { JSONObject.NULL });
            json.put("deleted", true);
            executor.executePut(item.getHref(), json.toString(), 200);
            return executor.executePut(item.getHref(), json.toString(), 200);

        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    public Request setSecurity(CloudAppItem item, boolean is_private)
            throws CloudAppException {
        try {
            JSONObject json = createBody(new String[] { "private" },
                    new Object[] { is_private });

            return executor.executePut(item.getHref(), json.toString(), 200);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    public Request rename(CloudAppItem item, String name) throws CloudAppException {
        try {
            JSONObject json = createBody(new String[] { "name" }, new Object[] { name });

            return executor.executePut(item.getHref(), json.toString(), 200);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    public Request getItem(String url) throws CloudAppException {
        return executor.executeGet(url, 200);
    }

    private JSONObject createBody(String[] keys, Object[] values) throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject item = new JSONObject();
        for (int i = 0; i < keys.length; i++) {
            item.put(keys[i], values [i]);
        }
        json.put("item", item);
        return json;
    }


}
