package com.localshare.app.server

import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Handles HTTP Range requests for video/audio streaming with seeking support.
 *
 * Key design decisions for reliable long-video streaming:
 *
 * 1. Uses [BoundedInputStream] to hard-cap the number of bytes sent per chunk.
 *    NanoHTTPD's `newFixedLengthResponse` trusts the caller's content-length,
 *    but FileInputStream positioned via FileChannel will happily read to EOF.
 *    Without bounding, a 4 GB file would overflow the advertised Content-Length,
 *    causing browsers to abort the transfer mid-stream.
 *
 * 2. Does NOT manually add a Content-Length header — NanoHTTPD does this
 *    internally from the `contentLength` parameter. Adding a duplicate causes
 *    double headers and breaks chunked transfer encoding in some browsers.
 *
 * 3. For the full-file (200 OK) path, also uses BoundedInputStream for safety.
 */
object RangeRequestHandler {

    data class RangeInfo(
        val start: Long,
        val end: Long,
        val totalSize: Long
    ) {
        val contentLength: Long get() = end - start + 1
        val contentRangeHeader: String get() = "bytes $start-$end/$totalSize"
    }

    /**
     * Parse a Range header value like "bytes=0-1023" or "bytes=1024-".
     * Returns null if the header is absent or malformed.
     */
    fun parseRange(rangeHeader: String?, totalSize: Long): RangeInfo? {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) {
            return null
        }

