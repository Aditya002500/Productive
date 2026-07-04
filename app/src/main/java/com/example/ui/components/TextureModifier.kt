package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TexturedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Removed the heavy noise grain per user request for a cleaner, modern look.
    Box(modifier = modifier.fillMaxSize()) {
        content()
    }
}
