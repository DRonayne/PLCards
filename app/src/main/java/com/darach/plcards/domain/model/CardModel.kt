package com.darach.plcards.domain.model

data class CardModel(
    val id: String,
    val season: String,
    val cardNumber: String,
    val playerName: String,
    val team: String,
    val cardImageUrl: String,
    val isFavorite: Boolean,
    val lastViewedTimestamp: Long?,
    val positionInFormation: Int? = 0
)