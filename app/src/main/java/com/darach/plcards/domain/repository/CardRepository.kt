package com.darach.plcards.domain.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.darach.plcards.data.local.entity.RecentSearchEntity
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCards(
        config: PagingConfig,
        query: String,
        sortOrder: SortOrder,
        teams: List<String>?,
        seasons: List<String>?,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getCardDetails(id: String): Flow<CardModel?>
    suspend fun toggleFavorite(id: String, isCurrentlyFavorite: Boolean)
    suspend fun setCardAsViewed(id: String)
    suspend fun updateCardPosition(cardId: String, position: Int?)
    suspend fun getCardCount(): Int
    fun getFavorites(): Flow<List<CardModel>>

    // Home Screen
    fun getFeaturedCarouselCards(isWc2002Mode: Boolean? = false): Flow<List<CardModel>>
    fun getRecentlyViewedShelf(isWc2002Mode: Boolean? = false): Flow<List<CardModel>>

    // Curated Shelves
    fun getFeaturedShelf(isWc2002Mode: Boolean? = false): Flow<List<CardModel>>
    fun getGoldenBootWinnersShelf(isWc2002Mode: Boolean? = false): Flow<List<CardModel>>
    fun getClubLegendsShelf(isWc2002Mode: Boolean? = false): Flow<List<CardModel>>
    fun getYoungStarsShelf(isWc2002Mode: Boolean? = false): Flow<List<CardModel>>
    fun getPremierLeagueIconsShelf(isWc2002Mode: Boolean? = false): Flow<List<CardModel>>
    fun getForgottenHerosShelf(isWc2002Mode: Boolean? = false): Flow<List<CardModel>>

    // Paginated Home Screen  
    fun getRecentlyViewedPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getLatestSeasonPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getPopularTeamsPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getNewAdditionsPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    // Paginated Curated Shelves
    fun getFeaturedShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getGoldenBootWinnersShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getClubLegendsShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getYoungStarsShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getForgottenHerosShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getPremierLeagueIconsShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean? = false
    ): Flow<PagingData<CardModel>>

    fun getSearchSuggestions(query: String, limit: Int): Flow<List<String>>


    // Filters
    fun getAllTeams(isWc2002Mode: Boolean? = false): Flow<List<String>>
    fun getAllSeasons(isWc2002Mode: Boolean? = false): Flow<List<String>>

    // Recent Searches
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>
    suspend fun addRecentSearch(query: String)
}