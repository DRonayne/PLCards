@file:OptIn(ExperimentalCoroutinesApi::class)

package com.darach.plcards.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.darach.plcards.data.repository.SettingsRepository
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.domain.model.SortOrder
import com.darach.plcards.domain.repository.CardRepository
import com.darach.plcards.util.AnalyticsHelper
import com.darach.plcards.util.CrashlyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: CardRepository,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val crashlyticsHelper: CrashlyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<CardModel>> = combine(
        uiState,
        settingsRepository.settings
    ) { uiState, settings ->
        val searchParameters = SearchParameters(
            query = uiState.searchQuery.trim(),
            sortOrder = uiState.appliedSortOrder,
            teams = uiState.appliedTeams,
            seasons = uiState.appliedSeasons,
            isWc2002Mode = settings.isWc2002Mode ?: false
        )
        repository.getCards(
            config = PagingConfig(pageSize = 50, enablePlaceholders = false),
            query = searchParameters.query,
            sortOrder = searchParameters.sortOrder,
            teams = searchParameters.teams.ifEmpty { null },
            seasons = searchParameters.seasons.ifEmpty { null },
            isWc2002Mode = searchParameters.isWc2002Mode
        )
    }
        .flatMapLatest { it }
        .cachedIn(viewModelScope)

    init {
        analyticsHelper.trackScreenView("Search")
        loadFilterData()
        loadDefaultSortOrder()
        updateSuggestionsAndHistory("")
    }

    private fun loadDefaultSortOrder() {
        viewModelScope.launch {
            settingsRepository.settings.firstOrNull()?.let { settings ->
                val defaultSortOrder = settings.defaultSortOrder
                _uiState.update {
                    it.copy(
                        sortOrder = defaultSortOrder,
                        appliedSortOrder = defaultSortOrder
                    )
                }
            }
        }
    }

    private fun loadFilterData() {
        viewModelScope.launch {
            settingsRepository.settings.flatMapLatest { settings ->
                repository.getAllTeams(settings.isWc2002Mode)
            }.catch { e ->
                crashlyticsHelper.recordException(e)
                _uiState.update { it.copy(error = "Failed to load teams filter") }
            }.collect { teams ->
                // Case-insensitive deduplication and sorting
                val distinctTeams = teams
                    .distinctBy { it.lowercase() }
                    .sorted()
                _uiState.update { it.copy(allTeams = distinctTeams, error = null) }
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.flatMapLatest { settings ->
                repository.getAllSeasons(settings.isWc2002Mode)
            }.catch { e ->
                crashlyticsHelper.recordException(e)
                _uiState.update { it.copy(error = "Failed to load seasons filter") }
            }.collect { seasons ->
                _uiState.update { it.copy(allSeasons = seasons.sortedDescending(), error = null) }
            }
        }
    }

    private fun updateSuggestionsAndHistory(query: String) {
        viewModelScope.launch {
            val historyFlow = repository.getRecentSearches().map { recent ->
                val filtered = if (query.isBlank()) {
                    recent
                } else {
                    recent.filter { it.query.contains(query, ignoreCase = true) }
                }
                filtered.take(2).map { Suggestion(it.query, SuggestionType.HISTORY) }
            }

            val suggestionFlow = if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.getSearchSuggestions(query, 4)
                    .map { suggestions ->
                        suggestions.map { Suggestion(it, SuggestionType.SUGGESTION) }
                    }
            }

            combine(historyFlow, suggestionFlow) { history, suggestions ->
                val finalSuggestions = (history + suggestions.filter { s ->
                    history.none { h ->
                        h.text.equals(s.text, ignoreCase = true)
                    }
                }).take(3) // Limit to 3 suggestions max
                finalSuggestions
            }.catch { e ->
                crashlyticsHelper.recordException(e)
                emptyList<Suggestion>()
            }.collect { finalSuggestions ->
                _uiState.update { it.copy(suggestions = finalSuggestions) }
            }
        }
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                updateSuggestionsAndHistory(event.query)
            }

            is SearchEvent.OnSearchSubmitted -> {
                analyticsHelper.trackSearchPerformed(event.query)
                viewModelScope.launch {
                    if (event.query.isNotBlank()) {
                        repository.addRecentSearch(event.query)
                    }
                }
                // Update query to ensure UI consistency and trigger search
                _uiState.update { it.copy(searchQuery = event.query) }
            }

            is SearchEvent.OnSortOrderChanged -> _uiState.update { it.copy(sortOrder = event.sortOrder) }
            is SearchEvent.OnTeamSelected -> {
                _uiState.update { state ->
                    val newSelection = state.selectedTeams.toMutableSet()
                    if (event.isSelected) {
                        newSelection.add(event.team)
                    } else {
                        newSelection.remove(event.team)
                    }

                    if (!state.isFilterSheetVisible && event.isSelected) {
                        state.copy(
                            selectedTeams = newSelection.toList(),
                            appliedTeams = newSelection.toList()
                        )
                    } else {
                        state.copy(selectedTeams = newSelection.toList())
                    }
                }
            }

            is SearchEvent.OnSeasonSelected -> {
                _uiState.update { state ->
                    val newSelection = state.selectedSeasons.toMutableSet()
                    if (event.isSelected) newSelection.add(event.season) else newSelection.remove(
                        event.season
                    )
                    state.copy(selectedSeasons = newSelection.toList())
                }
            }

            is SearchEvent.ToggleFilterSheet -> _uiState.update {
                val newState = !it.isFilterSheetVisible
                it.copy(
                    isFilterSheetVisible = newState,
                    selectedTeams = if (newState) it.appliedTeams else it.selectedTeams,
                    selectedSeasons = if (newState) it.appliedSeasons else it.selectedSeasons,
                    sortOrder = if (newState) it.appliedSortOrder else it.sortOrder
                )
            }

            is SearchEvent.ApplyFilters -> _uiState.update {
                it.copy(
                    appliedTeams = it.selectedTeams,
                    appliedSeasons = it.selectedSeasons,
                    appliedSortOrder = it.sortOrder,
                    isFilterSheetVisible = false
                )
            }

            is SearchEvent.ResetFilters -> {
                viewModelScope.launch {
                    val defaultSortOrder =
                        settingsRepository.settings.firstOrNull()?.defaultSortOrder ?: SortOrder.PLAYER_NAME_ASC

                    _uiState.update {
                        it.copy(
                            selectedTeams = emptyList(),
                            selectedSeasons = emptyList(),
                            sortOrder = defaultSortOrder,
                            appliedTeams = emptyList(),
                            appliedSeasons = emptyList(),
                            appliedSortOrder = defaultSortOrder,
                            isFilterSheetVisible = false
                        )
                    }
                }
            }


            is SearchEvent.RemoveAppliedFilter -> {
                _uiState.update { state ->
                    when (event.filterType) {
                        FilterType.TEAM -> state.copy(
                            appliedTeams = state.appliedTeams - event.value
                        )

                        FilterType.SEASON -> state.copy(
                            appliedSeasons = state.appliedSeasons - event.value
                        )
                    }
                }
            }
        }
    }
}

