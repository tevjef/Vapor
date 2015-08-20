package com.tevinjeffrey.vapr.okcloudapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import retrofit.mime.TypedFile;

public class ProgressiveTypedFile extends TypedFile {
    /**
     * Default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 4096;


    /**
     * Listens for progress of this array being written
     */
    private final ProgressListener mListener;


    public ProgressiveTypedFile(
                                final File file,
                                final ProgressListener listener) {
        super(FileUtils.getMimeType(file), file);
        this.mListener = listener;
    }


    @Override
    public void writeTo(final OutputStream out) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(super.file());
        long total = 0;
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                total += read;
                this.mListener.onProgress(total, super.file().length());
                out.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }
}