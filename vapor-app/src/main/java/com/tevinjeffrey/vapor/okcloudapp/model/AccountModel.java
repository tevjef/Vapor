package com.tevinjeffrey.vapor.okcloudapp.model;

import com.tevinjeffrey.vapor.okcloudapp.utils.CloudAppUtils;

import java.text.DateFormat;
import java.text.ParseException;

public class AccountModel {

    private static final String TAG = "AccountModel";
    private long id;
    private String email;
    private String domain;
    private String domain_home_page;
    private boolean private_items;
    private boolean subscribed;
    private String subscription_expires_at;
    private boolean alpha;
    private String created_at;
    private String updated_at;
    private String activated_at;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getDomainHomePage() {
        return domain_home_page;
    }
    public void setDomainHomePage(String domain_home_page) {
        this.domain_home_page = domain_home_page;
    }
    public boolean isPrivateItems() {
        return private_items;
    }

    public DefaultSecurity getDefaultSecurity() {
        return isPrivateItems()?DefaultSecurity.PRIVATE:DefaultSecurity.PUBLIC;
    }
    public void setPrivateItems(boolean private_items) {
        this.private_items = private_items;
    }
    public boolean isSubscribed() {
        return subscribed;
    }
    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public String getSubscriptionExpiresAt() {
        return subscription_expires_at;
    }

    public String getFormattedSubscriptionExpiresAt() {
        try {
            return subscription_expires_at == null? null:DateFormat.getDateInstance()
                    .format(CloudAppUtils.formatBis.parse(subscription_expires_at));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void setSubscriptionExpiresAt(String subscription_expires_at) {
        this.subscription_expires_at = subscription_expires_at;
    }
    public boolean isAlpha() {
        return alpha;
    }
    public void setAlpha(boolean alpha) {
        this.alpha = alpha;
    }
    public String getCreatedAt() {
        return created_at;
    }

    public String getFormattedCreatedAt()  {

        String s = null;
        try {
            s = DateFormat.getDateInstance()
                    .format(CloudAppUtils.format.parse(created_at));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return created_at == null? null: s;

    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getFormattedUpdatedAt()  {
        try {
            return updated_at ==null?null:DateFormat.getDateInstance()
                    .format(CloudAppUtils.format.parse(updated_at));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }
    public String getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getFormattedActivatedAt() {
        try {
            return activated_at==null?null:DateFormat.getDateInstance()
                        .format(CloudAppUtils.format.parse(activated_at));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getActivatedAt() {
        return activated_at;
    }
    public void setActivatedAt(String activated_at) {
        this.activated_at = activated_at;
    }

    enum DefaultSecurity {
        PRIVATE, PUBLIC
    }
}
