package com.tevinjeffrey.vapor.okcloudapp.model

class AccountModel {
    var id: Long = 0
    var email: String? = null
    var domain: String? = null
    var domainHomePage: String? = null
    var isPrivateItems: Boolean = false
    var isSubscribed: Boolean = false
    var subscriptionExpiresAt: String? = null
    var isAlpha: Boolean = false
    var createdAt: String? = null
    var updatedAt: String? = null
    var activatedAt: String? = null

    internal val defaultSecurity: DefaultSecurity
        get() = if (isPrivateItems) DefaultSecurity.PRIVATE else DefaultSecurity.PUBLIC

    internal enum class DefaultSecurity {
        PRIVATE, PUBLIC
    }
}
