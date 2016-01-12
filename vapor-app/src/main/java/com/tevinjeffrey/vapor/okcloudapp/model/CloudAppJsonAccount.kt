package com.tevinjeffrey.vapor.okcloudapp.model

class CloudAppJsonAccount {
    internal var user: User? = null
    internal inner class User {
        var email: String? = null
        var password: String? = null
        var currentPassword: String? = null
        var domain: String? = null
        var domainHomePage: String? = null
        var acceptTos: Boolean = false
        var privateItems: Boolean = false
    }
}
