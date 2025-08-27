package com.grensil.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.grensil.ui.image.loadBitmapFromUrl


@Composable
fun CachedImage(url: String? = null, width: Int? = null, height: Int? = null, modifier: Modifier? = null) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url) {
        bitmap = url?.let { loadBitmapFromUrl(it, width, height) }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            modifier = modifier?: Modifier,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = Modifier.then(modifier ?: Modifier).background(color = androidx.compose.ui.graphics.Color.LightGray)) // 로딩 placeholder
    }
}
