package com.tevinjeffrey.vapor.okcloudapp;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.internal.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import okio.BufferedSink;

public class ProgressiveStringRequestBody extends CloudAppRequestBody {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private ProgressListener listener;
    private final InputStream inputStream;
    private final byte[] bytes;
    private final String name;

    public ProgressiveStringRequestBody(final String string, String name, ProgressListener listener) {
        Charset charset = Util.UTF_8;
        this.bytes = string.getBytes(charset);
        this.name = name == null? string.substring(0, string.length() < 18? string.length(): 18).trim() + ".txt" : name; //Sorry
        this.inputStream = new ByteArrayInputStream(bytes);
        this.listener = listener;
    }

    @Override
    public long contentLength() {
        return bytes.length;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("text/plain");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = contentLength();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long total = 0;
        try {
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                this.listener.onProgress(total, fileLength);
                total += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            inputStream.close();
        }
    }

    @Override
    public String getFileName() {
        return name;
    }
}
