@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.septianrin.kotodextcg.ui.feature.carddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.septianrin.kotodextcg.R
import io.github.septianrin.kotodextcg.data.model.Attack
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.ui.component.CardDetailOverlay
import io.github.septianrin.kotodextcg.ui.component.CardListItem
import io.github.septianrin.kotodextcg.ui.component.ErrorDialog
import io.github.septianrin.kotodextcg.util.PokemonType
import org.koin.androidx.compose.koinViewModel


@Composable
fun CardDetailScreen(
    viewModel: CardDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showZoomableOverlay by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.tcgCard != null) {
            val card = uiState.tcgCard!!
            CardDetailContent(
                card = card,
                onImageClicked = { showZoomableOverlay = true }
            )
        }

        uiState.error?.let { errorMessage ->
            ErrorDialog(
                onDismissRequest = { viewModel.handleEvent(CardDetailEvent.ClearError) },
                onConfirmation = {
                    viewModel.handleEvent(CardDetailEvent.ClearError)
                    viewModel.handleEvent(CardDetailEvent.LoadDetails)
                },
                dialogTitle = stringResource(R.string.load_error),
                dialogText = errorMessage
            )
        }

        if (showZoomableOverlay && uiState.tcgCard != null) {
            CardDetailOverlay(
                tcgCard = uiState.tcgCard!!,
                onDismiss = { showZoomableOverlay = false }
            )
        }
    }
}

@Composable
fun CardDetailContent(card: TcgCard, onImageClicked: () -> Unit) {
    val primaryType = PokemonType.fromString(card.types?.firstOrNull().orEmpty())
    val backgroundBrush = getTypeGradientBrush(primaryType, MaterialTheme.colorScheme.surface)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CardListItem(
                tcgCard = card,
                onClick = onImageClicked,
                modifier = Modifier.fillMaxWidth(0.7f),
                showZoomIcon = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = card.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = stringResource(R.string.stats, card.hp ?: "N/A", card.rarity ?: "N/A"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            card.flavorText?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            card.attacks?.let { attacks ->
                DetailSection(title = stringResource(R.string.attacks)) {
                    attacks.forEachIndexed { index, attack ->
                        AttackInfo(attack)
                        if (index < attacks.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth()) {
                card.weaknesses?.let { weaknesses ->
                    DetailSection(title = stringResource(R.string.weaknesses), modifier = Modifier.weight(1f)) {
                        weaknesses.forEach { weakness ->
                            Text(
                                stringResource(
                                    R.string.weakness_data,
                                    weakness.type,
                                    weakness.value
                                ), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                card.resistances?.let { resistances ->
                    DetailSection(title = stringResource(R.string.resistances), modifier = Modifier.weight(1f)) {
                        resistances.forEach { resistance ->
                            Text(
                                stringResource(
                                    R.string.resistance_data,
                                    resistance.type,
                                    resistance.value
                                ), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun AttackInfo(attack: Attack) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = attack.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                attack.cost.forEach { energyTypeString ->
                    EnergyIcon(energyType = PokemonType.fromString(energyTypeString), modifier = Modifier.padding(horizontal = 2.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = attack.damage,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        if (attack.text.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = attack.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EnergyIcon(energyType: PokemonType, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(energyType.color)
            .padding(2.dp)
    )
}

@Composable
private fun getTypeGradientBrush(type: PokemonType, surface: Color): Brush {
    val baseColor = type.color
    return Brush.verticalGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.6f),
            surface
        )
    )
}