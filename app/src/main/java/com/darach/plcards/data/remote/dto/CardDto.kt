package com.darach.plcards.data.remote.dto

import com.squareup.moshi.Json

data class CardApiResponse(
    @field:Json(name = "count") val count: Int,
    @field:Json(name = "cards") val cards: List<CardDto>
)

data class CardDto(
    @Json(name = "Season") val season: String?,
    @Json(name = "Card Number") val cardNumber: String?,
    @Json(name = "Player Name") val playerName: String?,
    @Json(name = "Team") val team: String?,
    @Json(name = "Card Image URL") val cardImageUrl: String?
)