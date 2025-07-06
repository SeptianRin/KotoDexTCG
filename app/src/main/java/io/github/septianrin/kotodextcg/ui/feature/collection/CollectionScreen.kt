package io.github.septianrin.kotodextcg.ui.feature.collection

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.septianrin.kotodextcg.ui.component.CardListItem
import io.github.septianrin.kotodextcg.ui.component.ErrorDialog
import io.github.septianrin.kotodextcg.ui.component.InfoState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionScreen(
    onCardClicked: (String) -> Unit,
    onGoToGachaClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel()
) {
    val collectedCards by viewModel.collectionState.collectAsState()
    var showClearConfirmationDialog by remember { mutableStateOf(false) }

    if (showClearConfirmationDialog) {
        ErrorDialog(
            onDismissRequest = { showClearConfirmationDialog = false },
            onConfirmation = {
                viewModel.handleEvent(CollectionEvent.ClearCollection)
                showClearConfirmationDialog = false
            },
            dialogTitle = "Clear Collection?",
            dialogText = "Are you sure you want to permanently delete your entire card collection?",
            icon = Icons.Default.DeleteForever,
            confirmButtonText = "Clear",
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (collectedCards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                InfoState(
                    icon = Icons.Default.Inbox,
                    title = "Collection is Empty",
                    message = "Open some booster packs in the Gacha screen to start your collection!",
                    modifier = Modifier.padding(bottom = 64.dp)
                )
                Button(
                    onClick = onGoToGachaClicked,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Go to Gacha")
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { showClearConfirmationDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Collection", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Clear Collection")
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = collectedCards,
                    key = { card -> "${card.id}-${card.count}" }
                ) { card ->
                    CardListItem(
                        tcgCard =  card,
                        onClick = { onCardClicked(card.id) },
                        modifier = Modifier.animateItemPlacement()
                    )
                }
            }
        }
    }
}