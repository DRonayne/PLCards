package com.darach.plcards.domain.model

enum class SortOrder(
    val databaseKey: String,
    val displayName: String
) {
    PLAYER_NAME_ASC("PLAYER_NAME_ASC", "Player A-Z"),
    PLAYER_NAME_DESC("PLAYER_NAME_DESC", "Player Z-A"),
    SEASON_NEWEST("SEASON_NEWEST", "Season (Newest)"),
    SEASON_OLDEST("SEASON_OLDEST", "Season (Oldest)"),
    TEAM_ASC("TEAM_ASC", "Team A-Z"),
    TEAM_DESC("TEAM_DESC", "Team Z-A");

    companion object {

        fun fromSettingsKey(key: String): SortOrder {
            return when (key) {
                "player_name_asc" -> PLAYER_NAME_ASC
                "player_name_desc" -> PLAYER_NAME_DESC
                "season_newest" -> SEASON_NEWEST
                "season_oldest" -> SEASON_OLDEST
                "team_asc" -> TEAM_ASC
                "team_desc" -> TEAM_DESC
                else -> PLAYER_NAME_ASC
            }
        }

        fun toSettingsKey(sortOrder: SortOrder): String {
            return when (sortOrder) {
                PLAYER_NAME_ASC -> "player_name_asc"
                PLAYER_NAME_DESC -> "player_name_desc"
                SEASON_NEWEST -> "season_newest"
                SEASON_OLDEST -> "season_oldest"
                TEAM_ASC -> "team_asc"
                TEAM_DESC -> "team_desc"
            }
        }
    }
}