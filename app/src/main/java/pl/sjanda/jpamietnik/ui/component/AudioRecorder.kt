package pl.sjanda.jpamietnik.ui.component

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.delay
import pl.sjanda.jpamietnik.R
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
            mediaRecorder = MediaRecorder(context)

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
        }
    }

    fun pauseRecording() {
        mediaRecorder?.pause()
        isPaused = true
    }

    fun resumeRecording() {
        mediaRecorder?.resume()
        isPaused = false
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
                        text = stringResource(R.string.audio_recorder_permission_needed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { permissionState.launchPermissionRequest() }
                    ) {
                        Text(stringResource(R.string.audio_recorder_give_permission))
                    }
                }
            } else {
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
                        FilledIconButton(
                            onClick = { startRecording() },
                            modifier = Modifier.size(64.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = stringResource(R.string.audio_recorder_start_recording),
                                modifier = Modifier.size(32.dp),
                                tint = Color.White
                            )
                        }
                    }

                    if (isRecording) {
                        IconButton(
                            onClick = {
                                if (isPaused) resumeRecording() else pauseRecording()
                            }
                        ) {
                            Icon(
                                if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (isPaused) stringResource(R.string.resume) else stringResource(
                                    R.string.pause
                                )
                            )
                        }
                        FilledIconButton(
                            onClick = { stopRecording() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = stringResource(R.string.audio_recording_stop_recording)
                            )
                        }
                    }

                    if (hasRecording) {
                        IconButton(
                            onClick = { discardRecording() }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.audio_recording_decline),
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
                                contentDescription = stringResource(R.string.audio_recording_save),
                                tint = Color.White
                            )
                        }
                    }
                }

                if (isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPaused) stringResource(R.string.audo_recording_paused) else stringResource(
                            R.string.audio_recording_in_progress
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPaused) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}