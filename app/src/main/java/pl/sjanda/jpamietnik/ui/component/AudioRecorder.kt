package pl.sjanda.jpamietnik.ui.screens

import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioRecorder(
    onAudioRecorded: (Uri) -> Unit,
    permissionState: PermissionState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0L) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }
    var hasRecording by remember { mutableStateOf(false) }

    // Timer dla czasu nagrywania
    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (isRecording && !isPaused) {
                delay(1000)
                recordingTime += 1000
            }
        }
    }

    fun formatTime(timeMs: Long): String {
        val seconds = (timeMs / 1000) % 60
        val minutes = (timeMs / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun createOutputFile(): File {
        val audioDir = File(context.filesDir, "audio")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        return File(audioDir, "recording_${System.currentTimeMillis()}.m4a")
    }

    fun startRecording() {
        try {
            outputFile = createOutputFile()
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)

                prepare()
                start()
            }

            isRecording = true
            recordingTime = 0L
        } catch (e: IOException) {
            e.printStackTrace()
            // Obsługa błędu nagrywania
        }
    }

    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            isPaused = true
        }
    }

    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            isPaused = false
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            isPaused = false
            hasRecording = true
        } catch (e: RuntimeException) {
            e.printStackTrace()
            // Obsługa błędu zatrzymywania nagrywania
        }
    }

    fun saveRecording() {
        outputFile?.let { file ->
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            onAudioRecorded(uri)
            hasRecording = false
            recordingTime = 0L
        }
    }

    fun discardRecording() {
        outputFile?.delete()
        outputFile = null
        hasRecording = false
        recordingTime = 0L
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!permissionState.status.isGranted) {
                // Prośba o uprawnienia
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wymagane uprawnienie do mikrofonu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { permissionState.launchPermissionRequest() }
                    ) {
                        Text("Udziel uprawnienia")
                    }
                }
            } else {
                // Interfejs nagrywania
                Text(
                    text = formatTime(recordingTime),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isRecording && !isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRecording && !hasRecording) {
                        // Przycisk rozpoczęcia nagrywania
                        FilledIconButton(
                            onClick = { startRecording() },
                            modifier = Modifier.size(64.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Rozpocznij nagrywanie",
                                modifier = Modifier.size(32.dp),
                                tint = Color.White
                            )
                        }
                    }

                    if (isRecording) {
                        // Przycisk pauzy/wznowienia (tylko dla Android N+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            IconButton(
                                onClick = {
                                    if (isPaused) resumeRecording() else pauseRecording()
                                }
                            ) {
                                Icon(
                                    if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (isPaused) "Wznów" else "Pauzuj"
                                )
                            }
                        }

                        // Przycisk zatrzymania
                        FilledIconButton(
                            onClick = { stopRecording() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Zatrzymaj nagrywanie"
                            )
                        }
                    }

                    if (hasRecording) {
                        // Przyciski zapisz/odrzuć
                        IconButton(
                            onClick = { discardRecording() }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Odrzuć nagranie",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        FilledIconButton(
                            onClick = { saveRecording() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Zapisz nagranie",
                                tint = Color.White
                            )
                        }
                    }
                }

                if (isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPaused) "Nagrywanie wstrzymane" else "Nagrywanie...",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}