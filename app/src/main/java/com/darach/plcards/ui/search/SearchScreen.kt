@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalHazeMaterialsApi::class, ExperimentalSharedTransitionApi::class
)

package com.darach.plcards.ui.search

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.darach.plcards.R
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.domain.model.SortOrder
import com.darach.plcards.ui.common.CardItem
import com.darach.plcards.ui.common.LoadingSkeletonGrid
import com.darach.plcards.ui.navigation.Screen
import com.darach.plcards.ui.theme.PLCardsTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.flow.flowOf

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    val backStackEntry = navController.currentBackStackEntry
    LaunchedEffect(backStackEntry) {
        val teamFilter = backStackEntry?.arguments?.getString("teamFilter")
        teamFilter?.let { encodedTeam ->
            val decodedTeam = try {
                java.net.URLDecoder.decode(encodedTeam, "UTF-8")
            } catch (e: Exception) {
                encodedTeam // fallback to original if decoding fails
            }
            viewModel.onEvent(SearchEvent.OnTeamSelected(decodedTeam, true))
            backStackEntry.arguments?.remove("teamFilter")
        }
    }

    SearchScreenContent(
        uiState = uiState,
        searchResults = searchResults,
        onEvent = viewModel::onEvent,
        onCardClick = { cardId ->
            navController.navigate(Screen.CardDetail.createRoute(cardId))
        },
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope
    )
}

