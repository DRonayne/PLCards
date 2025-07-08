package com.darach.plcards.data.mappers

import android.util.Log
import com.darach.plcards.data.local.entity.CardEntity
import com.darach.plcards.data.remote.dto.CardDto
import com.darach.plcards.domain.model.CardModel

fun CardDto.toCardEntity(): CardEntity {
    val safeCardNumber = this.cardNumber ?: "Unknown"
    val safeSeason = this.season ?: "Unknown"
    val safePlayerName = this.playerName ?: "Unknown Player"
    val safeTeam = this.team?.takeIf { it.isNotEmpty() } ?: "N/A"
    val safeImageUrl = this.cardImageUrl ?: ""

    // Log the mapping for debugging
    Log.v(
        "CardMappers",
        "Mapping: season='$safeSeason', cardNumber='$safeCardNumber', playerName='$safePlayerName', team='$safeTeam'"
    )

    return CardEntity(
        id = "${safeSeason}-${safeCardNumber}",
        season = safeSeason,
        cardNumber = safeCardNumber,
        playerName = safePlayerName,
        team = safeTeam,
        cardImageUrl = safeImageUrl
    )
}

fun CardEntity.toCard(): CardModel {
    return CardModel(
        id = this.id,
        season = this.season,
        cardNumber = this.cardNumber,
        playerName = this.playerName,
        team = this.team,
        cardImageUrl = this.cardImageUrl,
        isFavorite = this.isFavorite,
        lastViewedTimestamp = this.lastViewedTimestamp,
        positionInFormation = this.positionInFormation
    )
}