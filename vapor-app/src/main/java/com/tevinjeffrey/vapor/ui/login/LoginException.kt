package com.tevinjeffrey.vapor.ui.login

class LoginException : Exception {
    var code: Int = 0

    constructor() {
    }

    constructor(detailMessage: String) : super(detailMessage) {
    }

    constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable) {
    }
}
