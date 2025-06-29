package pl.sjanda.jpamietnik.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.sjanda.jpamietnik.data.JournalEntry
import pl.sjanda.jpamietnik.ui.viewmodel.JournalDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(
    viewModel: JournalDetailViewModel,
    onNavigateUp: () -> Unit
) {
    val journal by viewModel.journal.collectAsState()

    Scaffold(
        topBar = {
            MovieDetailTopAppBar(
                onNavigateUp = onNavigateUp
            )
        }
    ) { paddingValues ->
        val currentMovie = journal

        if (currentMovie == null) {
            EmptyMovieDetailsPlaceholder(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            MovieDetailsContent(
                journal = currentMovie,
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailTopAppBar(
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = { Text("Details") },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Powrót",
                )
            }
        }
    )
}

@Composable
private fun MovieDetailsContent(
    journal: JournalEntry,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(16.dp))

    }
}

@Composable
private fun EmptyMovieDetailsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text("nout found")
    }
}