package com.grensil.ui.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.graphics.scale
import java.net.URI

suspend fun loadBitmapFromUrl(url: String, width: Int? = null, height: Int? = null): Bitmap? {
    Log.d("Logd","${url}")

    val safeUrl = safeUrl(url)
    MemoryCache.get(safeUrl)?.let { return it } // 캐시 체크

    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(safeUrl).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.doInput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Android)"
            )
            connection.connect()

            val bytes = connection.inputStream.readBytes()
            Log.d("LoadImage", "Downloaded ${bytes.size} bytes")

            // PNG 헤더 확인 (89 50 4E 47 0D 0A 1A 0A)
            if (bytes.size >= 8) {
                val header = bytes.take(8)
                val headerHex = header.joinToString(" ") { "%02X".format(it) }
                Log.d("LoadImage", "File header: $headerHex")

                val isPng = header[0] == 0x89.toByte() &&
                        header[1] == 0x50.toByte() &&
                        header[2] == 0x4E.toByte() &&
                        header[3] == 0x47.toByte()
                Log.d("LoadImage", "Is PNG: $isPng")
            }

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            Log.d("LoadImage", "Bitmap decoded: ${bitmap != null}")
            bitmap?.let {
                Log.d("LoadImage", "Bitmap size: ${it.width}x${it.height}, config: ${it.config}")
            }

            val scaled = bitmap?.let { original ->
                val targetWidth = width ?: original.width
                val targetHeight = height ?: original.height

                Log.d("LoadImage", "Scaling from ${original.width}x${original.height} to ${targetWidth}x${targetHeight}")

                if (targetWidth > 0 && targetHeight > 0) {
                    original.scale(targetWidth, targetHeight)
                } else {
                    Log.e("LoadImage", "Invalid scale dimensions: ${targetWidth}x${targetHeight}")
                    original
                }
            }

            scaled?.let {
                Log.d("LoadImage", "Putting in cache: ${safeUrl}")
                MemoryCache.put(safeUrl, it)
            }
            Log.d("LoadImage", "Final result: ${scaled != null}")

            scaled
        } catch (e: Exception) {
            Log.d("LoadImage", "error: ${e}")
            e.printStackTrace()
            null
        }
    }
}

fun safeUrl(rawUrl: String): String {
    val urlWithScheme = when {
        rawUrl.startsWith("//") -> "https:$rawUrl"
        !rawUrl.startsWith("http://") && !rawUrl.startsWith("https://") -> "https://$rawUrl"
        else -> rawUrl
    }

    // URI 변환 없이 그대로 반환
    return urlWithScheme
}