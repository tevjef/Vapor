package com.tevinjeffrey.vapor.okcloudapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import retrofit.mime.TypedFile;

public class ProgressiveTypedFile extends TypedFile {
    /**
     * Default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;


    /**
     * Listens for progress of this array being written
     */
    private final ProgressListener mListener;


    public ProgressiveTypedFile(
                                final File file,
                                final ProgressListener listener) {
        super(FileUtils.getMimeType(file.getAbsolutePath()), file);
        this.mListener = listener;
    }


    @Override
    public void writeTo(final OutputStream out) throws IOException {
        long fileLength  = file().length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(file());
        long total = 0;
        try {
            int read;
            while ((read = in.read(buffer)) != -1) {
                this.mListener.onProgress(total, fileLength);
                total += read;
                out.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }
}