package deadpixel.app.vapor.cloudapp.impl.model;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;

/**
 * Created by Tevin on 6/7/14.
 */
public class ItemModel implements CloudAppItem {


    private long id;
    private String href;
    private String name;
    @SerializedName("private")
    private boolean isPrivate;
    @SerializedName("subscribed")
    private boolean isSubscribed;
    private String content_url;
    private String item_type;
    private long view_counter;
    private String icon;
    private String url;
    private String remote_url;
    private String thumbnail_url;
    private String download_url;
    private String source;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private String lastViewed_at;

    public String getHref() {
        return href;
    }

    public String getName() {
        return name;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    @Override
    public boolean isTrashed() {
        return deleted_at == null;
    }


    public String getUrl() {
        return url == null? remote_url: url;
    }

    public String getContentUrl() {
        return content_url;
    }

    public Type getItemType() {
        try {
            return Type.valueOf(item_type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Type.UNKNOWN;
        }
    }

    public long getViewCounter() {
        return view_counter;
    }

    public String getIcon() {
        return icon;
    }

    public String getRemoteUrl() {
        return remote_url;
    }


    public String getSource() {
        return source;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public String getDeletedAt() {
        return deleted_at;
    }

    public String getLastViewedAt() {
        return lastViewed_at;
    }

    public String getThumbnailUrl() {
        return thumbnail_url;
    }

    public void setThumbnailUrl(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrivacy(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setSubscribed(boolean subscribed) {
        this.isSubscribed = subscribed;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setContentUrl(String content_url) {
        this.content_url = content_url;
    }

    public void setItemType(String item_type) {
        this.item_type = item_type;
    }

    public void setViewCounter(long view_counter) {
        this.view_counter = view_counter;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setRemoteUrl(String remote_url) {
        this.remote_url = remote_url;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at == null?null:String.valueOf(CloudAppModel.formatDate(created_at).getTime());
    }

    public void setUpdatedAt(String updated_at) {
        this.updated_at = updated_at  == null?null:String.valueOf(CloudAppModel.formatDate(updated_at).getTime());
    }

    public void setDeletedAt(String deleted_at) {
        this.deleted_at = deleted_at == null?null:String.valueOf(CloudAppModel.formatDate(deleted_at).getTime());
    }

    public void setLastViewedAt(String lastViewed_at) {
        this.lastViewed_at = lastViewed_at == null?null:String.valueOf(CloudAppModel.formatDate(lastViewed_at).getTime());
    }
    public void setEpochCreatedAt(Long created_at) {
        this.created_at = created_at == 0? null:String.valueOf(created_at);
    }

    public void setEpochUpdatedAt(Long updated_at) {
        this.updated_at = String.valueOf(updated_at);
    }

    public void setEpochDeletedAt(Long deleted_at) {
        this.deleted_at = deleted_at == 0? null: String.valueOf(deleted_at);
    }

    public void setEpochLastViewedAt(Long lastViewed_at) {
        this.lastViewed_at = lastViewed_at == 0?null:String.valueOf(lastViewed_at);
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDownloadUrl() {
        return download_url == null?remote_url : download_url;
    }

    public void setDownloadUrl(String download_url) {
        this.download_url = download_url;
    }

    public String getFormattedCreatedAt() throws CloudAppException {

        Date d = new Date();
        d.setTime(Long.parseLong(created_at));

        String s = DateFormat.getDateInstance()
                    .format(d);

        return s;
    }

    public String getFormattedUpdatedAt() throws CloudAppException {
        try {
            return updated_at ==null?null:DateFormat.getDateInstance()
                    .format(CloudAppModel.format.parse(updated_at));
        } catch (ParseException e) {
            throw new CloudAppException(500, "Error parsing account updated_at date", e);
        }
    }

    public String getFormattedDeletedAt() throws CloudAppException {
        try {
            return deleted_at==null?null:DateFormat.getDateInstance()
                    .format(CloudAppModel.format.parse(deleted_at));
        } catch (ParseException e) {
            throw new CloudAppException(500, "Error parsing account deleted_at date", e);
        }
    }




}