data class SearchUiState(
    val searchQuery: String = "",
    val isFilterSheetVisible: Boolean = false,
    val sortOrder: SortOrder = SortOrder.PLAYER_NAME_ASC,
    val allTeams: List<String> = emptyList(),
    val allSeasons: List<String> = emptyList(),
    val selectedTeams: List<String> = emptyList(),
    val selectedSeasons: List<String> = emptyList(),
    val suggestions: List<Suggestion> = emptyList(),
    val error: String? = null,
    val appliedTeams: List<String> = emptyList(),
    val appliedSeasons: List<String> = emptyList(),
    val appliedSortOrder: SortOrder = SortOrder.PLAYER_NAME_ASC
) {
    val hasFilterChanges: Boolean
        get() = selectedTeams.sorted() != appliedTeams.sorted() ||
                selectedSeasons.sorted() != appliedSeasons.sorted() ||
                sortOrder != appliedSortOrder
}


data class SearchParameters(
    val query: String,
    val sortOrder: SortOrder,
    val teams: List<String>,
    val seasons: List<String>,
    val isWc2002Mode: Boolean
)

sealed class SearchEvent {
    data class OnQueryChanged(val query: String) : SearchEvent()
    data class OnSearchSubmitted(val query: String) : SearchEvent()
    data class OnSortOrderChanged(val sortOrder: SortOrder) : SearchEvent()
    data class OnTeamSelected(val team: String, val isSelected: Boolean) : SearchEvent()
    data class OnSeasonSelected(val season: String, val isSelected: Boolean) : SearchEvent()
    object ToggleFilterSheet : SearchEvent()
    object ApplyFilters : SearchEvent()
    object ResetFilters : SearchEvent()
    data class RemoveAppliedFilter(val filterType: FilterType, val value: String) : SearchEvent()
}

enum class FilterType {
    TEAM, SEASON
}

data class Suggestion(val text: String, val type: SuggestionType)
enum class SuggestionType { HISTORY, SUGGESTION }