        try {
            val rangeSpec = rangeHeader.removePrefix("bytes=").trim()
            val parts = rangeSpec.split("-", limit = 2)

            if (parts.size != 2) return null

            val startStr = parts[0].trim()
            val endStr = parts[1].trim()

            val start: Long
            val end: Long

            when {
                // "bytes=500-999" — explicit range
                startStr.isNotEmpty() && endStr.isNotEmpty() -> {
                    start = startStr.toLong()
                    end = minOf(endStr.toLong(), totalSize - 1)
                }
                // "bytes=500-" — from offset to end
                startStr.isNotEmpty() && endStr.isEmpty() -> {
                    start = startStr.toLong()
                    end = totalSize - 1
                }
                // "bytes=-500" — last 500 bytes
                startStr.isEmpty() && endStr.isNotEmpty() -> {
                    val suffixLength = endStr.toLong()
                    start = maxOf(totalSize - suffixLength, 0)
                    end = totalSize - 1
                }
                else -> return null
            }

            // Validate
            if (start < 0 || start >= totalSize || end < start) {
                return null
            }

            return RangeInfo(start, end, totalSize)

        } catch (e: NumberFormatException) {
            return null
        }
    }

    private const val MAX_INT_CONTENT_LENGTH = 2_147_483_647L // Int.MAX_VALUE

    /**
     * Create a NanoHTTPD response for a physical file.
     * Supports Range requests (206) and full downloads (200).
     * For files > 2GB, uses chunked transfer to avoid NanoHTTPD's int overflow.
     */
    fun createResponse(
        file: File,
        mimeType: String,
        rangeHeader: String?
    ): NanoHTTPD.Response {
        val totalSize = file.length()
        val rangeInfo = parseRange(rangeHeader, totalSize)

        return if (rangeInfo != null) {
            val fis = FileInputStream(file)
            fis.channel.position(rangeInfo.start)

            if (rangeInfo.contentLength <= MAX_INT_CONTENT_LENGTH) {
                val bounded = BoundedInputStream(fis, rangeInfo.contentLength)
                val response = NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.PARTIAL_CONTENT,
                    mimeType,
                    bounded,
                    rangeInfo.contentLength
                )
                response.addHeader("Content-Range", rangeInfo.contentRangeHeader)
                response.addHeader("Accept-Ranges", "bytes")
                response.addHeader("Connection", "keep-alive")
                addCorsHeaders(response)
                response
            } else {
                val bounded = BoundedInputStream(fis, rangeInfo.contentLength)
                val response = NanoHTTPD.newChunkedResponse(
                    NanoHTTPD.Response.Status.PARTIAL_CONTENT,
                    mimeType,
                    bounded
                )
                response.addHeader("Content-Range", rangeInfo.contentRangeHeader)
                response.addHeader("Content-Length", rangeInfo.contentLength.toString())
                response.addHeader("Accept-Ranges", "bytes")
                response.addHeader("Connection", "keep-alive")
                addCorsHeaders(response)
                response
            }
        } else {
            val fis = FileInputStream(file)

            if (totalSize <= MAX_INT_CONTENT_LENGTH) {
                val bounded = BoundedInputStream(fis, totalSize)
                val response = NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    mimeType,
                    bounded,
                    totalSize
                )
                response.addHeader("Accept-Ranges", "bytes")
                addCorsHeaders(response)
                response
            } else {
                val bounded = BoundedInputStream(fis, totalSize)
                val response = NanoHTTPD.newChunkedResponse(
                    NanoHTTPD.Response.Status.OK,
                    mimeType,
                    bounded
                )
                response.addHeader("Content-Length", totalSize.toString())
                response.addHeader("Accept-Ranges", "bytes")
                addCorsHeaders(response)
                response
            }
        }
    }

    /**
     * Create a response using a ContentResolver InputStream (for content:// URIs).
     */
    fun createResponseFromStream(
        inputStream: InputStream,
        totalSize: Long,
        mimeType: String,
        rangeHeader: String?
    ): NanoHTTPD.Response {
        val rangeInfo = parseRange(rangeHeader, totalSize)

        return if (rangeInfo != null) {
            var skipped = 0L
            while (skipped < rangeInfo.start) {
                val s = inputStream.skip(rangeInfo.start - skipped)
                if (s <= 0) {
                    if (inputStream.read() == -1) break
                    skipped++
                    continue
                }
                skipped += s
            }

            val bounded = BoundedInputStream(inputStream, rangeInfo.contentLength)

            if (rangeInfo.contentLength <= MAX_INT_CONTENT_LENGTH) {
                val response = NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.PARTIAL_CONTENT,
                    mimeType,
                    bounded,
                    rangeInfo.contentLength
                )
                response.addHeader("Content-Range", rangeInfo.contentRangeHeader)
                response.addHeader("Accept-Ranges", "bytes")
                response.addHeader("Connection", "keep-alive")
                addCorsHeaders(response)
                response
            } else {
                val response = NanoHTTPD.newChunkedResponse(
                    NanoHTTPD.Response.Status.PARTIAL_CONTENT,
                    mimeType,
                    bounded
                )
                response.addHeader("Content-Range", rangeInfo.contentRangeHeader)
                response.addHeader("Content-Length", rangeInfo.contentLength.toString())
                response.addHeader("Accept-Ranges", "bytes")
                response.addHeader("Connection", "keep-alive")
                addCorsHeaders(response)
                response
            }
        } else {
            val bounded = BoundedInputStream(inputStream, totalSize)

            if (totalSize <= MAX_INT_CONTENT_LENGTH) {
                val response = NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    mimeType,
                    bounded,
                    totalSize
                )
                response.addHeader("Accept-Ranges", "bytes")
                addCorsHeaders(response)
                response
            } else {
                val response = NanoHTTPD.newChunkedResponse(
                    NanoHTTPD.Response.Status.OK,
                    mimeType,
                    bounded
                )
                response.addHeader("Content-Length", totalSize.toString())
                response.addHeader("Accept-Ranges", "bytes")
                addCorsHeaders(response)
                response
            }
        }
    }

    private fun addCorsHeaders(response: NanoHTTPD.Response) {
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Headers", "Range")
        response.addHeader("Access-Control-Expose-Headers", "Content-Range, Accept-Ranges, Content-Length")
    }
}
