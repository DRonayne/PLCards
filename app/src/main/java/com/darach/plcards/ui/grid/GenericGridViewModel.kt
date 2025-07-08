@file:OptIn(ExperimentalCoroutinesApi::class)

package com.darach.plcards.ui.grid

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.darach.plcards.data.repository.SettingsRepository
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.domain.repository.CardRepository
import com.darach.plcards.ui.home.ShelfType
import com.darach.plcards.util.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class GenericGridViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository,
    private val analyticsHelper: AnalyticsHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shelfType: ShelfType = ShelfType.valueOf(
        checkNotNull(savedStateHandle.get<String>("shelfType"))
    )

    init {
        analyticsHelper.trackScreenView("GenericGrid")
        analyticsHelper.trackShelfNavigation(shelfType.name)
    }

    private val _title = MutableStateFlow(getTitleForShelfType(shelfType))
    val title: StateFlow<String> = _title.asStateFlow()

    val cards: Flow<PagingData<CardModel>> = settingsRepository.settings
        .flatMapLatest { settings ->
            getCardsForShelfType(
                shelfType,
                settings.isWc2002Mode ?: false
            )
        }
        .cachedIn(viewModelScope)

    private fun getCardsForShelfType(
        shelfType: ShelfType,
        isWc2002Mode: Boolean
    ): Flow<PagingData<CardModel>> {
        val config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )

        return when (shelfType) {
            ShelfType.RECENTLY_VIEWED -> cardRepository.getRecentlyViewedPaginated(
                config,
                isWc2002Mode
            )

            ShelfType.FEATURED_SHELF -> cardRepository.getFeaturedShelfPaginated(
                config,
                isWc2002Mode
            )

            ShelfType.GOLDEN_BOOT_WINNERS -> cardRepository.getGoldenBootWinnersShelfPaginated(
                config,
                isWc2002Mode
            )

            ShelfType.CLUB_LEGENDS -> cardRepository.getClubLegendsShelfPaginated(
                config,
                isWc2002Mode
            )

            ShelfType.YOUNG_STARS -> cardRepository.getYoungStarsShelfPaginated(
                config,
                isWc2002Mode
            )

            ShelfType.FORGOTTEN_HEROS -> cardRepository.getForgottenHerosShelfPaginated(
                config,
                isWc2002Mode
            )

            ShelfType.PREMIER_LEAGUE_ICONS -> cardRepository.getPremierLeagueIconsShelfPaginated(
                config,
                isWc2002Mode
            )
        }
    }

    private fun getTitleForShelfType(shelfType: ShelfType): String {
        return when (shelfType) {
            ShelfType.RECENTLY_VIEWED -> "Recently Viewed"
            ShelfType.FEATURED_SHELF -> "Featured Players"
            ShelfType.GOLDEN_BOOT_WINNERS -> "Golden Boot Winners"
            ShelfType.CLUB_LEGENDS -> "United Legends"
            ShelfType.YOUNG_STARS -> "Young Stars"
            ShelfType.FORGOTTEN_HEROS -> "Forgotten Heroes"
            ShelfType.PREMIER_LEAGUE_ICONS -> "Premier League Icons"
        }
    }
}