@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    searchResults: LazyPagingItems<CardModel>,
    onEvent: (SearchEvent) -> Unit,
    onCardClick: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hazeState = remember { HazeState() }
    var isTextFieldFocused by remember { mutableStateOf(false) }


    val baseTopPadding = 140.dp
    val additionalPadding by animateDpAsState(
        targetValue = if (uiState.appliedTeams.isNotEmpty() || uiState.appliedSeasons.isNotEmpty()) 50.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "additionalPadding"
    )
    val topPadding = baseTopPadding + additionalPadding

    if (uiState.error != null) {
        SearchErrorContent(
            error = uiState.error,
            onRetry = { }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = searchResults.loadState.refresh,
            animationSpec = tween(durationMillis = 200),
            label = "searchResultsTransition"
        ) { loadState ->
            when (loadState) {
                is LoadState.Loading -> {
                    LoadingSkeletonGrid(topPadding = topPadding)
                }

                is LoadState.Error -> {
                    PaginationErrorContent(
                        onRetry = { searchResults.retry() }
                    )
                }

                else -> {
                    if (searchResults.itemCount == 0 && uiState.searchQuery.isNotBlank()) {
                        // Empty state for no results
                        EmptySearchResults()
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            contentPadding = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding() + 88.dp, // Extra space for nav bar
                                top = topPadding
                            ),
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.hazeSource(hazeState)
                        ) {
                            items(searchResults.itemCount) { index ->
                                searchResults[index]?.let { card ->
                                    CardItem(
                                        cardModel = card,
                                        onClick = { onCardClick(card.id) },
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedContentScope = animatedContentScope,
                                        sharedElementKey = "card-image-${card.id}"
                                    )
                                }
                            }
                            if (searchResults.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .hazeEffect(state = hazeState, style = HazeMaterials.thin())
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onEvent(SearchEvent.OnQueryChanged(it)) },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isTextFieldFocused = focusState.isFocused
                        },
                    placeholder = { Text("Search player, team...") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onEvent(SearchEvent.OnQueryChanged(""))
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                isTextFieldFocused = false
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (uiState.searchQuery.isNotBlank()) {
                                onEvent(SearchEvent.OnSearchSubmitted(uiState.searchQuery))
                            }
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            isTextFieldFocused = false
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                IconButton(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(48.dp),
                    onClick = { onEvent(SearchEvent.ToggleFilterSheet) }
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (uiState.appliedSeasons.isNotEmpty() || uiState.appliedTeams.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Suggestions dropdown
            val filteredSuggestions = uiState.suggestions.filter { suggestion ->
                !suggestion.text.equals(uiState.searchQuery, ignoreCase = true)
            }
            if (isTextFieldFocused && filteredSuggestions.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 180.dp)
                    ) {
                        items(filteredSuggestions, key = { it.text + it.type }) { suggestion ->
                            ListItem(
                                headlineContent = { Text(suggestion.text) },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (suggestion.type == SuggestionType.HISTORY) {
                                            Icons.Default.History
                                        } else {
                                            Icons.Default.Search
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onEvent(SearchEvent.OnSearchSubmitted(suggestion.text))
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    isTextFieldFocused = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.appliedTeams.isNotEmpty() || uiState.appliedSeasons.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.appliedTeams.forEach { team ->
                        InputChip(
                            onClick = {
                                onEvent(SearchEvent.RemoveAppliedFilter(FilterType.TEAM, team))
                            },
                            label = { Text(team) },
                            selected = true,
                            trailingIcon = { Icon(Icons.Default.Close, "Remove filter") }
                        )
                    }
                    uiState.appliedSeasons.forEach { season ->
                        InputChip(
                            onClick = {
                                onEvent(SearchEvent.RemoveAppliedFilter(FilterType.SEASON, season))
                            },
                            label = { Text(season) },
                            selected = true,
                            trailingIcon = { Icon(Icons.Default.Close, "Remove filter") }
                        )
                    }
                }
            }
        }

    }

    if (uiState.isFilterSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(SearchEvent.ToggleFilterSheet) },
            sheetState = bottomSheetState,
            modifier = Modifier.padding(top = 64.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                FilterSheetContent(uiState = uiState, onEvent = onEvent)

                // Floating Apply Button inside the sheet
                if (uiState.hasFilterChanges) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .padding(
                                bottom = WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            )
                    ) {
                        Button(
                            onClick = { onEvent(SearchEvent.ApplyFilters) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                        ) {
                            Text("Apply Filters")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSheetContent(uiState: SearchUiState, onEvent: (SearchEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (uiState.hasFilterChanges) 80.dp else 0.dp)
    ) {
        // Header Section
        FilterSheetHeader(
            onResetClick = { onEvent(SearchEvent.ResetFilters) },
            onCloseClick = { onEvent(SearchEvent.ToggleFilterSheet) }
        )

        // Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = if (uiState.hasFilterChanges) {
                    100.dp + WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                } else {
                    16.dp + WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                }
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sort Options Section
            item {
                FilterSection(title = "Sort By") {
                    SortOptionsGrid(
                        currentSortOrder = uiState.sortOrder,
                        onSortOrderChanged = { sortOrder ->
                            onEvent(SearchEvent.OnSortOrderChanged(sortOrder))
                        }
                    )
                }
            }

            // Seasons Section
            item {
                FilterSection(title = "Seasons") {
                    ChipGrid(
                        items = uiState.allSeasons,
                        selectedItems = uiState.selectedSeasons,
                        onItemToggled = { season, isSelected ->
                            onEvent(SearchEvent.OnSeasonSelected(season, isSelected))
                        },
                        chipMinWidth = 100.dp
                    )
                }
            }

            // Teams Section
            item {
                FilterSection(title = "Teams") {
                    TeamChipGrid(
                        teams = uiState.allTeams,
                        selectedTeams = uiState.selectedTeams,
                        onTeamToggled = { team, isSelected ->
                            onEvent(SearchEvent.OnTeamSelected(team, isSelected))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSheetHeader(
    onResetClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close filters",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Filters & Sort",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(
                onClick = onResetClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Reset")
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
    }
}

@Composable
private fun SortOptionsGrid(
    currentSortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortOrder.entries.forEach { sortOrder ->
            FilterChip(
                onClick = { onSortOrderChanged(sortOrder) },
                label = {
                    Text(
                        text = sortOrder.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                selected = sortOrder == currentSortOrder,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun ChipGrid(
    items: List<String>,
    selectedItems: List<String>,
    onItemToggled: (String, Boolean) -> Unit,
    chipMinWidth: Dp = 100.dp,
    useAutoSizeText: Boolean = false
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = item in selectedItems
            FilterChip(
                onClick = { onItemToggled(item, !isSelected) },
                label = {
                    if (useAutoSizeText) {
                        AutoSizeText(
                            text = item,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                },
                selected = isSelected,
                modifier = Modifier.widthIn(min = chipMinWidth),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun TeamChipGrid(
    teams: List<String>,
    selectedTeams: List<String>,
    onTeamToggled: (String, Boolean) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(teams.size) { index ->
            val team = teams[index]
            val isSelected = team in selectedTeams
            FilterChip(
                onClick = { onTeamToggled(team, !isSelected) },
                label = {
                    AutoSizeText(
                        text = team,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = isSelected,
                modifier = Modifier.fillMaxWidth(),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun SearchErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun PaginationErrorContent(
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Results Found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try adjusting your search or filters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Try Again")
        }
    }
}

@Composable
private fun EmptySearchResults() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Results Found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try different keywords or adjust your filters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = Int.MAX_VALUE,
    minFontSize: Dp = 8.dp,
    maxFontSize: Dp = 14.dp
) {
    var fontSizeValue by remember(text) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    BasicText(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = style.copy(
            fontSize = fontSizeValue.value.sp,
            color = LocalContentColor.current
        ),
        maxLines = maxLines,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth || textLayoutResult.didOverflowHeight) {
                val nextFontSizeValue = fontSizeValue.value - 0.5f
                if (nextFontSizeValue >= minFontSize.value) {
                    fontSizeValue = nextFontSizeValue.dp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

private val mockCards = listOf(
    CardModel("1", "2023-24", "145", "Bukayo Saka", "Arsenal", "", true, 1L),
    CardModel("2", "2023-24", "10", "Martin Ødegaard", "Arsenal", "", true, 2L),
    CardModel("3", "2022-23", "11", "Mohamed Salah", "Liverpool", "", false, null),
    CardModel("4", "2023-24", "9", "Erling Haaland", "Manchester City", "", false, null),
    CardModel("5", "2021-22", "7", "Son Heung-min", "Tottenham Hotspur", "", false, null)
)

@Composable
private fun rememberMockPagingItems(
    loadState: LoadState,
    items: List<CardModel> = emptyList()
): LazyPagingItems<CardModel> {
    val flow = when (loadState) {
        is LoadState.Loading -> flowOf(PagingData.empty())
        is LoadState.Error -> flowOf(PagingData.empty())
        else -> flowOf(PagingData.from(items))
    }
    return flow.collectAsLazyPagingItems()
}

@Preview(name = "Initial Loading State", showBackground = true)
@Composable
private fun SearchScreenPreview_Loading() {
    val mockPagingItems = rememberMockPagingItems(LoadState.Loading)
    val uiState = SearchUiState()

    PLCardsTheme(isWc2002Mode = true) {
        SearchScreenContent(
            uiState = uiState,
            searchResults = mockPagingItems,
            onEvent = {},
            onCardClick = {}
        )
    }
}

@Preview(name = "With Search Results", showBackground = true)
@Composable
private fun SearchScreenPreview_WithResults() {
    val mockPagingItems = rememberMockPagingItems(LoadState.NotLoading(false), mockCards)
    val uiState = SearchUiState(searchQuery = "Saka")

    PLCardsTheme(isWc2002Mode = true) {
        SearchScreenContent(
            uiState = uiState,
            searchResults = mockPagingItems,
            onEvent = {},
            onCardClick = {}
        )
    }
}

@Preview(name = "With Applied Filters", showBackground = true)
@Composable
private fun SearchScreenPreview_WithAppliedFilters() {
    val mockPagingItems = rememberMockPagingItems(
        LoadState.NotLoading(false),
        mockCards.filter { it.team == "Arsenal" })
    val uiState = SearchUiState(
        appliedTeams = listOf("Arsenal"),
        appliedSeasons = listOf("2023-24")
    )

    PLCardsTheme(isWc2002Mode = true) {
        SearchScreenContent(
            uiState = uiState,
            searchResults = mockPagingItems,
            onEvent = {},
            onCardClick = {}
        )
    }
}

@Preview(name = "Filter Sheet Open", showBackground = true)
@Composable
private fun SearchScreenPreview_FilterSheetOpen() {
    val mockPagingItems = rememberMockPagingItems(LoadState.NotLoading(false), mockCards)
    val uiState = SearchUiState(
        isFilterSheetVisible = true,
        allTeams = listOf("Arsenal", "Liverpool", "Manchester City", "Tottenham Hotspur"),
        allSeasons = listOf("2023-24", "2022-23", "2021-22"),
        selectedTeams = listOf("Arsenal")
    )

    PLCardsTheme(isWc2002Mode = true) {
        SearchScreenContent(
            uiState = uiState,
            searchResults = mockPagingItems,
            onEvent = {},
            onCardClick = {}
        )
    }
}

@Preview(name = "Full Page Error", showBackground = true)
@Composable
private fun SearchScreenPreview_ErrorState() {
    val mockPagingItems = rememberMockPagingItems(LoadState.Error(Exception()))
    val uiState = SearchUiState(
        error = "Could not connect to the server. Please check your internet connection."
    )

    PLCardsTheme(isWc2002Mode = true) {
        SearchScreenContent(
            uiState = uiState,
            searchResults = mockPagingItems,
            onEvent = {},
            onCardClick = {}
        )
    }
}