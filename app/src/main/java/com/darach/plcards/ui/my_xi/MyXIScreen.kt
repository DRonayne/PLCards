@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.darach.plcards.ui.my_xi

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.darach.plcards.R
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.ui.navigation.Screen
import androidx.compose.ui.graphics.Color as ComposeColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MyXIScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    viewModel: MyXIViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val bottomSheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            ModernTopBar(
                onShareClick = {
                    viewModel.shareFormation(context)
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(bottom = 100.dp), // Extra space for bottom navigation
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Segmented Formation Selector
            item {
                FormationSelector(
                    selectedFormation = uiState.formation,
                    onFormationSelected = { formation ->
                        viewModel.onEvent(MyXIEvent.ChangeFormation(formation))
                    },
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Football Pitch
            item {
                FootballPitch(
                    formation = uiState.formation,
                    playersInFormation = uiState.playersInFormation,
                    windowSizeClass = windowSizeClass,
                    onSlotClick = { position ->
                        viewModel.onEvent(MyXIEvent.SlotClicked(position))
                    },
                    onPlayerRemove = { player ->
                        viewModel.onEvent(MyXIEvent.RemovePlayerFromSlot(player))
                    },
                    onPlayerInfo = { player ->
                        navController.navigate(Screen.CardDetail.createRoute(player.id))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
            }

            // Available Players Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Players",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "${uiState.availablePlayers.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Available Players Grid
            if (uiState.availablePlayers.isEmpty()) {
                item {
                    EmptyAvailablePlayersCard()
                }
            } else {
                val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
                val columns = if (isTablet) 4 else 2
                val spacing = if (isTablet) 16.dp else 12.dp

                items(uiState.availablePlayers.chunked(columns)) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing / 2),
                        horizontalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        rowItems.forEach { player ->
                            Box(modifier = Modifier.weight(1f)) {
                                ModernCompactCard(
                                    cardModel = player,
                                    isTablet = isTablet,
                                    onClick = {
                                        navController.navigate(
                                            Screen.CardDetail.createRoute(
                                                player.id
                                            )
                                        )
                                    },
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedContentScope = animatedContentScope
                                )
                            }
                        }
                        // Fill remaining space if row is not complete
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Player Selection Bottom Sheet
        if (uiState.slotToFill != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(MyXIEvent.DismissPlayerSelection) },
                sheetState = bottomSheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                PlayerSelectionSheet(
                    availablePlayers = uiState.availablePlayers,
                    onPlayerSelected = { player ->
                        viewModel.onEvent(MyXIEvent.AssignPlayerToSlot(player))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun getRowStartIndex(formation: Formation, row: FormationRow): Int {
    return when (row) {
        FormationRow.GOALKEEPER -> 0
        FormationRow.DEFENDERS -> 1
        FormationRow.MIDFIELDERS -> 1 + formation.defenders
        FormationRow.FORWARDS -> 1 + formation.defenders + formation.midfielders
    }
}

// Segmented Button for Formation Selection
@Composable
fun FormationSelector(
    selectedFormation: Formation,
    onFormationSelected: (Formation) -> Unit,
    modifier: Modifier = Modifier
) {
    val formations = Formation.values()

    Column(modifier = modifier) {
        Text(
            text = "Formation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                formations.forEach { formation ->
                    val isSelected = formation == selectedFormation
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            ComposeColor.Transparent
                        },
                        animationSpec = tween(300),
                        label = "backgroundColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) {
                                onFormationSelected(formation)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formation.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}

// Football Pitch Component
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FootballPitch(
    formation: Formation,
    playersInFormation: Map<Int, CardModel>,
    windowSizeClass: WindowSizeClass,
    onSlotClick: (Int) -> Unit,
    onPlayerRemove: (CardModel) -> Unit,
    onPlayerInfo: (CardModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val isLandscape = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val useHorizontalLayout = isTablet || isLandscape

    Card(
        modifier = modifier.then(
            if (useHorizontalLayout) {
                Modifier.aspectRatio(16f / 10f)
            } else {
                Modifier.height(500.dp)
            }
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ComposeColor(0xFF2E7D32)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw pitch markings
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                if (useHorizontalLayout) {
                    drawHorizontalPitchMarkings()
                } else {
                    drawPitchMarkings()
                }
            }

            // Animated formation layout
            AnimatedContent(
                targetState = formation,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "formationChange"
            ) { currentFormation ->
                if (useHorizontalLayout) {
                    // Horizontal layout for tablets/landscape
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Goalkeeper (left)
                        AnimatedFormationColumn(
                            positions = 1,
                            playersInFormation = playersInFormation,
                            rowStartIndex = 0,
                            isTablet = isTablet,
                            onSlotClick = onSlotClick,
                            onPlayerRemove = onPlayerRemove,
                            onPlayerInfo = onPlayerInfo
                        )

                        // Defenders
                        AnimatedFormationColumn(
                            positions = currentFormation.defenders,
                            playersInFormation = playersInFormation,
                            rowStartIndex = getRowStartIndex(
                                currentFormation,
                                FormationRow.DEFENDERS
                            ),
                            isTablet = isTablet,
                            onSlotClick = onSlotClick,
                            onPlayerRemove = onPlayerRemove,
                            onPlayerInfo = onPlayerInfo
                        )

                        // Midfielders
                        AnimatedFormationColumn(
                            positions = currentFormation.midfielders,
                            playersInFormation = playersInFormation,
                            rowStartIndex = getRowStartIndex(
                                currentFormation,
                                FormationRow.MIDFIELDERS
                            ),
                            isTablet = isTablet,
                            onSlotClick = onSlotClick,
                            onPlayerRemove = onPlayerRemove,
                            onPlayerInfo = onPlayerInfo
                        )

                        // Forwards (right)
                        AnimatedFormationColumn(
                            positions = currentFormation.forwards,
                            playersInFormation = playersInFormation,
                            rowStartIndex = getRowStartIndex(
                                currentFormation,
                                FormationRow.FORWARDS
                            ),
                            isTablet = isTablet,
                            onSlotClick = onSlotClick,
                            onPlayerRemove = onPlayerRemove,
                            onPlayerInfo = onPlayerInfo
                        )
                    }
                } else {
                    // Original vertical layout for phones
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Forwards
                        AnimatedFormationRow(
                            positions = currentFormation.forwards,
                            playersInFormation = playersInFormation,
                            rowStartIndex = getRowStartIndex(
                                currentFormation,
                                FormationRow.FORWARDS
                            ),
                            onSlotClick = onSlotClick,
                            onPlayerRemove = onPlayerRemove,
                            onPlayerInfo = onPlayerInfo
                        )

                        // Midfielders
                        AnimatedFormationRow(
                            positions = currentFormation.midfielders,
                            playersInFormation = playersInFormation,
                            rowStartIndex = getRowStartIndex(
                                currentFormation,
                                FormationRow.MIDFIELDERS
                            ),
                            onSlotClick = onSlotClick,
                            onPlayerRemove = onPlayerRemove,
                            onPlayerInfo = onPlayerInfo
                        )

                        // Defenders
                        AnimatedFormationRow(
                            positions = currentFormation.defenders,
                            playersInFormation = playersInFormation,
                            rowStartIndex = getRowStartIndex(
                                currentFormation,
                                FormationRow.DEFENDERS
                            ),
                            onSlotClick = onSlotClick,
                            onPlayerRemove = onPlayerRemove,
                            onPlayerInfo = onPlayerInfo
                        )

                        // Goalkeeper
                        AnimatedFormationRow(
                            positions = 1,
                            playersInFormation = playersInFormation,
                            rowStartIndex = 0,
                            onSlotClick = onSlotClick,
                            onPlayerRemove = onPlayerRemove,
                            onPlayerInfo = onPlayerInfo
                        )
                    }
                }
            }
        }
    }
}

// Draw pitch markings
fun DrawScope.drawPitchMarkings() {
    val strokeWidth = 2.dp.toPx()
    val color = ComposeColor.White.copy(alpha = 0.3f)

    // Center circle
    drawCircle(
        color = color,
        radius = size.width * 0.15f,
        center = Offset(size.width / 2, size.height / 2),
        style = Stroke(width = strokeWidth)
    )

    // Center line
    drawLine(
        color = color,
        start = Offset(0f, size.height / 2),
        end = Offset(size.width, size.height / 2),
        strokeWidth = strokeWidth
    )

    // Goal areas
    val goalAreaWidth = size.width * 0.5f
    val goalAreaHeight = size.height * 0.15f

    // Top goal area
    drawLine(
        color = color,
        start = Offset((size.width - goalAreaWidth) / 2, 0f),
        end = Offset((size.width - goalAreaWidth) / 2, goalAreaHeight),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset((size.width + goalAreaWidth) / 2, 0f),
        end = Offset((size.width + goalAreaWidth) / 2, goalAreaHeight),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset((size.width - goalAreaWidth) / 2, goalAreaHeight),
        end = Offset((size.width + goalAreaWidth) / 2, goalAreaHeight),
        strokeWidth = strokeWidth
    )

    // Bottom goal area
    drawLine(
        color = color,
        start = Offset((size.width - goalAreaWidth) / 2, size.height),
        end = Offset((size.width - goalAreaWidth) / 2, size.height - goalAreaHeight),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset((size.width + goalAreaWidth) / 2, size.height),
        end = Offset((size.width + goalAreaWidth) / 2, size.height - goalAreaHeight),
        strokeWidth = strokeWidth
    )
    drawLine(
        color = color,
        start = Offset((size.width - goalAreaWidth) / 2, size.height - goalAreaHeight),
        end = Offset((size.width + goalAreaWidth) / 2, size.height - goalAreaHeight),
        strokeWidth = strokeWidth
    )
}

// Animated Formation Row
@Composable
fun AnimatedFormationRow(
    positions: Int,
    playersInFormation: Map<Int, CardModel>,
    rowStartIndex: Int,
    onSlotClick: (Int) -> Unit,
    onPlayerRemove: (CardModel) -> Unit,
    onPlayerInfo: (CardModel) -> Unit
) {
    // Calculate appropriate slot size based on number of positions
    val slotSize = when (positions) {
        1 -> 85.dp // Goalkeeper gets bigger slot
        2 -> 80.dp
        3 -> 75.dp
        4 -> 70.dp
        5 -> 65.dp // Smaller slots for 5 players to fit properly
        else -> 60.dp
    }

    val horizontalPadding = when (positions) {
        5 -> 8.dp // Less padding for 5 players
        4 -> 12.dp
        3 -> 20.dp
        2 -> 40.dp
        1 -> 80.dp
        else -> 8.dp
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        horizontalArrangement = if (positions == 1) Arrangement.Center else Arrangement.SpaceBetween
    ) {
        repeat(positions) { index ->
            val positionIndex = rowStartIndex + index
            val player = playersInFormation[positionIndex]

            AnimatedPlayerSlot(
                player = player,
                slotSize = slotSize,
                onSlotClick = { onSlotClick(positionIndex) },
                onPlayerRemove = { player?.let { onPlayerRemove(it) } },
                onPlayerInfo = { player?.let { onPlayerInfo(it) } }
            )
        }
    }
}

// Animated Player Slot
@Composable
fun AnimatedPlayerSlot(
    player: CardModel?,
    slotSize: Dp = 80.dp,
    onSlotClick: () -> Unit,
    onPlayerRemove: () -> Unit,
    onPlayerInfo: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (player != null) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(slotSize)
            .scale(scale)
    ) {
        AnimatedVisibility(
            visible = player != null,
            enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            player?.let {
                ModernPlayerCard(
                    cardModel = it,
                    onRemove = onPlayerRemove,
                    onInfo = onPlayerInfo
                )
            }
        }

        AnimatedVisibility(
            visible = player == null,
            enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        ) {
            EmptyPlayerSlot(
                onClick = onSlotClick
            )
        }
    }
}

// Player Card
@Composable
fun ModernPlayerCard(
    cardModel: CardModel,
    onRemove: () -> Unit,
    onInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showActions by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (showActions) 5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showActions = !showActions },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 4.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                // Player Image or Initials
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (cardModel.cardImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(cardModel.cardImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = cardModel.playerName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = cardModel.playerName.take(2).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = cardModel.playerName.split(" ").lastOrNull() ?: cardModel.playerName,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action buttons with animation
        AnimatedVisibility(
            visible = showActions,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        onInfo()
                        showActions = false
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Player info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        onRemove()
                        showActions = false
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove player",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Empty Player Slot
@Composable
fun EmptyPlayerSlot(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ComposeColor.White.copy(alpha = 0.1f),
        border = BorderStroke(
            width = 2.dp,
            color = ComposeColor.White.copy(alpha = 0.3f)
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add player",
                tint = ComposeColor.White.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Empty Available Players Card
@Composable
fun EmptyAvailablePlayersCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "No available players",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Add cards to favorites to build your XI!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Compact Card
@Composable
fun ModernCompactCard(
    cardModel: CardModel,
    isTablet: Boolean = false,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedVisibilityScope? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            ) { onClick() },
        shape = RoundedCornerShape(if (isTablet) 16.dp else 12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isTablet) 4.dp else 2.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                if (cardModel.cardImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(cardModel.cardImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = cardModel.playerName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (isTablet) 8.dp else 4.dp)
                            .then(
                                if (sharedTransitionScope != null && animatedContentScope != null) {
                                    with(sharedTransitionScope) {
                                        Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(
                                                key = "card-image-${cardModel.id}"
                                            ),
                                            animatedVisibilityScope = animatedContentScope
                                        )
                                    }
                                } else Modifier
                            )
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = cardModel.playerName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isTablet) 12.dp else 8.dp),
                verticalArrangement = Arrangement.spacedBy(if (isTablet) 4.dp else 2.dp)
            ) {
                Text(
                    text = cardModel.playerName.split(" ").lastOrNull() ?: cardModel.playerName,
                    style = if (isTablet) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cardModel.team,
                    style = if (isTablet) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Player Selection Sheet
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayerSelectionSheet(
    availablePlayers: List<CardModel>,
    onPlayerSelected: (CardModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(20.dp)
    ) {

        Text(
            text = "Select Player",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        AnimatedContent(
            targetState = availablePlayers.isEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with
                        fadeOut(animationSpec = tween(300))
            },
            label = "playerSelection"
        ) { isEmpty ->
            if (isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "No available players",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add more cards to favorites to expand your squad!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(availablePlayers) { player ->
                        SelectablePlayerCard(
                            cardModel = player,
                            onClick = { onPlayerSelected(player) }
                        )
                    }
                }
            }
        }
    }
}

// Selectable Player Card
@Composable
fun SelectablePlayerCard(
    cardModel: CardModel,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                if (cardModel.cardImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(cardModel.cardImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = cardModel.playerName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = cardModel.playerName.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = cardModel.playerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cardModel.team,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = cardModel.season,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

// Top Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(
    onShareClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My XI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Build your dream team",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                onClick = onShareClick,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = "Share formation",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// Horizontal formation column for tablets/landscape
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedFormationColumn(
    positions: Int,
    playersInFormation: Map<Int, CardModel>,
    rowStartIndex: Int,
    isTablet: Boolean,
    onSlotClick: (Int) -> Unit,
    onPlayerRemove: (CardModel) -> Unit,
    onPlayerInfo: (CardModel) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = if (positions > 1) Arrangement.SpaceEvenly else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(positions) { index ->
            val slotIndex = rowStartIndex + index
            val player = playersInFormation[slotIndex]
            val slotSize = when {
                slotIndex == 0 -> if (isTablet) 90.dp else 75.dp  // Goalkeeper
                positions >= 5 -> if (isTablet) 65.dp else 55.dp   // 5-player formations
                else -> if (isTablet) 75.dp else 65.dp             // Normal formations
            }

            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                if (player != null) {
                    ModernPlayerCard(
                        cardModel = player,
                        onRemove = { onPlayerRemove(player) },
                        onInfo = { onPlayerInfo(player) },
                        modifier = Modifier.size(slotSize)
                    )
                } else {
                    EmptyPlayerSlot(
                        onClick = { onSlotClick(slotIndex) },
                        modifier = Modifier.size(slotSize)
                    )
                }
            }
        }
    }
}

// Draw horizontal pitch markings for landscape/tablet view
fun DrawScope.drawHorizontalPitchMarkings() {
    val white = Color.White
    val strokeWidth = 4.dp.toPx()

    // Center line (vertical)
    drawLine(
        color = white,
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, size.height),
        strokeWidth = strokeWidth
    )

    // Center circle
    drawCircle(
        color = white,
        radius = 60.dp.toPx(),
        center = Offset(size.width / 2, size.height / 2),
        style = Stroke(width = strokeWidth)
    )

    // Left goal area
    val goalAreaHeight = 120.dp.toPx()
    val goalAreaWidth = 80.dp.toPx()
    drawRect(
        color = white,
        topLeft = Offset(0f, (size.height - goalAreaHeight) / 2),
        size = androidx.compose.ui.geometry.Size(goalAreaWidth, goalAreaHeight),
        style = Stroke(width = strokeWidth)
    )

    // Right goal area
    drawRect(
        color = white,
        topLeft = Offset(size.width - goalAreaWidth, (size.height - goalAreaHeight) / 2),
        size = androidx.compose.ui.geometry.Size(goalAreaWidth, goalAreaHeight),
        style = Stroke(width = strokeWidth)
    )

    // Outer boundary
    drawRect(
        color = white,
        topLeft = Offset(0f, 0f),
        size = androidx.compose.ui.geometry.Size(size.width, size.height),
        style = Stroke(width = strokeWidth)
    )
}
