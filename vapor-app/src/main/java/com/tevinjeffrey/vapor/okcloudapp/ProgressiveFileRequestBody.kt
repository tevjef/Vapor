package com.tevinjeffrey.vapor.okcloudapp

import com.squareup.okhttp.MediaType
import com.tevinjeffrey.vapor.okcloudapp.utils.FileUtils

import java.io.File
import java.io.FileInputStream
import java.io.IOException

import okio.BufferedSink

class ProgressiveFileRequestBody(private val file: File, private val listener: ProgressListener) : CloudAppRequestBody() {

    private val DEFAULT_BUFFER_SIZE = 4096

    override val fileName: String
        get() = file.name

    override fun contentLength(): Long {
        return file.length()
    }

    override fun contentType(): MediaType {
        return MediaType.parse(FileUtils.getMimeType(file.absolutePath))
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = contentLength()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val `in` = FileInputStream(file)
        var total: Long = 0
        try {
            var read = `in`.read(buffer)
            while (read != -1) { // Arg!! Kotlin doesn't allow assignment expressions  while ((read = in.read()) != -1)
                this.listener.onProgress(total, fileLength)
                total += read.toLong()
                sink.write(buffer, 0, read)
                read = `in`.read(buffer)
            }
        } finally {
            `in`.close()
        }
    }
}
