package com.grensil.ui.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager


@Composable
fun DismissKeyboardOnTouch(content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }.pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    Log.d("Logd","dragAmount: $dragAmount")
                    focusManager.clearFocus()
                }
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        content()
    }
}

