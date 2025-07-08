package com.darach.plcards.ui.home

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.darach.plcards.R
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.ui.common.CardItem
import com.darach.plcards.ui.common.SkeletonLoader
import com.darach.plcards.ui.navigation.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: HomeScreenViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    if (isTablet) {
        // Centered layout for tablets
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(
                        max = when (windowSizeClass.widthSizeClass) {
                            WindowWidthSizeClass.Medium -> 800.dp
                            WindowWidthSizeClass.Expanded -> 1200.dp
                            else -> Int.MAX_VALUE.dp
                        }
                    )
                    .fillMaxHeight()
            ) {
                TabletHomeContent(
                    uiState = uiState,
                    windowSizeClass = windowSizeClass,
                    navController = navController,
                    viewModel = viewModel,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }
        }
    } else {
        // Original phone layout
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                uiState.error != null -> {
                    ErrorContent(
                        modifier = Modifier.fillMaxSize(),
                        error = uiState.error!!,
                        onRetry = viewModel::retryLoadData
                    )
                }

                uiState.isLoading && uiState.shelves.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                !uiState.isLoading && uiState.shelves.isEmpty() -> {
                    EmptyStateContent(
                        modifier = Modifier.fillMaxSize(),
                        onRetry = viewModel::retryLoadData
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                            ) {
                                // Logo at the top
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = if (uiState.isWc2002Mode) R.drawable.wc2002_logo else R.drawable.retro_pl
                                        ),
                                        contentDescription = if (uiState.isWc2002Mode) "WC2002 Logo" else "Premier League Logo",
                                        modifier = Modifier
                                            .size(60.dp),
                                        contentScale = ContentScale.Fit,
                                        colorFilter = if (!uiState.isWc2002Mode) {
                                            ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                        } else null
                                    )
                                }

                                FeaturedCarousel(
                                    cardModels = uiState.featuredCarouselCardModels,
                                    isLoading = uiState.isLoading,
                                    isWc2002Mode = uiState.isWc2002Mode,
                                    windowSizeClass = windowSizeClass,
                                    onCardClick = { cardId ->
                                        navController.navigate(Screen.CardDetail.createRoute(cardId))
                                    },
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedContentScope = animatedContentScope
                                )
                            }
                        }

                        item {
                            QuickFilters(
                                isWc2002Mode = uiState.isWc2002Mode,
                                onTeamClick = { teamName ->
                                    navController.navigate(Screen.Search.createRoute(teamName))
                                }
                            )
                        }

                        items(uiState.shelves, key = { it.type }) { shelf ->
                            CardShelf(
                                title = shelf.title,
                                cardModels = shelf.cardModels,
                                windowSizeClass = windowSizeClass,
                                onCardClick = { cardId ->
                                    navController.navigate(Screen.CardDetail.createRoute(cardId))
                                },
                                onSeeAllClick = {
                                    navController.navigate(Screen.GenericGrid.createRoute(shelf.type))
                                },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedContentScope = animatedContentScope
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TabletHomeContent(
    uiState: HomeUiState,
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: HomeScreenViewModel,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    when {
        uiState.error != null -> {
            ErrorContent(
                modifier = Modifier.fillMaxSize(),
                error = uiState.error,
                onRetry = viewModel::retryLoadData
            )
        }

        uiState.isLoading && uiState.shelves.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        !uiState.isLoading && uiState.shelves.isEmpty() -> {
            EmptyStateContent(
                modifier = Modifier.fillMaxSize(),
                onRetry = viewModel::retryLoadData
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo at the top
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (uiState.isWc2002Mode) R.drawable.wc2002_logo else R.drawable.retro_pl
                                ),
                                contentDescription = if (uiState.isWc2002Mode) "WC2002 Logo" else "Premier League Logo",
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Fit,
                                colorFilter = if (!uiState.isWc2002Mode) {
                                    ColorFilter.tint(MaterialTheme.colorScheme.primary)
                                } else null
                            )
                        }

                        FeaturedCarousel(
                            cardModels = uiState.featuredCarouselCardModels,
                            isLoading = uiState.isLoading,
                            isWc2002Mode = uiState.isWc2002Mode,
                            windowSizeClass = windowSizeClass,
                            onCardClick = { cardId ->
                                navController.navigate(Screen.CardDetail.createRoute(cardId))
                            },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope
                        )
                    }
                }

                item {
                    QuickFilters(
                        isWc2002Mode = uiState.isWc2002Mode,
                        onTeamClick = { teamName ->
                            navController.navigate(Screen.Search.createRoute(teamName))
                        }
                    )
                }

                items(uiState.shelves, key = { it.type }) { shelf ->
                    CardShelf(
                        title = shelf.title,
                        cardModels = shelf.cardModels,
                        windowSizeClass = windowSizeClass,
                        onCardClick = { cardId ->
                            navController.navigate(Screen.CardDetail.createRoute(cardId))
                        },
                        onSeeAllClick = {
                            navController.navigate(Screen.GenericGrid.createRoute(shelf.type))
                        },
                        sharedTransitionScope = if (shelf.type != ShelfType.RECENTLY_VIEWED) sharedTransitionScope else null,
                        animatedContentScope = if (shelf.type != ShelfType.RECENTLY_VIEWED) animatedContentScope else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CardShelf(
    title: String,
    cardModels: List<CardModel>,
    windowSizeClass: WindowSizeClass,
    onCardClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onSeeAllClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "See all $title",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
        val horizontalPadding = if (isTablet)
            when (windowSizeClass.widthSizeClass) {
                WindowWidthSizeClass.Medium -> 24.dp
                WindowWidthSizeClass.Expanded -> 32.dp
                else -> 16.dp
            } else 16.dp
        val cardSpacing = if (isTablet) 16.dp else 12.dp

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing)
        ) {
            items(cardModels, key = { it.id }) { card ->
                CardItem(
                    cardModel = card,
                    onClick = { onCardClick(card.id) },
                    windowSizeClass = windowSizeClass,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    sharedElementKey = "shelf-${title.replace(" ", "")}-card-${card.id}"
                )
            }
        }
    }
}


