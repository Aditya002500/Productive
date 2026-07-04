package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CollectionItem(
    val title: String,
    val count: Int,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    onBack: () -> Unit,
    onNavigateToSearch: (String) -> Unit
) {
    val collections = listOf(
        CollectionItem("Work", 12, Icons.Default.Folder, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer), // Emerald
        CollectionItem("Study", 8, Icons.Default.School, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primaryContainer), // Lime
        CollectionItem("Personal", 24, Icons.Default.Home, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer), // Teal
        CollectionItem("Travel", 5, Icons.Default.Flight, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer), // Green
        CollectionItem("Research", 19, Icons.Default.Science, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primaryContainer), // Light Lime
        CollectionItem("Shopping", 3, Icons.Default.ShoppingCart, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer) // Green variation
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections", fontWeight = FontWeight.SemiBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(collections) { collection ->
                CollectionCard(collection, onNavigateToSearch)
            }
        }
    }
}

@Composable
fun CollectionCard(item: CollectionItem, onNavigateToSearch: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onNavigateToSearch(item.title) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(item.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(24.dp))
            }

            Column {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.count} items",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
