package pl.sjanda.jpamietnik.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import pl.sjanda.jpamietnik.R
import pl.sjanda.jpamietnik.data.DiaryEntry
import pl.sjanda.jpamietnik.ui.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryMapScreen(
    onBackClick: () -> Unit,
    onEntryClick: (String) -> Unit,
    viewModel: DiaryViewModel = viewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var selectedEntry by remember { mutableStateOf<DiaryEntry?>(null) }

    val entriesWithLocation = entries.filter {
        it.location.latitude != 0.0 && it.location.longitude != 0.0
    }

    LaunchedEffect(Unit) {
        viewModel.loadEntries()
    }

    LaunchedEffect(entriesWithLocation) {
        googleMap?.let { map ->
            addMarkersToMap(map, entriesWithLocation) { entry ->
                selectedEntry = entry
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diary_map)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            googleMap?.let { map ->
                                if (entriesWithLocation.isNotEmpty()) {
                                    fitMapToMarkers(map, entriesWithLocation)
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.CenterFocusStrong,
                            contentDescription = "Center"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            getMapAsync { map ->
                                googleMap = map
                                map.uiSettings.isZoomControlsEnabled = true
                                map.uiSettings.isCompassEnabled = true
                                map.uiSettings.isMapToolbarEnabled = false

                                if (entriesWithLocation.isEmpty()) {
                                    val warsaw = LatLng(52.2297, 21.0122)
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 10f))
                                } else {
                                    addMarkersToMap(map, entriesWithLocation) { entry ->
                                        selectedEntry = entry
                                    }
                                    fitMapToMarkers(map, entriesWithLocation)
                                }
                            }
                            mapView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { mapView ->
                    mapView.onResume()
                }

                selectedEntry?.let { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = SimpleDateFormat(
                                            "dd.MM.yyyy HH:mm",
                                            Locale.getDefault()
                                        )
                                            .format(entry.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (entry.location.address.isNotEmpty() == true) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = "Location",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = entry.location.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (entry.content.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = entry.content.take(100) + if (entry.content.length > 100) "..." else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { selectedEntry = null }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedEntry = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.map_close))
                                }

                                Button(
                                    onClick = {
                                        onEntryClick(entry.id)
                                        selectedEntry = null
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.map_check_entry))
                                }
                            }
                        }
                    }
                }

                if (entriesWithLocation.isEmpty() && !isLoading) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.Center),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.LocationOff,
                                contentDescription = "No locations",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.map_no_entries_with_loc),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.map_add_entries_with_loc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDestroy()
        }
    }
}

private fun addMarkersToMap(
    map: GoogleMap,
    entries: List<DiaryEntry>,
    onMarkerClick: (DiaryEntry) -> Unit
) {
    map.clear()
    entries.forEach { entry ->
        entry.location.let { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(entry.title)
                    .snippet(entry.content.take(50) + if (entry.content.length > 50) "..." else "")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            marker?.tag = entry
        }
    }
    map.setOnMarkerClickListener { marker ->
        val entry = marker.tag as? DiaryEntry
        entry?.let { onMarkerClick(it) }
        true
    }
}

private fun fitMapToMarkers(map: GoogleMap, entries: List<DiaryEntry>) {
    if (entries.isEmpty()) return
    val builder = LatLngBounds.Builder()
    entries.forEach { entry ->
        entry.location.let { location ->
            builder.include(LatLng(location.latitude, location.longitude))
        }
    }
    val bounds = builder.build()
    val padding = 100
    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
}