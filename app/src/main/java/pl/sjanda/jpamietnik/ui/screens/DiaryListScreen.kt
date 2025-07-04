package pl.sjanda.jpamietnik.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.sjanda.jpamietnik.R
import pl.sjanda.jpamietnik.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryListScreen(
    onCreateEntry: () -> Unit,
    onEditEntry: (String) -> Unit,
    onViewEntry: (String) -> Unit,
    onOpenMap: () -> Unit,
    viewModel: DiaryViewModel = viewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var entryToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadEntries()
    }

    if (showDeleteDialog && entryToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                entryToDelete = null
            },
            title = { Text(stringResource(R.string.delete_entry_title)) },
            text = { Text(stringResource(R.string.delete_entry_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        entryToDelete?.let { id ->
                            viewModel.deleteEntry(
                                id, onSuccess = {
                                    showDeleteDialog = false
                                    entryToDelete = null
                                },
                                onError = {
                                    //tbd
                                })
                        }
                    }
                ) {
                    Text(stringResource(R.string.delete_entry_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        entryToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete_entry_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.diary_title)) },
                actions = {
                    IconButton(onClick = onOpenMap) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = stringResource(R.string.diary_map)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEntry
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.diary_add_entry)
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(items = entries, key = { it.id }) { entry ->
                    DiaryEntryCard(
                        entry = entry,
                        onClick = { onViewEntry(entry.id) },
                        onEdit = { onEditEntry(entry.id) },
                        onLongPress = {
                            entryToDelete = entry.id
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
}