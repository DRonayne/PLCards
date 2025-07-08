package com.darach.plcards.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.darach.plcards.data.local.ShelfData
import com.darach.plcards.data.local.WC2002ShelfData
import com.darach.plcards.data.local.dao.CardDao
import com.darach.plcards.data.local.dao.RecentSearchDao
import com.darach.plcards.data.local.entity.RecentSearchEntity
import com.darach.plcards.data.mappers.toCard
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.domain.model.SortOrder
import com.darach.plcards.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val cardDao: CardDao,
    private val recentSearchDao: RecentSearchDao
) : CardRepository {

    override fun getCards(
        config: PagingConfig,
        query: String,
        sortOrder: SortOrder,
        teams: List<String>?,
        seasons: List<String>?,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = {
                cardDao.getCardsPaginated(
                    query,
                    sortOrder.databaseKey,
                    teams,
                    seasons,
                    isWc2002Mode ?: false
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getCardDetails(id: String): Flow<CardModel?> {
        return cardDao.getCardById(id).map { it?.toCard() }
    }

    override suspend fun toggleFavorite(id: String, isCurrentlyFavorite: Boolean) {
        cardDao.setFavorite(id, !isCurrentlyFavorite)
    }


    override suspend fun setCardAsViewed(id: String) {
        cardDao.setLastViewed(id, System.currentTimeMillis())
    }

    override suspend fun updateCardPosition(cardId: String, position: Int?) {
        cardDao.updateCardPosition(cardId, position)
    }

    override suspend fun getCardCount(): Int {
        return cardDao.count()
    }

    override fun getFavorites(): Flow<List<CardModel>> {
        return cardDao.getFavorites().map { entities -> entities.map { it.toCard() } }
    }

    override fun getFeaturedCarouselCards(isWc2002Mode: Boolean?): Flow<List<CardModel>> {
        return if (isWc2002Mode == true) {
            val carouselIds = WC2002ShelfData.tournamentIcons.take(6)
            cardDao.getCardsByIds(carouselIds).map { entities -> entities.map { it.toCard() } }
        } else {
            val carouselIds = ShelfData.featuredShelf.take(6)
            cardDao.getCardsByIds(carouselIds).map { entities -> entities.map { it.toCard() } }
        }
    }

    override fun getRecentlyViewedShelf(isWc2002Mode: Boolean?): Flow<List<CardModel>> {
        return if (isWc2002Mode == true) {
            cardDao.getCardsByIds(WC2002ShelfData.irelandAtWC2002)
                .map { entities -> entities.map { it.toCard() } }
        } else {
            cardDao.getRecentlyViewed(false).map { entities -> entities.map { it.toCard() } }
        }
    }

    override fun getFeaturedShelf(isWc2002Mode: Boolean?): Flow<List<CardModel>> {
        return if (isWc2002Mode == true) {
            cardDao.getCardsByIds(WC2002ShelfData.tournamentIcons)
                .map { entities -> entities.map { it.toCard() } }
        } else {
            cardDao.getCardsByIds(ShelfData.featuredShelf)
                .map { entities -> entities.map { it.toCard() } }
        }
    }

    override fun getGoldenBootWinnersShelf(isWc2002Mode: Boolean?): Flow<List<CardModel>> {
        return if (isWc2002Mode == true) {
            cardDao.getCardsByIds(WC2002ShelfData.goldenBootRace)
                .map { entities -> entities.map { it.toCard() } }
        } else {
            cardDao.getCardsByIds(ShelfData.goldenBootWinners)
                .map { entities -> entities.map { it.toCard() } }
        }
    }

    override fun getClubLegendsShelf(isWc2002Mode: Boolean?): Flow<List<CardModel>> {
        return if (isWc2002Mode == true) {
            cardDao.getCardsByIds(WC2002ShelfData.premierLeagueStars)
                .map { entities -> entities.map { it.toCard() } }
        } else {
            cardDao.getCardsByIds(ShelfData.manuLegends)
                .map { entities -> entities.map { it.toCard() } }
        }
    }

    override fun getYoungStarsShelf(isWc2002Mode: Boolean?): Flow<List<CardModel>> {
        return if (isWc2002Mode == true) {
            cardDao.getCardsByIds(WC2002ShelfData.youngTalents)
                .map { entities -> entities.map { it.toCard() } }
        } else {
            cardDao.getCardsByIds(ShelfData.youngStars)
                .map { entities -> entities.map { it.toCard() } }
        }
    }

    override fun getPremierLeagueIconsShelf(isWc2002Mode: Boolean?): Flow<List<CardModel>> {
        return if (isWc2002Mode == true) {
            cardDao.getCardsByIds(WC2002ShelfData.groupOfDeath)
                .map { entities -> entities.map { it.toCard() } }
        } else {
            cardDao.getCardsByIds(ShelfData.premierLeagueIcons)
                .map { entities -> entities.map { it.toCard() } }
        }
    }

    override fun getForgottenHerosShelf(isWc2002Mode: Boolean?): Flow<List<CardModel>> {
        return if (isWc2002Mode == true) {
            cardDao.getCardsByIds(WC2002ShelfData.surprisePackages)
                .map { entities -> entities.map { it.toCard() } }
        } else {
            cardDao.getCardsByIds(ShelfData.forgottenHeros)
                .map { entities -> entities.map { it.toCard() } }
        }
    }

    override fun getRecentlyViewedPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = { cardDao.getRecentlyViewedPaginated(isWc2002Mode ?: false) }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getLatestSeasonPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = { cardDao.getLatestSeasonPaginated(isWc2002Mode ?: false) }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getPopularTeamsPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = { cardDao.getPopularTeamsPaginated(isWc2002Mode ?: false) }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getNewAdditionsPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = { cardDao.getNewAdditionsPaginated(isWc2002Mode ?: false) }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getFeaturedShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = {
                cardDao.getCardsByIdsPaginated(
                    ShelfData.featuredShelf,
                    isWc2002Mode ?: false
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getGoldenBootWinnersShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = {
                cardDao.getCardsByIdsPaginated(
                    ShelfData.goldenBootWinners,
                    isWc2002Mode ?: false
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getClubLegendsShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = {
                cardDao.getCardsByIdsPaginated(
                    ShelfData.manuLegends,
                    isWc2002Mode ?: false
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getYoungStarsShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = {
                cardDao.getCardsByIdsPaginated(
                    ShelfData.youngStars,
                    isWc2002Mode ?: false
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getPremierLeagueIconsShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = {
                cardDao.getCardsByIdsPaginated(
                    ShelfData.premierLeagueIcons,
                    isWc2002Mode ?: false
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getForgottenHerosShelfPaginated(
        config: PagingConfig,
        isWc2002Mode: Boolean?
    ): Flow<PagingData<CardModel>> {
        return Pager(
            config = config,
            pagingSourceFactory = {
                cardDao.getCardsByIdsPaginated(
                    ShelfData.forgottenHeros,
                    isWc2002Mode ?: false
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toCard() }
        }
    }

    override fun getSearchSuggestions(query: String, limit: Int): Flow<List<String>> {
        return cardDao.getSearchSuggestions(query, limit)
    }

    override fun getAllTeams(isWc2002Mode: Boolean?): Flow<List<String>> =
        cardDao.getAllTeams(isWc2002Mode ?: false)

    override fun getAllSeasons(isWc2002Mode: Boolean?): Flow<List<String>> =
        cardDao.getAllSeasons(isWc2002Mode ?: false)

    override fun getRecentSearches(): Flow<List<RecentSearchEntity>> =
        recentSearchDao.getRecentSearches()

    override suspend fun addRecentSearch(query: String) {
        // Trim and ignore blank searches to keep history clean
        val trimmedQuery = query.trim()
        if (trimmedQuery.isNotBlank()) {
            recentSearchDao.insert(
                RecentSearchEntity(
                    query = trimmedQuery,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}