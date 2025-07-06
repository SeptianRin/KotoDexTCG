package io.github.septianrin.kotodextcg.ui.feature.gacha

import android.graphics.Typeface
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.septianrin.kotodextcg.R
import io.github.septianrin.kotodextcg.data.model.TcgCard
import io.github.septianrin.kotodextcg.ui.component.CardDetailOverlay
import io.github.septianrin.kotodextcg.ui.component.CardListItem
import io.github.septianrin.kotodextcg.ui.component.ErrorDialog
import io.github.septianrin.kotodextcg.ui.state.GachaInteractionState
import io.github.septianrin.kotodextcg.util.Rarity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun GachaScreen(
    onCardClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GachaViewModel = koinViewModel()
) {
    val vmState by viewModel.uiState.collectAsState()
    var selectedTcgCard by remember { mutableStateOf<TcgCard?>(null) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (vmState.isPreparing) {
            CircularProgressIndicator()
            Text(
                "Preparing next pack...",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            when (vmState.interactionState) {
                GachaInteractionState.Tearing -> {
                    InteractiveBoosterPack(
                        rarity = Rarity.parseRarity(vmState.packRarity.orEmpty()),
                        onSlashComplete = {
                            viewModel.handleEvent(GachaEvent.ShowResults)
                        }
                    )
                }

                GachaInteractionState.ShowingResults -> {
                    LaunchedEffect(Unit) {
                        viewModel.handleEvent(GachaEvent.SavePulledCards)
                    }
                    Text(
                        "You pulled:",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(vmState.pulledTcgCards.size) { index ->
                            val card = vmState.pulledTcgCards[index]
                            CardListItem(
                                tcgCard = card,
                                onClick = { onCardClicked(card.id) }
                            )
                        }
                    }
                    vmState.error?.let { errorMessage ->
                        ErrorDialog(
                            onDismissRequest = { viewModel.handleEvent(GachaEvent.ClearError) },
                            onConfirmation = {
                                viewModel.handleEvent(GachaEvent.ClearError)
                                viewModel.handleEvent(GachaEvent.PrepareNewPack)
                            },
                            dialogTitle = "Pack Error",
                            dialogText = errorMessage
                        )
                    }

                    if (selectedTcgCard != null) {
                        CardDetailOverlay(
                            tcgCard = selectedTcgCard!!,
                            onDismiss = { selectedTcgCard = null }
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.handleEvent(GachaEvent.PrepareNewPack)
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Open Another Pack")
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveBoosterPack(rarity: Rarity, onSlashComplete: () -> Unit) {
    var isSlashed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var trailPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var trailVisible by remember { mutableStateOf(false) }

    val topPieceOffsetY by animateFloatAsState(
        targetValue = if (isSlashed) -800f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutLinearInEasing),
        label = "topPieceOffset",
        finishedListener = {
            onSlashComplete()
        }
    )
    val packArt = remember {
        listOf(
            R.drawable.eevee,
            R.drawable.magikarp,
            R.drawable.mewtwo,
            R.drawable.pikachu,
            R.drawable.rayquaza,
            R.drawable.snorlax
        ).random()
    }.let { painterResource(id = it) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (!isSlashed) "Slash to tear open!" else "Torn!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .pointerInput(isSlashed) {
                    if (!isSlashed) {
                        var dragStartPos = Offset.Zero
                        var dragEndPos = Offset.Zero
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStartPos = offset
                                trailPoints = listOf(offset)
                                trailVisible = true
                            },
                            onDragEnd = {
                                val dragVector = dragEndPos - dragStartPos
                                val isTopArea = dragStartPos.y < size.height / 2
                                val isSlashGesture =
                                    abs(dragVector.x) > size.width * 0.3
                                val isTolerableStraight =
                                    abs(dragVector.y) < size.height * 0.5

                                if (isTopArea && isSlashGesture && isTolerableStraight) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSlashed = true
                                }
                                coroutineScope.launch {
                                    delay(200)
                                    trailVisible = false
                                }
                            },
                            onDrag = { change, _ ->
                                dragEndPos = change.position
                                trailPoints = trailPoints + change.position
                                change.consume()
                            }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            BoosterPackAsset(
                rarity = rarity,
                topPieceOffsetY = topPieceOffsetY,
                packArt = packArt,
                modifier = Modifier.size(200.dp, 350.dp)
            )
            if (!isSlashed) {
                SlashGuide(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = ((-350).dp / 2) + (350.dp * 0.125f))
                        .width(200.dp)
                        .height(1.dp)
                )
            }
            RainbowTrail(points = trailPoints, isVisible = trailVisible)
        }
    }
}

@Composable
fun RainbowTrail(points: List<Offset>, isVisible: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "trailAlpha"
    )

    // Color Shift
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow-transition")
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow-color-shift"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (points.size > 1) {
            val baseColors = listOf(
                Color.Red,
                Color.Yellow,
                Color.Green,
                Color.Cyan,
                Color.Blue,
                Color.Magenta,
                Color.Red
            )

            val colorOffset = colorShift * baseColors.size

            val brush = Brush.linearGradient(
                colors = baseColors,
                start = Offset(points.first().x - colorOffset * 100, points.first().y),
                end = Offset(points.last().x + colorOffset * 100, points.last().y),
            )

            for (i in 1 until points.size) {
                val p1 = points[i - 1]
                val p2 = points[i]
                val progress = i.toFloat() / (points.size - 1)

                val strokeWidth = (10.dp.toPx() * (1 - progress)).coerceAtLeast(1f)
                val currentAlpha = alpha * (1 - progress)

                drawLine(
                    brush = brush,
                    start = p1,
                    end = p2,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Butt,
                    alpha = currentAlpha
                )
            }
        }
    }
}

@Composable
fun SlashGuide(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "slash-guide-transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "slash-guide-alpha"
    )

    Canvas(modifier = modifier) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
        drawLine(
            color = Color.Black.copy(alpha = alpha),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 5.dp.toPx(),
            pathEffect = pathEffect,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun BoosterPackAsset(
    rarity: Rarity,
    topPieceOffsetY: Float,
    packArt: Painter,
    modifier: Modifier = Modifier
) {
    val rarityBrush = getRarityBrush(rarity)
    val paint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            color = android.graphics.Color.WHITE
            typeface = Typeface.create(
                Typeface.DEFAULT,
                Typeface.BOLD
            )
            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }
    val shimmerAlpha by rememberInfiniteTransition(label = "sparkle-shimmer")
        .animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shimmerAlpha"
        )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val topPortionHeight = height * 0.15f

        // Draw the main body of the pack
        drawRect(
            brush = rarityBrush,
            topLeft = Offset(0f, topPortionHeight),
            size = Size(width, height - topPortionHeight)
        )

        // Add a sparkle effect for high-tier rarities
        if (rarity.isHighestTierCard()) {
            val sparkleCount = 10
            for (i in 0 until sparkleCount) {
                val x = Random.nextFloat() * width
                val y = topPortionHeight + (Random.nextFloat() * (height - topPortionHeight))
                val radius = Random.nextFloat() * 4.dp.toPx()
                drawCircle(
                    color = Color.Magenta.copy(alpha = shimmerAlpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }

        // Pack Art
        translate(top = topPortionHeight + 20.dp.toPx(), left = 20.dp.toPx()) {
            with(packArt) {
                draw(size = Size(160.dp.toPx(), 200.dp.toPx()))
            }
        }

        // Naming
        drawIntoCanvas {
            paint.textSize = 18.sp.toPx()
            it.nativeCanvas.drawText(
                rarity.key,
                center.x,
                height - 20.dp.toPx(),
                paint
            )
        }

        // Top part (The one that will tear)
        translate(top = topPieceOffsetY) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(width, topPortionHeight)
                lineTo(0f, topPortionHeight)
                close()
            }
            clipPath(path) {
                drawRect(brush = rarityBrush)
                drawRect(
                    color = Color(0xFFBDBDBD),
                    topLeft = Offset(0f, 0f),
                    size = Size(width, height * 0.05f)
                )
            }
        }
    }
}

@Composable
private fun getRarityBrush(rarity: Rarity): Brush {
    val colors = when {
        rarity.isHighestTierCard() ->
            listOf(Color(0xFFFDEB71), Color(0xFFF8D800)) // Gold
        rarity.isHighTierCard() ->
            listOf(Color(0xFFFF5959), Color(0xFFF83434)) // Red
        rarity.isRareCard() ->
            listOf(Color(0xFF42A5F5), Color(0xFF1976D2)) // Blue
        rarity.isUncommonCard() ->
            listOf(Color(0xFF66BB6A), Color(0xFF388E3C)) // Green
        else ->
            listOf(Color(0xFFBDBDBD), Color(0xFF757575)) // Grey
    }
    return Brush.verticalGradient(colors)
}