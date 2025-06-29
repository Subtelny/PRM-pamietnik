package pl.sjanda.jpamietnik.ui.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.sjanda.jpamietnik.R
import pl.sjanda.jpamietnik.data.JournalEntry
import pl.sjanda.jpamietnik.ui.viewmodel.JournalListViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(
    viewModel: JournalListViewModel,
    onAddJournalClick: () -> Unit,
    onJournalClick: (journalId: String) -> Unit,
    onJournalLongClick: (journalId: String) -> Unit,
) {

    val journals by viewModel.journals.collectAsState()

    Scaffold(
        topBar = { JournalListTopAppBar() },
        floatingActionButton = {
            JournalListFloatingActionButton(onAddJournalClick = onAddJournalClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            if (journals.isEmpty()) {
                Log.d("JournalListScreen", "No journals found")
            } else {
                Log.d("JournalListScreen", "Found ${journals.size} journals")
            }

            if (journals.isEmpty()) {
                EmptyMovieListPlaceholder(modifier = Modifier.fillMaxSize())
            } else {
                JournalListContent(
                    journals = journals,
                    onJournalClick = onJournalClick,
                    onJournalLongClick = onJournalLongClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalListTopAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(id = R.drawable.avatar_express),
                    contentDescription = "test",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.app_name))
            }
        },
    )
}

@Composable
private fun JournalListFloatingActionButton(onAddJournalClick: () -> Unit) {
    FloatingActionButton(onClick = onAddJournalClick) {
        Icon(Icons.Filled.Add, contentDescription = "add")
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun JournalListContent(
    journals: List<JournalEntry>,
    onJournalClick: (journalId: String) -> Unit,
    onJournalLongClick: (journalId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = journals, key = { it.id }) { journal ->
            JournalListItem(
                journal = journal,
                modifier = Modifier.combinedClickable(
                    onClick = { onJournalClick(journal.id) },
                    onLongClick = { onJournalLongClick(journal.id) },
                )
            )
        }
    }
}

@Composable
private fun EmptyMovieListPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("no journals")
    }
}

@Composable
fun JournalListItem(journal: JournalEntry, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            JournalItemDetails(
                journal = journal,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.MovieFilter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun JournalItemDetails(
    journal: JournalEntry,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            journal.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Spacer(modifier = Modifier.height(4.dp))
    }
}
