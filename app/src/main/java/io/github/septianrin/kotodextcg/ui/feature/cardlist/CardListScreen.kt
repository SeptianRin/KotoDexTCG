@file:OptIn(ExperimentalFoundationApi::class)

package io.github.septianrin.kotodextcg.ui.feature.cardlist

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.septianrin.kotodextcg.R
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.ui.component.CardDetailOverlay
import io.github.septianrin.kotodextcg.ui.component.CardListItem
import io.github.septianrin.kotodextcg.ui.component.ErrorDialog
import io.github.septianrin.kotodextcg.ui.component.LoadingIndicator
import org.koin.androidx.compose.koinViewModel

@Composable
fun CardListScreen(
    modifier: Modifier = Modifier,
    listState: LazyGridState,
    onCardClicked: (String) -> Unit,
    viewModel: CardListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTcgCard by remember { mutableStateOf<TcgCard?>(null) }

    val isAtBottom = !listState.canScrollForward

    LaunchedEffect(isAtBottom) {
        if (isAtBottom && !uiState.isLoading) {
            viewModel.handleEvent(CardListEvent.LoadNextPage)
        }
    }

    LaunchedEffect(uiState.isLoadingNextPage) {
        if (uiState.isLoadingNextPage) {
            val lastIndex = uiState.tcgCards.size - 1
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
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = listState,
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.matchParentSize()
                ) {
                    items(uiState.tcgCards.size) { index ->
                        val card = uiState.tcgCards[index]
                        CardListItem(
                            tcgCard = card,
                            onClick = { onCardClicked(card.id) },
                            modifier = Modifier.animateItemPlacement(
                                tween(durationMillis = 300)
                            )
                        )
                    }

                    if (uiState.isLoadingNextPage) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingIndicator()
                        }
                    }
                }
            }
        }
        uiState.error?.let { errorMessage ->
            ErrorDialog(
                onDismissRequest = { viewModel.handleEvent(CardListEvent.ClearError) },
                onConfirmation = {
                    viewModel.handleEvent(CardListEvent.ClearError)
                    viewModel.handleEvent(CardListEvent.LoadFirstPage)
                },
                dialogTitle = stringResource(R.string.network_error),
                dialogText = errorMessage
            )
        }
        if (selectedTcgCard != null) {
            CardDetailOverlay(
                tcgCard = selectedTcgCard!!,
                onDismiss = { selectedTcgCard = null }
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
        label = { Text(stringResource(R.string.search_hint)) },
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
