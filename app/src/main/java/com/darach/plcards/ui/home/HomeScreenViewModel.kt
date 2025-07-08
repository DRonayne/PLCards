@file:OptIn(ExperimentalCoroutinesApi::class)

package com.darach.plcards.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.plcards.data.repository.SettingsRepository
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.domain.repository.CardRepository
import com.darach.plcards.util.AnalyticsHelper
import com.darach.plcards.util.CrashlyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

// Represents a single horizontal row (shelf) in the UI.
data class Shelf(
    val title: String,
    val type: ShelfType,
    val cardModels: List<CardModel>
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val isWc2002Mode: Boolean = false,
    val featuredCarouselCardModels: List<CardModel> = emptyList(),
    val shelves: List<Shelf> = emptyList()
)

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val crashlyticsHelper: CrashlyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        analyticsHelper.trackScreenView("Home")
        loadData()
    }

    fun retryLoadData() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            settingsRepository.settings.flatMapLatest { settings ->
                combine(
                    cardRepository.getFeaturedCarouselCards(settings.isWc2002Mode),
                    cardRepository.getRecentlyViewedShelf(settings.isWc2002Mode),
                    cardRepository.getFeaturedShelf(settings.isWc2002Mode),
                    cardRepository.getGoldenBootWinnersShelf(settings.isWc2002Mode),
                    cardRepository.getClubLegendsShelf(settings.isWc2002Mode),
                    cardRepository.getYoungStarsShelf(settings.isWc2002Mode),
                    cardRepository.getForgottenHerosShelf(settings.isWc2002Mode),
                    cardRepository.getPremierLeagueIconsShelf(settings.isWc2002Mode)
                ) { allData ->
                    val featuredCarousel = allData[0]
                    val recentlyViewed = allData[1]
                    val featured = allData[2]
                    val goldenBoot = allData[3]
                    val clubLegends = allData[4]
                    val youngStars = allData[5]
                    val forgottenHeros = allData[6]
                    val premierLeagueIcons = allData[7]

                    // Build a dynamic list of shelves based on mode
                    val shelves = buildList {
                        if (settings.isWc2002Mode == true) {
                            // WC2002 specific shelves with custom titles
                            if (featured.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "WC2002 Tournament Icons",
                                        ShelfType.FEATURED_SHELF,
                                        featured
                                    )
                                )
                            }
                            if (goldenBoot.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "WC2002 Golden Boot Race",
                                        ShelfType.GOLDEN_BOOT_WINNERS,
                                        goldenBoot
                                    )
                                )
                            }
                            if (clubLegends.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "Premier League at WC2002",
                                        ShelfType.CLUB_LEGENDS,
                                        clubLegends
                                    )
                                )
                            }
                            if (youngStars.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "WC2002 Young Talents",
                                        ShelfType.YOUNG_STARS,
                                        youngStars
                                    )
                                )
                            }
                            if (forgottenHeros.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "WC2002 Surprise Heroes",
                                        ShelfType.FORGOTTEN_HEROS,
                                        forgottenHeros
                                    )
                                )
                            }
                            if (premierLeagueIcons.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "Group of Death Stars",
                                        ShelfType.PREMIER_LEAGUE_ICONS,
                                        premierLeagueIcons
                                    )
                                )
                            }
                            // Add ROI players shelf if available
                            if (recentlyViewed.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "Ireland at WC2002",
                                        ShelfType.RECENTLY_VIEWED,
                                        recentlyViewed
                                    )
                                )
                            }
                        } else {
                            // Regular shelves
                            if (featured.isNotEmpty()) {
                                add(Shelf("Featured Players", ShelfType.FEATURED_SHELF, featured))
                            }
                            if (goldenBoot.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "Golden Boot Winners",
                                        ShelfType.GOLDEN_BOOT_WINNERS,
                                        goldenBoot
                                    )
                                )
                            }
                            if (clubLegends.isNotEmpty()) {
                                add(Shelf("United Legends", ShelfType.CLUB_LEGENDS, clubLegends))
                            }
                            if (youngStars.isNotEmpty()) {
                                add(Shelf("Young Stars", ShelfType.YOUNG_STARS, youngStars))
                            }
                            if (forgottenHeros.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "Forgotten Heroes",
                                        ShelfType.FORGOTTEN_HEROS,
                                        forgottenHeros
                                    )
                                )
                            }
                            if (premierLeagueIcons.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "Premier League Icons",
                                        ShelfType.PREMIER_LEAGUE_ICONS,
                                        premierLeagueIcons
                                    )
                                )
                            }
                            if (recentlyViewed.isNotEmpty()) {
                                add(
                                    Shelf(
                                        "Recently Viewed",
                                        ShelfType.RECENTLY_VIEWED,
                                        recentlyViewed
                                    )
                                )
                            }
                        }
                    }

                    Log.d(
                        "HomeScreenViewModel",
                        "Data transformed. Carousel: ${featuredCarousel.size}, Shelves: ${shelves.size}"
                    )

                    HomeUiState(
                        isLoading = false,
                        isWc2002Mode = settings.isWc2002Mode == true,
                        featuredCarouselCardModels = featuredCarousel,
                        shelves = shelves
                    )
                }
            }
                .onStart {
                    Log.d("HomeScreenViewModel", "Starting data load, setting loading state.")
                    emit(HomeUiState(isLoading = true))
                }
                .catch { e ->
                    Log.e("HomeScreenViewModel", "Error loading home screen data", e)
                    crashlyticsHelper.recordException(e)
                    emit(
                        HomeUiState(
                            isLoading = false,
                            error = "Failed to load cards. Please check your connection and try again."
                        )
                    )
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }
}