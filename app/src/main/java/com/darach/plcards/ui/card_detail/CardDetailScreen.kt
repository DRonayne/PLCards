@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.darach.plcards.ui.card_detail

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.darach.plcards.R
import com.darach.plcards.domain.model.CardModel

@Composable
fun CardDetailScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: CardDetailViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val card = uiState.cardModel

    if (card == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        CardDetailContent(
            card = card,
            windowSizeClass = windowSizeClass,
            onNavigateBack = { navController.navigateUp() },
            onToggleFavorite = { viewModel.onEvent(CardDetailEvent.ToggleFavorite(card)) },
            onShare = {
                viewModel.onEvent(CardDetailEvent.ShareCard(card))
            },
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
        )
    }
}

@Composable
private fun CardDetailContent(
    card: CardModel,
    windowSizeClass: WindowSizeClass,
    onNavigateBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val scrollState = rememberScrollState()
    LocalDensity.current

    // Calculate parallax offset
    val parallaxOffset = scrollState.value * 0.5f

    // Determine layout based on window size
    val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val isLandscape = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val useHorizontalLayout = isTablet || isLandscape

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (useHorizontalLayout) {
            // Horizontal layout for tablets and landscape
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Image section (left side)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(if (isTablet) 0.65f else 0.5f)
                ) {
                    CardImageSection(
                        card = card,
                        parallaxOffset = parallaxOffset,
                        useHorizontalLayout = true,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope
                    )
                }

                // Content section (right side)
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(if (isTablet) 0.35f else 0.5f),
                    shape = RoundedCornerShape(
                        topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                    ),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    CardContentSection(
                        card = card,
                        isTablet = isTablet,
                        onToggleFavorite = onToggleFavorite,
                        onShare = onShare
                    )
                }
            }
        } else {
            // Original vertical layout for phones
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                CardImageSection(
                    card = card,
                    parallaxOffset = parallaxOffset,
                    useHorizontalLayout = false,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )

                // Content section
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    CardContentSection(
                        card = card,
                        isTablet = false,
                        onToggleFavorite = onToggleFavorite,
                        onShare = onShare
                    )
                }
            }
        }

        // Floating back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            IconButton(
                onClick = onNavigateBack, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.3f), contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back"
                )
            }
        }
    }
}

@Composable
private fun CardImageSection(
    card: CardModel,
    parallaxOffset: Float,
    useHorizontalLayout: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // Background blur effect
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(card.cardImageUrl)
            .crossfade(true).allowHardware(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = if (useHorizontalLayout) 0f else -parallaxOffset
                }
                .blur(25.dp)
                .scale(1.1f))

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (useHorizontalLayout) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    }
                )
        )

        // Main card image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(card.cardImageUrl)
            .crossfade(true).allowHardware(false).build(),
            contentDescription = card.playerName,
            contentScale = if (useHorizontalLayout) ContentScale.Fit else ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (useHorizontalLayout) 16.dp else 64.dp)
                .graphicsLayer {
                    translationY = if (useHorizontalLayout) 0f else -parallaxOffset
                }
                .then(
                    if (sharedTransitionScope != null && animatedContentScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = "card-image-${card.id}"),
                            animatedVisibilityScope = animatedContentScope
                        )
                    }
                } else Modifier))
    }
}

@Composable
private fun CardContentSection(
    card: CardModel, isTablet: Boolean, onToggleFavorite: () -> Unit, onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isTablet) 32.dp else 24.dp,
                vertical = if (isTablet) 40.dp else 32.dp
            ), verticalArrangement = Arrangement.spacedBy(if (isTablet) 24.dp else 20.dp)
    ) {
        // Player name and number
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.playerName, style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = if (isTablet) 36.sp else 32.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = if (isTablet) 40.sp else 36.sp
                    ), color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.team,
                    style = if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Card number badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = "#${card.cardNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(
                        horizontal = if (isTablet) 20.dp else 16.dp,
                        vertical = if (isTablet) 12.dp else 8.dp
                    )
                )
            }
        }

        // Season info
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isTablet) 24.dp else 20.dp)
            ) {
                Text(
                    text = "Season",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = card.season,
                    style = (if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge).copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isTablet) 16.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 12.dp)
        ) {
            // Add to XI button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggleFavorite() }
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                color = if (card.isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isTablet) 24.dp else 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (card.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (card.isFavorite) "Remove from XI" else "Add to XI",
                        tint = if (card.isFavorite) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (card.isFavorite) "In My XI" else "Add to XI",
                        style = (if (isTablet) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium).copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (card.isFavorite) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Share button
            Surface(
                modifier = Modifier
                    .size(if (isTablet) 72.dp else 60.dp)
                    .clip(CircleShape)
                    .clickable { onShare() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(if (isTablet) 28.dp else 24.dp)
                    )
                }
            }
        }
    }
}

private fun Modifier.offset(y: Dp) = this.then(
    Modifier.graphicsLayer { translationY = y.toPx() })