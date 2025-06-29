package pl.sjanda.jpamietnik.ui.component

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.sjanda.jpamietnik.R
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditor(
    imageUri: Uri,
    onImageEdited: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showTextDialog by remember { mutableStateOf(false) }
    var imageText by remember { mutableStateOf("") }
    var tempText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    suspend fun saveImageWithText(): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null

            val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            if (imageText.isNotEmpty()) {
                val textPaint = Paint().apply {
                    color = Color.White.toArgb()
                    textSize = mutableBitmap.width * 0.05f // 5% szerokości obrazu
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    setShadowLayer(4f, 2f, 2f, Color.Black.toArgb()) // Cień dla lepszej czytelności
                }

                val textBounds = Rect()
                textPaint.getTextBounds(imageText, 0, imageText.length, textBounds)
                val textWidth = textBounds.width()
                val textHeight = textBounds.height()

                val x = (mutableBitmap.width - textWidth) / 2f
                val y = mutableBitmap.height - textHeight - (mutableBitmap.height * 0.03f) // 3% margines od dołu

                val backgroundPaint = Paint().apply {
                    color = Color.Black.copy(alpha = 0.6f).toArgb()
                    style = Paint.Style.FILL
                }

                val padding = textHeight * 0.3f
                canvas.drawRoundRect(
                    x - padding,
                    y - textHeight - padding,
                    x + textWidth + padding,
                    y + padding,
                    20f, 20f,
                    backgroundPaint
                )

                canvas.drawText(imageText, x, y, textPaint)
            }

            val editedFile = File(context.cacheDir, "edited_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(editedFile)
            mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                editedFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Zdjęcie do edycji",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                if (imageText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = imageText,
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.6f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempText = imageText
                            showTextDialog = true
                        }
                    ) {
                        Icon(
                            if (imageText.isEmpty()) Icons.Default.Add else Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (imageText.isEmpty()) "Dodaj tekst" else "Edytuj tekst")
                    }

                    if (imageText.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { imageText = "" },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Usuń tekst",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (imageText.isNotEmpty()) {
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                val editedUri = saveImageWithText()
                                isLoading = false
                                editedUri?.let { uri ->
                                    onImageEdited(uri)
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Zapisz")
                        }
                    }
                }
            }
        }

        if (showTextDialog) {
            AlertDialog(
                onDismissRequest = { showTextDialog = false },
                title = {
                    Text(if (imageText.isEmpty()) "Dodaj tekst do zdjęcia" else "Edytuj tekst")
                },
                text = {
                    Column {
                        Text(
                            text = "Tekst zostanie dodany na dole zdjęcia:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempText,
                            onValueChange = { tempText = it },
                            label = { Text("Wpisz tekst") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            placeholder = { Text("Np. Wakacje 2024, Kraków") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            imageText = tempText
                            showTextDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTextDialog = false }
                    ) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}