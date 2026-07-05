package com.example.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Single persistent top bar shared by all 5 main tabs (Home/Planner/Capture/Notes/Search).
 * Lives above the HorizontalPager in MainTabsScreen's Scaffold so it never remounts on
 * swipe — only [title] recomposes as the current page changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureFlowTopBar(
    title: String,
    onOpenDrawer: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
