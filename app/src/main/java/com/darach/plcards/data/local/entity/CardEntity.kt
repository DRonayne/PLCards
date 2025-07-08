package com.darach.plcards.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String, // "Season-CardNumber"
    val season: String,
    val cardNumber: String,
    val playerName: String,
    val team: String,
    val cardImageUrl: String,
    val isFavorite: Boolean = false,
    val lastViewedTimestamp: Long? = null,
    val positionInFormation: Int? = null
)