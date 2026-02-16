package com.zettl.vocabuhero.ui.deck

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zettl.vocabuhero.data.db.CardEntity

@Composable
fun DeckScreen(
    viewModel: DeckViewModel,
    onCardClick: (Long) -> Unit,
    onAddCard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(uiState.navigateToAddCard) {
        if (uiState.navigateToAddCard) {
            viewModel.clearNavigateToAddCard()
            onAddCard()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        uiState.deck?.let { deck ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        deck.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "${deck.frontLang} → ${deck.backLang}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "Practice direction",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !deck.practiceReversed,
                            onClick = { viewModel.setPracticeReversed(false) },
                            label = { Text("${deck.frontLang} → ${deck.backLang}") }
                        )
                        FilterChip(
                            selected = deck.practiceReversed,
                            onClick = { viewModel.setPracticeReversed(true) },
                            label = { Text("${deck.backLang} → ${deck.frontLang}") }
                        )
                    }
                    if (!uiState.isCurrentDeck) {
                        Button(
                            onClick = { viewModel.setAsCurrentDeck() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                "Set as current deck",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.onAddCardClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 14.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            "Add word",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.showResetProgressDialog(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 14.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            "Reset progress",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.showResetProgressDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showResetProgressDialog(false) },
                title = { Text("Reset progress") },
                text = {
                    Text("Reset all SRS progress and review history for this deck? Cards will appear as new again. This cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.resetProgress() }
                    ) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showResetProgressDialog(false) }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }

        if (uiState.cardToDelete != null) {
            AlertDialog(
                onDismissRequest = { viewModel.showDeleteCardDialog(null) },
                title = { Text("Delete word") },
                text = {
                    val c = uiState.cardToDelete!!
                    Text("Delete \"${c.frontText}\" — \"${c.backText}\"? This cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            uiState.cardToDelete?.let { viewModel.deleteCard(it) }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.showDeleteCardDialog(null) }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.cards, key = { it.id }) { card ->
                DeckCard(
                    card = card,
                    onClick = { onCardClick(card.id) },
                    onDelete = { viewModel.showDeleteCardDialog(card) }
                )
            }
        }
    }
}

@Composable
private fun DeckCard(
    card: CardEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = card.frontText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    card.backText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete word",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
