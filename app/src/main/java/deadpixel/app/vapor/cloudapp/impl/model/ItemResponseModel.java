package deadpixel.app.vapor.cloudapp.impl.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.util.Date;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;
import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;

/**
 * Created by Tevin on 6/7/14.
 */
public class ItemResponseModel extends CloudAppModel implements CloudAppItem{



    private String href;
    private String name;
    @SerializedName("private")
    private boolean visibility;
    private boolean subscribed;
    private String url;
    private String content_url;
    private String item_type;
    private long view_counter;
    private String icon;
    private String remote_url;
    private String redirect_url;
    private String source;
    private String created_at;
    private String updated_at;
    private String deleted_at;

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
        return deleted_at == null || deleted_at == "null";
    }


    public String getUrl() {
        return url;
    }

    public String getContentUrl() throws CloudAppException {
        return null;
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

    public String getSource() {
        return source;
    }

    public Date getCreatedAt() throws CloudAppException {
        try {
            return format.parse(created_at);
        } catch(ParseException e) {
            throw new CloudAppException(600, "Error parsing item created_at date", e);
        }
    }

    public Date getUpdatedAt() throws CloudAppException {
        try {
            return format.parse(updated_at);
        } catch(ParseException e) {
            throw new CloudAppException(600, "Error parsing item updated_at date", e);
        }
    }

    public Date getDeletedAt() throws CloudAppException {
        try {
            return format.parse(deleted_at);
        } catch(ParseException e) {
            throw new CloudAppException(600, "Error parsing item deleted_At date", e);
        }
    }

    public void setHref(String href) {
        this.href = href;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
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

    public void setRedirectUrl(String redirect_url) {
        this.redirect_url = redirect_url;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdatedAt(String updated_at) {
        this.updated_at = updated_at;
    }

    public void setDeletedAt(String deleted_at) {
        this.deleted_at = deleted_at;
    }

}
