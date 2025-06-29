package pl.twojprojekt.dziennik.ui.screens // Dostosuj pakiet

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile // Ikona dla wyboru pliku
import androidx.compose.material.icons.filled.Image // Ikona dla wyboru obrazu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import pl.sjanda.jpamietnik.ui.viewmodel.JournalEntryEditViewModel
import pl.sjanda.jpamietnik.ui.viewmodel.JournalEntryFormState
import java.io.IOException
import java.util.Locale
import pl.sjanda.jpamietnik.R

// --- Główne funkcje Composable ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun JournalEditScreen(
    viewModel: JournalEntryEditViewModel,
    onNavigateUp: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val context = LocalContext.current

    // Launcher do wyboru zdjęcia z galerii/plików
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoUriChange(it) }
    }

    // Launcher do wyboru pliku audio
    // PickVisualMedia może nie być idealny dla wszystkich typów audio, GetContent jest bardziej generyczny
    // Można spróbować z typem MIME: PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType("audio/*"))
    // Ale GetContent jest pewniejszy dla plików ogólnych.
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // Bardziej generyczny wybór plików
        // Alternatywnie, jeśli chcesz spróbować z PickVisualMedia dla spójności UI:
        // contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onVoiceRecordingUriChange(it) }
    }

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            onSaveComplete()
        }
    }

    // --- Permisje (tylko lokalizacja) ---
    val locationPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val permissionsState = rememberMultiplePermissionsState(permissions = locationPermissions)

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        } else {
            if (formState.locationName.isBlank() && formState.isNewEntry) {
                getCurrentLocation(context) { cityName ->
                    viewModel.onLocationChange(cityName ?: "cityName")
                }
            }
        }
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted && formState.locationName.isBlank() && formState.isNewEntry) {
            getCurrentLocation(context) { cityName ->
                viewModel.onLocationChange(cityName ?: "nieznana lokalizacja")
            }
        }
    }

    Scaffold(
        topBar = {
            JournalEntryEditTopAppBar(
                isNewEntry = formState.isNewEntry,
                onNavigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.saveJournalEntry() }) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = "desc"
                )
            }
        }
    ) { paddingValues ->
        if (formState.isLoadingInitial) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            JournalEntryForm(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                formState = formState,
                onNoteChange = viewModel::onNoteChange,
                onPickPhotoClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onPickAudioClick = {
                    audioPickerLauncher.launch("audio/*") // Użyj typu MIME dla GetContent
                    // Jeśli używasz PickVisualMedia dla audio:
                    // audioPickerLauncher.launch(
                    //     PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType("audio/*"))
                    // )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalEntryEditTopAppBar(
    isNewEntry: Boolean,
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = {
            Text((if (isNewEntry) "add title bar" else "edit title bar"))
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "desc"
                )
            }
        }
    )
}

@Composable
private fun JournalEntryForm(
    modifier: Modifier = Modifier,
    formState: JournalEntryFormState,
    onNoteChange: (String) -> Unit,
    onPickPhotoClick: () -> Unit,
    onPickAudioClick: () -> Unit
) {
    Column(modifier = modifier) {
        LocationInfoField(locationName = formState.locationName)
        Spacer(modifier = Modifier.height(16.dp))

        NoteField(
            note = formState.note,
            onNoteChange = onNoteChange,
            noteError = formState.noteError
        )
        Spacer(modifier = Modifier.height(16.dp))

        PhotoPickerField( // Zmieniona nazwa
            photoUri = formState.photoUri,
            onPickPhotoClick = onPickPhotoClick
        )
        Spacer(modifier = Modifier.height(16.dp))

        AudioPickerField( // Zmieniona nazwa
            voiceRecordingUri = formState.voiceRecordingUri,
            onPickAudioClick = onPickAudioClick
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (formState.isSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        formState.saveError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// --- Komponenty pól formularza (zmodyfikowane) ---

@Composable
private fun LocationInfoField(locationName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(id = R.drawable.paris_1), // Użyj swojej ikony
            contentDescription ="marker",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (locationName.isNotBlank()) locationName else "location",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteField(
    note: String,
    onNoteChange: (String) -> Unit,
    noteError: String?
) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        label = { Text("note") },
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp),
        isError = noteError != null,
        supportingText = { noteError?.let { Text(it) } }
    )
}

@Composable
private fun PhotoPickerField(
    photoUri: Uri?,
    onPickPhotoClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = onPickPhotoClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Image, contentDescription = "photo action")
            Spacer(Modifier.width(8.dp))
            Text("select photo")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onPickPhotoClick),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Selected photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.paris_1),
                    placeholder = painterResource(id = R.drawable.paris_1)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.paris_1),
                    contentDescription = "placeholder",
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioPickerField(
    voiceRecordingUri: Uri?,
    onPickAudioClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = onPickAudioClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.AttachFile, contentDescription = "audio action") // Zmieniona ikona
            Spacer(Modifier.width(8.dp))
            Text("select audio file") // Zmieniony tekst
        }
        voiceRecordingUri?.let { uri ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
//                stringResource(R.string.selected_audio_file, uri.lastPathSegment ?: "audio_file"),
                text = uri.lastPathSegment ?: "audio_file",
                style = MaterialTheme.typography.bodyMedium
            )
            // Można dodać prosty odtwarzacz lub informację o pliku, jeśli potrzebne
        }
    }
}

// --- Funkcje pomocnicze (getCurrentLocation pozostaje bez zmian) ---

@Suppress("MissingPermission")
private fun getCurrentLocation(context: Context, onResult: (String?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            onResult(addresses.firstOrNull()?.locality ?: addresses.firstOrNull()?.subAdminArea)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        onResult(addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subAdminArea)
                    }
                } catch (e: IOException) {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener {
            onResult(null)
        }
}