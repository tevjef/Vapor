package deadpixel.app.vapor.cloudapp.impl.model;

import com.google.gson.annotations.SerializedName;

import java.sql.Date;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;

/**
 * Created by Tevin on 6/7/14.
 */
public class ItemResponseModel implements CloudAppItem{

    public String getHref() {
        return href;
    }

    public String getName() {
        return name;
    }

    public boolean isPrivate() {
        return visibility;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    @Override
    public boolean isTrashed() throws CloudAppException {
        return false;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getContentUrl() throws CloudAppException {
        return null;
    }

    public Type getItemType() {
        return item_type;
    }

    public long getViewCounter() {
        return view_counter;
    }

    @Override
    public String getIconUrl() throws CloudAppException {
        return null;
    }

    public String getIcon() {
        return icon;
    }

    public String getRemoteUrl() {
        return remote_url;
    }

    public String getRedirectUrl() {
        return redirect_url;
    }

    @Override
    public String getThumbnailUrl() throws CloudAppException {
        return null;
    }

    public String getSource() {
        return source;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public Date getDeletedAt() {
        return deleted_at;
    }

    private String href;
    private String name;
    @SerializedName("private")
    private boolean visibility;
    private boolean subscribed;
    private String url;
    private String content_url;
    private Type item_type;
    private long view_counter;
    private String icon;
    private String remote_url;
    private String redirect_url;
    private String source;
    private Date created_at;
    private Date updated_at;
    private Date deleted_at;



}
