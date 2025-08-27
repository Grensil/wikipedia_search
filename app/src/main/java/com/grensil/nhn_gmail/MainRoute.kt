package com.grensil.nhn_gmail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TabScreen(val route: String) {
    object Search : TabScreen("search")
    object Detail : TabScreen("detail")
}