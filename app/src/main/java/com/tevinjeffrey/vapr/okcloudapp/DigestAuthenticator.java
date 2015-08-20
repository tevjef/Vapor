package com.tevinjeffrey.vapr.okcloudapp;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;

import java.io.IOException;
import java.net.Proxy;

import timber.log.Timber;

public class DigestAuthenticator implements Authenticator {
    private DigestScheme mDigestScheme;
    private Credentials mCredentials;

    public DigestAuthenticator(DigestScheme mDigestScheme, Credentials credentials) {
        this.mDigestScheme = mDigestScheme;
        this.mCredentials = credentials;

    }

    public void setCredentials(Credentials mCredentials) {
        this.mCredentials = mCredentials;
    }

    @Override
    public Request authenticate(Proxy proxy, Response response) throws IOException {
        String authHeader = buildAuthorizationHeader(response);
        if (authHeader == null) {
            return null;
        }
        return response.request().newBuilder().addHeader("Authorization", authHeader).build();
    }

    @Override
    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
        return null;
    }

    private String buildAuthorizationHeader(Response response) throws IOException {
        processChallenge("WWW-Authenticate", response.header("WWW-Authenticate"));
        return generateDigestHeader(response);
    }

    private void processChallenge(String headerName, String headerValue) {
        try {
            mDigestScheme.processChallenge(new BasicHeader(headerName, headerValue));
        } catch (MalformedChallengeException e) {
            Timber.e(e, "Error processing header " + headerName + " for DIGEST authentication.");
        }
    }

    private String generateDigestHeader(Response response) throws IOException {
        org.apache.http.HttpRequest request = new BasicHttpRequest(
                response.request().method(),
                response.request().uri().toString()
        );

        try {
            return mDigestScheme.authenticate(mCredentials, request).getValue();
        } catch (AuthenticationException e) {
            Timber.e(e, "Error generating DIGEST auth header.");
            return null;
        }
    }
}