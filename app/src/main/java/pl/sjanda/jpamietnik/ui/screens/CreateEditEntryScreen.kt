package pl.sjanda.jpamietnik.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import pl.sjanda.jpamietnik.R
import pl.sjanda.jpamietnik.data.DiaryEntry
import pl.sjanda.jpamietnik.data.Location
import pl.sjanda.jpamietnik.ui.component.AudioRecorder
import pl.sjanda.jpamietnik.ui.component.ImageEditor
import pl.sjanda.jpamietnik.ui.viewmodel.DiaryViewModel
import pl.sjanda.jpamietnik.util.LocationPicker
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateEditEntryScreen(
    entryId: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    viewModel: DiaryViewModel = viewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val currentEntry by viewModel.currentEntry.collectAsState()

    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.loadEntry(entryId)
        }
    }

    LaunchedEffect(currentEntry) {
        currentEntry?.let { entry ->
            title = entry.title
            content = entry.content
            selectedLocation = entry.location
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (entryId == null) stringResource(R.string.new_entry) else stringResource(
                            R.string.edit_entry
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.entry_back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                val entry = DiaryEntry(
                                    id = entryId ?: "",
                                    title = title,
                                    content = content,
                                    location = selectedLocation ?: Location(),
                                    imageUrl = selectedImageUri?.toString(),
                                    audioUrl = selectedAudioUri?.toString(),
                                    createdAt = currentEntry?.createdAt ?: Date(),
                                    updatedAt = Date(),
                                )

                                if (entryId == null) {
                                    viewModel.saveEntry(entry, onSave) { error ->

                                    }
                                } else {
                                    viewModel.updateEntry(entry, onSave) { error ->

                                    }
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.edit_entry_save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.entry_title)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(stringResource(R.string.entry_content)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                maxLines = 10
            )

            LocationPicker(
                selectedLocation = selectedLocation,
                onLocationSelected = { selectedLocation = it },
                locationPermissionState = locationPermissionState
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Zdjęcie", style = MaterialTheme.typography.titleMedium)
                        IconButton(
                            onClick = {
                                if (cameraPermissionState.status.isGranted) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.entry_add_photo)
                            )
                        }
                    }

                    if (selectedImageUri != null) {
                        ImageEditor(
                            imageUri = selectedImageUri!!,
                            onImageEdited = { editedUri ->
                                selectedImageUri = editedUri
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.entry_audio_record),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AudioRecorder(
                        onAudioRecorded = { audioUri ->
                            selectedAudioUri = audioUri
                        },
                        permissionState = audioPermissionState
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}