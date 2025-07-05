package io.github.septianrin.kotodextcg.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.septianrin.kotodextcg.data.model.Card
import io.github.septianrin.kotodextcg.ui.component.CardDetailOverlay
import io.github.septianrin.kotodextcg.ui.component.CardListItem
import io.github.septianrin.kotodextcg.ui.viewmodel.CardListEvent
import io.github.septianrin.kotodextcg.ui.viewmodel.CardListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardListScreen(
    modifier: Modifier = Modifier,
    listState: LazyGridState,
    viewModel: CardListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCard by remember { mutableStateOf<Card?>(null) }

    val isAtBottom = !listState.canScrollForward

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && !uiState.isLoading) {
            viewModel.handleEvent(CardListEvent.LoadNextPage)
        }
    }

    LaunchedEffect(uiState.isLoadingNextPage) {
        if (uiState.isLoadingNextPage) {
            val lastIndex = uiState.cards.size - 1
            if (lastIndex >= 0) {
                listState.animateScrollToItem(lastIndex)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChanged = { viewModel.handleEvent(CardListEvent.OnSearchQueryChanged(it)) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    Text(
                        text = "An error occurred:\n${uiState.error}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                uiState.cards.isEmpty() -> {
                    val message = if (uiState.searchQuery.isNotBlank()) {
                        "No results found for \"${uiState.searchQuery}\""
                    } else {
                        "No Pokémon cards found."
                    }
                    Text(
                        text = message,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = listState,
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.matchParentSize()
                    ) {
                        items(uiState.cards.size) { index ->
                            val card = uiState.cards[index]
                            CardListItem(card = card) { selectedCard = card }
                        }

                        if (uiState.isLoadingNextPage) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                LoadingIndicator()
                            }
                        }
                    }
                }
            }

        }
        if (selectedCard != null) {
            CardDetailOverlay(
                card = selectedCard!!,
                onDismiss = { selectedCard = null }
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        label = { Text("Search Pokémon Name, Type...") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
        })
    )
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
