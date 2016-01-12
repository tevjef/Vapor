package com.tevinjeffrey.vapor.okcloudapp

import com.squareup.okhttp.MediaType
import com.squareup.okhttp.internal.Util

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

import okio.BufferedSink

class ProgressiveStringRequestBody(string: String, name: String?, private val listener: ProgressListener) : CloudAppRequestBody() {
    private val DEFAULT_BUFFER_SIZE = 4096

    override val fileName: String
        get() = name

    private val inputStream: InputStream
    private val bytes: ByteArray
    private val name: String

    init {
        val charset = Util.UTF_8
        this.bytes = string.toByteArray(charset)
        this.name = name ?: string.substring(0, if (string.length < 18) string.length else 18).trim { it <= ' ' } + ".txt" //Sorry
        this.inputStream = ByteArrayInputStream(bytes)
    }

    override fun contentLength(): Long {
        return bytes.size.toLong()
    }

    override fun contentType(): MediaType {
        return MediaType.parse("text/plain")
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val fileLength = contentLength()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total: Long = 0
        try {
            var read: Int = 0
            while (read != -1) {
                read = inputStream.read(buffer)
                this.listener.onProgress(total, fileLength)
                total += read.toLong()
                sink.write(buffer, 0, read)
            }
        } finally {
            inputStream.close()
        }
    }
}
