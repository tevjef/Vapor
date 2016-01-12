package com.tevinjeffrey.vapor.okcloudapp

import com.squareup.okhttp.Authenticator
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response

import org.apache.http.HttpRequest
import org.apache.http.auth.AuthenticationException
import org.apache.http.auth.Credentials
import org.apache.http.auth.MalformedChallengeException
import org.apache.http.impl.auth.DigestScheme
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicHttpRequest

import java.io.IOException
import java.net.Proxy

import timber.log.Timber

class DigestAuthenticator(private val mDigestScheme: DigestScheme, private var mCredentials: Credentials?) : Authenticator {

    fun setCredentials(mCredentials: Credentials) {
        this.mCredentials = mCredentials
    }

    @Throws(IOException::class)
    override fun authenticate(proxy: Proxy, response: Response): Request? {
        val authHeader = buildAuthorizationHeader(response) ?: return null
        return response.request().newBuilder().addHeader("Authorization", authHeader).build()
    }

    @Throws(IOException::class)
    override fun authenticateProxy(proxy: Proxy, response: Response): Request? {
        return null
    }

    @Throws(IOException::class)
    private fun buildAuthorizationHeader(response: Response): String? {
        processChallenge("WWW-Authenticate", response.header("WWW-Authenticate"))
        return generateDigestHeader(response)
    }

    private fun processChallenge(headerName: String, headerValue: String) {
        try {
            mDigestScheme.processChallenge(BasicHeader(headerName, headerValue))
        } catch (e: MalformedChallengeException) {
            Timber.e(e, "Error processing header $headerName for DIGEST authentication.")
        }

    }

    @Throws(IOException::class)
    private fun generateDigestHeader(response: Response): String? {
        val request = BasicHttpRequest(
                response.request().method(),
                response.request().uri().toString())

        try {
            return mDigestScheme.authenticate(mCredentials, request).value
        } catch (e: AuthenticationException) {
            Timber.e(e, "Error generating DIGEST auth header.")
            return null
        }

    }
}