@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SignalCellularConnectedNoInternet4Bar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FeaturedCarousel(
    cardModels: List<CardModel>,
    isLoading: Boolean,
    isWc2002Mode: Boolean,
    windowSizeClass: WindowSizeClass,
    onCardClick: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isWc2002Mode) "WC2002 Featured Stars" else "Featured",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (isLoading && cardModels.isEmpty()) {
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        } else if (cardModels.isNotEmpty()) {
            val actualPageCount = cardModels.size
            val pagerState = rememberPagerState(
                initialPage = Int.MAX_VALUE / 2,
                pageCount = { Int.MAX_VALUE }
            )

            LaunchedEffect(pagerState, actualPageCount) {
                if (actualPageCount > 1) {
                    while (true) {
                        delay(5000)
                        pagerState.animateScrollToPage(
                            page = pagerState.currentPage + 1,
                            animationSpec = tween(durationMillis = 1500) // Slower transition animation
                        )
                    }
                }
            }

            val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
            val carouselHeight = if (isTablet) 320.dp else 240.dp
            val horizontalPadding = if (isTablet)
                maxOf(32.dp, (windowSizeClass.widthSizeClass.let {
                    when (it) {
                        WindowWidthSizeClass.Medium -> 80.dp
                        WindowWidthSizeClass.Expanded -> 120.dp
                        else -> 32.dp
                    }
                })) else 16.dp

            if (isTablet && actualPageCount >= 2) {
                // Show 2 cards side by side on tablets
                val pairedPagerState = rememberPagerState(
                    initialPage = Int.MAX_VALUE / 4,
                    pageCount = { Int.MAX_VALUE }
                )

                LaunchedEffect(pairedPagerState, actualPageCount) {
                    while (true) {
                        delay(5000)
                        pairedPagerState.animateScrollToPage(
                            page = pairedPagerState.currentPage + 1,
                            animationSpec = tween(durationMillis = 1500)
                        )
                    }
                }

                HorizontalPager(
                    state = pairedPagerState,
                    modifier = Modifier.height(carouselHeight),
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = 16.dp
                ) { page ->
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(2) { index ->
                            val cardIndex = (page * 2 + index) % actualPageCount
                            val card = cardModels[cardIndex]
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = { onCardClick(card.id) },
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(card.cardImageUrl)
                                            .crossfade(true)
                                            .allowHardware(true)
                                            .build(),
                                        contentDescription = card.playerName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .then(
                                                if (sharedTransitionScope != null && animatedContentScope != null) {
                                                    with(sharedTransitionScope) {
                                                        Modifier.sharedElement(
                                                            sharedContentState = rememberSharedContentState(
                                                                key = "card-image-${card.id}"
                                                            ),
                                                            animatedVisibilityScope = animatedContentScope
                                                        )
                                                    }
                                                } else Modifier
                                            ),
                                        alignment = Alignment.TopCenter
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.8f)
                                                    ),
                                                    startY = 150f
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = card.playerName,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${card.team} • ${card.season}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Original single card layout for phones or when less than 2 cards
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.height(carouselHeight),
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = if (isTablet) 16.dp else 12.dp
                ) { page ->
                    val card = cardModels[page % actualPageCount]
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        onClick = { onCardClick(card.id) },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(card.cardImageUrl)
                                    .crossfade(true)
                                    .allowHardware(true)
                                    .build(),
                                contentDescription = card.playerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize(),
                                alignment = Alignment.TopCenter
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.8f)
                                            ),
                                            startY = 300f
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = card.playerName,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = "${card.team} • ${card.season}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickFilters(isWc2002Mode: Boolean, onTeamClick: (String) -> Unit) {
    val teams = if (isWc2002Mode) {
        listOf(
            "Brazil",
            "Germany",
            "Argentina",
            "England",
            "France",
            "Spain",
            "Portugal",
            "Ireland"
        )
    } else {
        listOf(
            "Manchester United",
            "Arsenal FC",
            "Liverpool",
            "Manchester City",
            "Chelsea FC",
            "Newcastle United"
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isWc2002Mode) "WC2002 Nations" else "Popular Teams",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(teams) { team ->
                FilterChip(
                    onClick = { onTeamClick(team) },
                    label = {
                        Text(
                            text = team,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    selected = false,
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
private fun EmptyStateContent(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SignalCellularConnectedNoInternet4Bar,
                contentDescription = "No content",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "No cards available",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Please check your connection and try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Retry")
            }
        }
    }
}