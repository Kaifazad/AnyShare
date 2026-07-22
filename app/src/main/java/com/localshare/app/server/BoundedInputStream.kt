package com.localshare.app.server

import java.io.InputStream

/**
 * Wraps an [InputStream] and limits the number of bytes that can be read from it.
 * After [limit] bytes have been read, further reads return -1 (EOF).
 *
 * This is essential for NanoHTTPD range request support because
 * [FileInputStream] positioned via [FileChannel.position()] will read
 * until the actual EOF of the file, not until the requested range end.
 * Without this wrapper, NanoHTTPD may attempt to send more data than
 * Content-Length advertises, causing browsers to abort the connection.
 */
class BoundedInputStream(
    private val wrapped: InputStream,
    private val limit: Long
) : InputStream() {

    private var bytesRead: Long = 0

    override fun read(): Int {
        if (bytesRead >= limit) return -1
        val b = wrapped.read()
        if (b != -1) bytesRead++
        return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (bytesRead >= limit) return -1
        val maxToRead = minOf(len.toLong(), limit - bytesRead).toInt()
        val n = wrapped.read(b, off, maxToRead)
        if (n > 0) bytesRead += n
        return n
    }

    override fun available(): Int {
        val remaining = limit - bytesRead
        return minOf(wrapped.available().toLong(), remaining).toInt()
    }

    override fun close() {
        wrapped.close()
    }
}
