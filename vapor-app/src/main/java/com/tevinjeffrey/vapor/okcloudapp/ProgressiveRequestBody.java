package com.tevinjeffrey.vapor.okcloudapp;

import android.webkit.MimeTypeMap;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.tevinjeffrey.vapor.okcloudapp.utils.ProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okio.BufferedSink;

public class ProgressiveRequestBody extends RequestBody {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private final ProgressListener listener;
    private final File file;

    public ProgressiveRequestBody(final File file, final ProgressListener listener) {
        this.file = file;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(getMimeType(file.getAbsolutePath()));
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = file.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(file);
        long total = 0;
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                this.listener.onProgress(total, fileLength);
                total += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (type == null) {
            return "application/octet-stream";
        }
        return type;
    }

}
