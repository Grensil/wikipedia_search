package com.grensil.ui.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

suspend fun loadBitmapFromUrl(url: String, width: Int? = null, height: Int? = null): Bitmap? {
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
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            val scaled = bitmap?.let { original ->
                val targetWidth = width ?: original.width
                val targetHeight = height ?: original.height

                if (targetWidth > 0 && targetHeight > 0) {
                    original.scale(targetWidth, targetHeight)
                } else {
                    original
                }
            }

            scaled?.let {
                MemoryCache.put(safeUrl, it)
            }
            scaled
        } catch (e: Exception) {
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