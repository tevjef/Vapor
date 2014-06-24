package deadpixel.app.vapor.cloudapp.impl;

import android.util.Log;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import deadpixel.app.vapor.callbacks.ResponseCallback;
import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppProgressListener;
import deadpixel.app.vapor.cloudapp.impl.model.ItemResponseModel;
import deadpixel.app.vapor.cloudapp.impl.model.MuliItemResponseModel;
import deadpixel.app.vapor.cloudapp.impl.model.UploadResponseModel;
import deadpixel.app.vapor.networkOp.RequestExecutors;
import deadpixel.app.vapor.networkOp.RequestHandler;

public class CloudAppItemsImpl extends RequestExecutors  {

    public static final String MY_CL_LY = "http://my.cl.ly";
    private static final String TAG = "CLOUDAPPIMPL";
    private static final String ITEMS_URL = MY_CL_LY + "/items";
    private static final String NEW_ITEM_URL = ITEMS_URL + "/new?item[private]=false";

    DefaultHttpClient client = RequestHandler.client;
    public CloudAppItemsImpl() {

    }

    Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    String response;


    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem createBookmark(java.lang.String,
     *      java.lang.String)
     */
    public CloudAppItem createBookmark(String name, String url) throws CloudAppException {
        try {
            JSONObject json = createBody(new String[] { "name", "redirect_url" }, new String[] {
                    name, url });
            executePost(ITEMS_URL, json.toString(), 200);

            return gson.fromJson(response, ItemResponseModel.class);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem createBookmarks(java.lang.String[][])
     */
    public List<CloudAppItem> createBookmarks(String[][] bookmarks)
            throws CloudAppException {
        try {
            JSONArray arr = new JSONArray();
            for (String[] bookmark : bookmarks) {
                arr.put(createJSONBookmark(bookmark[0], bookmark[1]));
            }

            JSONObject json = new JSONObject();
            json.put("items", arr);
            executePost(ITEMS_URL, json.toString(), 200);

            List<CloudAppItem> items = new ArrayList<CloudAppItem>();
            for (int i = 0; i < arr.length(); i++) {
                //This needs a single item but the server responds with an array of objects.
                //MuliItemResponseModel uses gson to store the ItemReponseModel objects an ArrayList
                //This gets the items from the Arraylist.
                items.add( gson.fromJson(response, MuliItemResponseModel.class).getList().get(i));
            }
            return items;
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
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem getItems(int, int,
     *      deadpixel.app.vapor.cloudapp.api.model.CloudAppItem.Type, boolean, java.lang.String)
     */
    public List<CloudAppItem> getItems(int page, int perPage, CloudAppItem.Type type,
                                       boolean showDeleted, String source) throws CloudAppException {

            if (perPage < 5)
                perPage = 5;
            if (page == 0)
                page = 1;

            List<String> params = new ArrayList<String>();
            params.add("page="+page);
            params.add("per_page="+perPage);
            params.add("deleted="+ (showDeleted ? "true" : "false"));

            if (type != null)
            {
                params.add("type=" + type.toString().toLowerCase());
            }
            if (source != null)
            {
                params.add("source=" + source);
            }

            String queryString = StringUtils.join(params.iterator(), "&");
            executeGet(ITEMS_URL + "?" + queryString, 200);

            List<CloudAppItem> items = new ArrayList<CloudAppItem>();

            MuliItemResponseModel model = gson.fromJson(response, MuliItemResponseModel.class);

            for (int i = 0; i < model.getList().size(); i++) {
                items.add(gson.fromJson(response, MuliItemResponseModel.class).getList().get(i));
            }
            return items;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see deadpixel.app.vapor.cloudapp.api.model.CloudAppItem upload(java.io.File)
     */
    public CloudAppItem upload(File file) throws CloudAppException {
        return upload( file, CloudAppProgressListener.NO_OP );
    }

    public CloudAppItem upload(File file, CloudAppProgressListener listener) throws CloudAppException {
        try {
            // Do a GET request so we have the S3 endpoint
            HttpGet req = new HttpGet(NEW_ITEM_URL);
            executeGet(NEW_ITEM_URL, 200);

            UploadResponseModel uploadResponse;

            uploadResponse = gson.fromJson(response, UploadResponseModel.class);

            if (uploadResponse.getParams() == null) {
                // Something went wrong, maybe we crossed the threshold?
                if (uploadResponse.getUploads_remaining() == 0) {

                    //TODO: Get message back to UI
                    throw new CloudAppException(200, "Uploads remaining is 0", null);
                }
                throw new CloudAppException(500, "Missing params object from the CloudApp API.",
                        null);
            }

            return uploadToAmazon(uploadResponse, file, listener);

        } catch (ClientProtocolException e) {
            Log.e(TAG, "Something went wrong trying to contact the CloudApp API.", e);
            throw new CloudAppException(500,
                    "Something went wrong trying to contact the CloudApp API", e);
        } catch (IOException e) {
            Log.e(TAG, "Something went wrong trying to contact the CloudApp API.", e);
            throw new CloudAppException(500,
                    "Something went wrong trying to contact the CloudApp API.", e);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
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
    private CloudAppItem uploadToAmazon(UploadResponseModel uploadResponse, File file, CloudAppProgressListener listener) throws JSONException,
            CloudAppException, ParseException, IOException {



        Map<String, Object> paramMap = new LinkedHashMap<String, Object>();

        paramMap.put("acl", uploadResponse.getParams().getAcl());
        paramMap.put("AWSAccessKeyId", uploadResponse.getParams().getAWSAccessKeyId());
        paramMap.put("key", uploadResponse.getParams().getKey());
        paramMap.put("success_action_redirect", uploadResponse.getParams().getSuccess_action_redirect());
        paramMap.put("policy", uploadResponse.getParams().getPolicy());
        paramMap.put("signature", uploadResponse.getParams().getSignature());

        // Add the actual file.
        // We have to use the 'file' parameter for the S3 storage.
        InputStreamBody stream = new CloudAppInputStream(file, listener);
        paramMap.put("file", stream);

        executePost(uploadResponse.getUrl(), null, 200);
       /* HttpPost uploadRequest = new HttpPost(json.getString("url"));
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
        return null;
    }

    public CloudAppItem delete(CloudAppItem item) throws CloudAppException {
        executeDelete(item.getHref(), 200);
        return gson.fromJson(response, ItemResponseModel.class);
    }

    public CloudAppItem recover(CloudAppItem item) throws CloudAppException {
        try {
            JSONObject json = createBody(new String[] { "deleted_at" },
                    new Object[] { JSONObject.NULL });
            json.put("deleted", true);
            executePut(item.getHref(), json.toString(), 200);
            return gson.fromJson(response, ItemResponseModel.class);

        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    public CloudAppItem setSecurity(CloudAppItem item, boolean is_private)
            throws CloudAppException {
        try {
            JSONObject json = createBody(new String[] { "private" },
                    new Object[] { is_private });
            executePut(item.getHref(), json.toString(), 200);
            return gson.fromJson(response, ItemResponseModel.class);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    public CloudAppItem rename(CloudAppItem item, String name) throws CloudAppException {
        try {
            JSONObject json = createBody(new String[] { "name" }, new Object[] { name });
            executePut(item.getHref(), json.toString(), 200);
            return gson.fromJson(response, ItemResponseModel.class);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong trying to handle JSON.", e);
            throw new CloudAppException(500, "Something went wrong trying to handle JSON.", e);
        }
    }

    public CloudAppItem getItem(String url) throws CloudAppException {
        executeGet(url, 200);
        return gson.fromJson(response, ItemResponseModel.class);
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
