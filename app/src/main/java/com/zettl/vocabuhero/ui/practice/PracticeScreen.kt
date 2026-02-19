package com.zettl.vocabuhero.ui.practice

import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.background
import com.zettl.vocabuhero.ui.theme.CardBackgrounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel,
    onSessionComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSession() }
    LaunchedEffect(uiState.sessionComplete, uiState.currentIndex, uiState.cards.size) {
        if (uiState.sessionComplete && uiState.cards.isNotEmpty()) onSessionComplete()
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
                Text(
                    "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    if (uiState.sessionComplete && uiState.cards.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "No cards to review today.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onSessionComplete,
                    modifier = Modifier.padding(top = 20.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Back")
                }
            }
        }
        return
    }

    // When past last card, return; LaunchedEffect(uiState.sessionComplete, ...) above will call onSessionComplete()
    val card = viewModel.currentCard()
    if (card == null) {
        return
    }

    // Flip only once when revealing (0 -> 180). Next card gets fresh state, no reverse flip.
    val rotationAnim = remember(uiState.currentIndex) { Animatable(0f) }
    LaunchedEffect(uiState.currentIndex, uiState.isRevealed) {
        if (uiState.isRevealed) {
            rotationAnim.animateTo(180f, animationSpec = tween(durationMillis = 500))
        }
    }
    val rotation = rotationAnim.value
    val density = LocalDensity.current.density

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            "${uiState.currentIndex + 1} / ${uiState.cards.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        val t = uiState.cardContrastLevel
        val bg = uiState.cardBackgroundPreset
        val scheme = MaterialTheme.colorScheme
        val preset = CardBackgrounds.preset(
            bg,
            scheme.surfaceVariant,
            scheme.primaryContainer,
            scheme.onSurface,
            scheme.onPrimaryContainer,
            scheme.primary,
            scheme.surface,
            scheme.onSurfaceVariant
        )
        val isThemeBased = bg == 0
        val cardContainerColor = when {
            isThemeBased -> lerp(scheme.surfaceVariant, scheme.primaryContainer, t)
            preset?.isGradient == true -> Color.Transparent
            else -> preset!!.containerColor!!
        }
        val cardContentColor = when {
            isThemeBased -> lerp(scheme.onSurface, scheme.onPrimaryContainer, t)
            else -> preset!!.contentColor
        }
        val cardSecondaryColor = when {
            isThemeBased -> lerp(scheme.primary, scheme.onPrimaryContainer.copy(alpha = 0.9f), t)
            else -> preset!!.secondaryColor
        }
        val cardTertiaryColor = when {
            isThemeBased -> lerp(scheme.onSurfaceVariant, scheme.onPrimaryContainer.copy(alpha = 0.8f), t)
            else -> preset!!.tertiaryColor
        }
        val gradientBrush = preset?.gradientBrush
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .clickable { if (!uiState.isRevealed) viewModel.reveal() },
            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large)
                    .then(
                        if (gradientBrush != null) Modifier.background(gradientBrush)
                        else Modifier
                    )
                    .graphicsLayer {
                        rotationY = rotation
                        transformOrigin = TransformOrigin.Center
                        cameraDistance = 12f * density
                        clip = true
                    }
            ) {
                // Compose only the visible side — front = prompt, back = answer (swap when reversed)
                val promptText = if (uiState.reversed) card.backText else card.frontText
                val answerText = if (uiState.reversed) card.frontText else card.backText
                if (rotation < 90f) {
                    // Front: only the prompt (word to learn), nothing else
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = promptText,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = cardContentColor,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Back: answer (and note), rotated so it appears right-side up when card is flipped
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = answerText,
                                style = MaterialTheme.typography.headlineMedium,
                                color = cardSecondaryColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            card.note?.takeIf { it.isNotBlank() }?.let { note ->
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 16.dp),
                                    color = cardTertiaryColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        if (uiState.isRevealed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Again: error/destructive (red) — "wrong"
                Button(
                    onClick = { viewModel.rate(0) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Again")
                }
                // Hard: tertiary (orange-ish) — "difficult"
                Button(
                    onClick = { viewModel.rate(1) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Hard")
                }
                // Good: primary (teal) — "correct, normal"
                Button(
                    onClick = { viewModel.rate(2) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Good")
                }
                // Easy: secondary — "easy win"
                Button(
                    onClick = { viewModel.rate(3) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Easy")
                }
            }
        }
    }
}
