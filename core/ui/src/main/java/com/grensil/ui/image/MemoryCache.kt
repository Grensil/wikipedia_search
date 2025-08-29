package com.grensil.ui.image

import android.graphics.Bitmap
import android.util.LruCache

object MemoryCache {
    private val cache = LruCache<String, Bitmap>(10 * 1024 * 1024) // 10MB

    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, bitmap: Bitmap) = cache.put(key, bitmap)
}