package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocationTracker
import com.example.ui.AppViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackHabitScreen(
    habitId: Int,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val habit = habits.find { it.id == habitId }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // The tracking session lives in TrackingService, not this composable, so it keeps
    // running (and posts a notification) even after the user navigates away or exits
    // the app entirely — see the floating tracking bar in MainActivity.
    LaunchedEffect(habitId) { viewModel.prepareTrackingSession(habitId) }
    val recenterTracker = remember { LocationTracker(context) }
    val isTracking by viewModel.trackingIsRunning.collectAsStateWithLifecycle()
    val secondsElapsed by viewModel.trackingSeconds.collectAsStateWithLifecycle()
    val distanceMeters by viewModel.trackingDistanceMeters.collectAsStateWithLifecycle()
    val path by viewModel.trackingPath.collectAsStateWithLifecycle()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 4f)
    }

    fun recenterOnUser() {
        recenterTracker.getLastLocation { location ->
            location?.let {
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 17f)
                    )
                }
            }
        }
    }

    val trackingPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) viewModel.startTrackingUpdates()
    }

    // Center on the user as soon as we have permission, instead of the India-wide default.
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) recenterOnUser()
    }

    if (habit == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val distanceKm = distanceMeters / 1000f
    var note by remember { mutableStateOf("") }

    LaunchedEffect(path.lastOrNull()) {
        path.lastOrNull()?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track ${habit.title}", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
                ) {
                    if (path.size >= 2) {
                        Polyline(points = path, color = MaterialTheme.colorScheme.primary, width = 10f)
                    }
                    path.lastOrNull()?.let { Marker(state = MarkerState(position = it), title = "Current position") }
                }
                if (path.isEmpty()) {
                    Text(
                        "Start tracking to see your route",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(12.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                FilledIconButton(
                    onClick = {
                        if (hasLocationPermission) recenterOnUser()
                        else permissionLauncher.launch(trackingPermissions)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Go to my location")
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrackStat(label = "Time", value = "%02d:%02d".format(secondsElapsed / 60, secondsElapsed % 60))
                TrackStat(label = "Distance", value = "%.2f km".format(distanceKm))
            }

            Spacer(Modifier.height(24.dp))

            val buttonLabel = when {
                isTracking -> "Pause"
                distanceMeters > 0f -> "Resume"
                else -> "Start Tracking"
            }
            Button(onClick = {
                if (isTracking) {
                    viewModel.pauseTrackingUpdates()
                } else if (hasLocationPermission) {
                    viewModel.startTrackingUpdates()
                } else {
                    permissionLauncher.launch(trackingPermissions)
                }
            }) {
                Icon(
                    if (isTracking) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(buttonLabel)
            }
            if (!hasLocationPermission) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Location permission is needed to measure distance automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.endTrackingSession()
                    viewModel.completeHabitToday(habit, distanceKm, note)
                    onBack()
                },
                enabled = distanceMeters > 0f,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun TrackStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
