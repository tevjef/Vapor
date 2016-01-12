package com.tevinjeffrey.vapor.okcloudapp

import android.webkit.MimeTypeMap

import com.squareup.okhttp.MediaType

import java.io.File
import java.io.FileInputStream
import java.io.IOException

import okio.BufferedSink

class ProgressiveFileRequestBody(private val file: File, private val listener: ProgressListener) : CloudAppRequestBody() {
    override val fileName: String
        get() = file.name

    override fun contentLength(): Long {
        return file.length()
    }

    override fun contentType(): MediaType {
        return MediaType.parse(getMimeType(file.absolutePath))
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = contentLength()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val `in` = FileInputStream(file)
        var total: Long = 0
        try {
            var read: Int = 0
            while (read != -1) {
                read = `in`.read(buffer)
                this.listener.onProgress(total, fileLength)
                total += read.toLong()
                sink.write(buffer, 0, read)
            }
        } finally {
            `in`.close()
        }
    }

    companion object {

        private val DEFAULT_BUFFER_SIZE = 4096

        private fun getMimeType(url: String): String {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            if (type == null) {
                return "application/octet-stream"
            }
            return type
        }
    }
}
