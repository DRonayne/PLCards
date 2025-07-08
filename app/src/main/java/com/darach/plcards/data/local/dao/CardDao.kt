package com.darach.plcards.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.darach.plcards.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

data class CardUserState(
    val id: String,
    val isFavorite: Boolean,
    val positionInFormation: Int?
)

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<CardEntity>)

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardById(id: String): Flow<CardEntity?>

    @Query("SELECT id, isFavorite, positionInFormation FROM cards")
    suspend fun getAllCardsUserState(): List<CardUserState>

    @Query(
        """
        SELECT * FROM cards 
        WHERE (:query = '' OR playerName LIKE '%' || :query || '%' OR team LIKE '%' || :query || '%' OR season LIKE '%' || :query || '%')
        AND (:teams IS NULL OR team IN (:teams))
        AND (:seasons IS NULL OR season IN (:seasons))
        AND (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY 
            CASE WHEN :sortOrder = 'PLAYER_NAME_ASC' THEN playerName END ASC,
            CASE WHEN :sortOrder = 'PLAYER_NAME_DESC' THEN playerName END DESC,
            CASE WHEN :sortOrder = 'SEASON_NEWEST' THEN CAST(SUBSTR(season, 1, 4) AS INT) END DESC,
            CASE WHEN :sortOrder = 'SEASON_OLDEST' THEN CAST(SUBSTR(season, 1, 4) AS INT) END ASC,
            CASE WHEN :sortOrder = 'TEAM_ASC' THEN team END ASC,
            CASE WHEN :sortOrder = 'TEAM_DESC' THEN team END DESC
    """
    )
    fun getCardsPaginated(
        query: String,
        sortOrder: String,
        teams: List<String>?,
        seasons: List<String>?,
        isWc2002Mode: Boolean
    ): PagingSource<Int, CardEntity>

    @Query("SELECT * FROM cards WHERE isFavorite = 1 ORDER BY playerName ASC")
    fun getFavorites(): Flow<List<CardEntity>>

    @Query("UPDATE cards SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE cards SET positionInFormation = :position WHERE id = :cardId")
    suspend fun updateCardPosition(cardId: String, position: Int?)

    @Query(
        """
        SELECT * FROM cards 
        WHERE lastViewedTimestamp IS NOT NULL 
        AND (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY lastViewedTimestamp DESC 
        LIMIT 20
    """
    )
    fun getRecentlyViewed(isWc2002Mode: Boolean): Flow<List<CardEntity>>

    @Query("UPDATE cards SET lastViewedTimestamp = :timestamp WHERE id = :id")
    suspend fun setLastViewed(id: String, timestamp: Long)

    @Query("SELECT COUNT(id) FROM cards")
    suspend fun count(): Int

    @Query(
        """
        SELECT * FROM cards 
        WHERE (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY RANDOM() 
        LIMIT :limit
    """
    )
    fun getRandomCards(limit: Int, isWc2002Mode: Boolean): Flow<List<CardEntity>>

    // Home Screen Shelf Queries
    @Query("SELECT * FROM cards WHERE id IN (:ids)")
    fun getCardsByIds(ids: List<String>): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY RANDOM() LIMIT 15")
    fun getLatestSeasonShelf(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY RANDOM() LIMIT 15")
    fun getPopularTeamsShelf(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards ORDER BY RANDOM() LIMIT 15")
    fun getNewAdditions(): Flow<List<CardEntity>>

    // Paginated Home Screen Shelf Queries
    @Query(
        """
        SELECT * FROM cards 
        WHERE lastViewedTimestamp IS NOT NULL 
        AND (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY lastViewedTimestamp DESC
    """
    )
    fun getRecentlyViewedPaginated(isWc2002Mode: Boolean): PagingSource<Int, CardEntity>

    @Query(
        """
        SELECT * FROM cards 
        WHERE (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY CAST(SUBSTR(season, 1, 4) AS INT) DESC, playerName ASC
    """
    )
    fun getLatestSeasonPaginated(isWc2002Mode: Boolean): PagingSource<Int, CardEntity>

    @Query(
        """
        SELECT * FROM cards 
        WHERE (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY team ASC, playerName ASC
    """
    )
    fun getPopularTeamsPaginated(isWc2002Mode: Boolean): PagingSource<Int, CardEntity>

    @Query(
        """
        SELECT * FROM cards 
        WHERE (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY id DESC
    """
    )
    fun getNewAdditionsPaginated(isWc2002Mode: Boolean): PagingSource<Int, CardEntity>

    @Query(
        """
        SELECT * FROM cards 
        WHERE id IN (:ids)
        AND (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY playerName ASC
    """
    )
    fun getCardsByIdsPaginated(
        ids: List<String>,
        isWc2002Mode: Boolean
    ): PagingSource<Int, CardEntity>

    // Search Filter Queries
    @Query(
        """
        SELECT DISTINCT team FROM cards 
        WHERE team != 'N/A' 
        AND (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY team ASC
    """
    )
    fun getAllTeams(isWc2002Mode: Boolean = false): Flow<List<String>>

    @Query(
        """
        SELECT DISTINCT season FROM cards 
        WHERE (:isWc2002Mode = 1 AND season = 'WC2002' OR :isWc2002Mode = 0 AND season != 'WC2002')
        ORDER BY season DESC
    """
    )
    fun getAllSeasons(isWc2002Mode: Boolean = false): Flow<List<String>>

    @Query(
        """
        SELECT DISTINCT playerName FROM cards
        WHERE playerName LIKE :query || '%'
        ORDER BY playerName ASC
        LIMIT :limit
    """
    )
    fun getSearchSuggestions(query: String, limit: Int): Flow<List<String>>
}