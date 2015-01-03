package deadpixel.app.vapor.cloudapp.api.model;

import deadpixel.app.vapor.cloudapp.api.CloudAppException;

/**
 *
 */
public interface CloudAppItem {

    enum Type {

       ALL, DELETED, AUDIO, BOOKMARK, IMAGE, UNKNOWN, VIDEO, ARCHIVE, TEXT;
    }


    /**
     * @return A unique URL that points to this resource. ie:
     *         "http://my.cl.ly/items/1912729"
     * @throws CloudAppExtion
     */
    String getHref() ;

    /**
     * @return The name of this resource.
     * @throws CloudAppExtion
     */
    String getName() ;

    /**
     * @return The name of this resource.
     * @throws CloudAppExtion
     */
    long getId() ;

    /**
     * @return Wether or not this is item is marked as private
     * @throws CloudAppExtion
     */
    boolean isPrivate();

    /**
     * @return
     */
    boolean isSubscribed() ;

    /**
     * @return Whether or not an item resides in the trash bin.
     */
    public boolean isTrashed();

    /**
     * @return A short url to this resource. ie: "http://cl.ly/2wr4"
     * @throws CloudAppExtion
     */
    String getUrl();

    /**
     * @return The url to the actual content of this resource. ie:
     *         "http://cl.ly/2wr4/CloudApp_Logo.png"
     * @throws CloudAppExtion
     */
    String getContentUrl() ;

    /**
     * @return The type of this resource. ie: bookmark, image, ..
     * @throws CloudAppExtion
     */
    Type getItemType() ;

    /**
     * @return How many times this item has been viewed.
     * @throws CloudAppExtion
     */
    long getViewCounter() ;

    /**
     * @return A url that you can use to publicly point to this url. ie:
     *         "http://f.cl.ly/items/7c7aea1395c3db0aee18/CloudApp%20Logo.png"
     * @throws CloudAppExtion
     */
    String getRemoteUrl();

    /**
     * @return A url that you can use to publicly point to this url. ie:
     *         "http://f.cl.ly/items/download/7c7aea1395c3db0aee18/CloudApp%20Logo.png"
     * @throws CloudAppExtion
     */
    String getDownloadUrl();


    String getThumbnailUrl();

    /**
     * @return A url that points to a icon of this item if one is available,
     *         null otherwise. ie: "http://thumbs.cl.ly/2wr4"
     * @throws CloudAppException
     */
    String getIcon() ;

    /**
     * @return Identifies the app that uploaded this item. ie:
     *         "Cloud/1.5.1 CFNetwork/520.0.13 Darwin/11.0.0 (x86_64) (MacBookPro5%2C5)"
     * @throws CloudAppExtion
     */
    String getSource();

    /**
     * @return When this item was created.
     * @throws CloudAppExtion
     */
    String getCreatedAt() ;

    /**
     * @return When this item was last updated. (or null if it has not been)
     * @throws CloudAppExtion
     */
    String getUpdatedAt();

    /**
     * @return When this item was deleted. (or null if it has not been)
     * @throws CloudAppExtion
     */
    String getDeletedAt();

    /**
     * @return When this item was deleted. (or null if it has not been)
     * @throws CloudAppExtion
     */
    String getLastViewedAt() ;

}
