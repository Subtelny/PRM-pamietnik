package pl.sjanda.jpamietnik.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import pl.sjanda.jpamietnik.R
import pl.sjanda.jpamietnik.ui.viewmodel.DiaryViewModel
import pl.sjanda.jpamietnik.ui.viewmodel.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    onEntryClick: (String) -> Unit,
    viewModel: DiaryViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val entries by viewModel.entries.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(52.2297, 21.0122), 10f)
    }

    LaunchedEffect(Unit) {
        viewModel.loadEntries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_entries)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.map_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            cameraPositionState = cameraPositionState
        ) {
            entries.forEach { entry ->
                if (entry.location.latitude != 0.0 && entry.location.longitude != 0.0) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(entry.location.latitude, entry.location.longitude)
                        ),
                        title = entry.title,
                        snippet = entry.location.city,
                        onClick = {
                            onEntryClick(entry.id)
                            true
                        }
                    )
                }
            }
        }
    }
}