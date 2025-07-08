package com.darach.plcards.ui.card_detail

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.domain.repository.CardRepository
import com.darach.plcards.util.AnalyticsHelper
import com.darach.plcards.util.ShareHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    @ApplicationContext private val context: Context,
    private val analyticsHelper: AnalyticsHelper,
    private val shareHelper: ShareHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cardId: String = checkNotNull(savedStateHandle.get<String>("cardId"))

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    init {
        analyticsHelper.trackScreenView("CardDetail")
        loadCardDetails()
        setCardAsViewed()
    }

    private fun loadCardDetails() {
        viewModelScope.launch {
            cardRepository.getCardDetails(cardId).collect { card ->
                card?.let {
                    analyticsHelper.trackCardViewed(it.id, it.playerName)
                }
                _uiState.value = _uiState.value.copy(cardModel = card)
            }
        }
    }

    private fun setCardAsViewed() {
        viewModelScope.launch {
            cardRepository.setCardAsViewed(cardId)
        }
    }

    fun onEvent(event: CardDetailEvent) {
        when (event) {
            is CardDetailEvent.ToggleFavorite -> {
                analyticsHelper.trackFavoriteToggled(event.cardModel.id, !event.cardModel.isFavorite)
                viewModelScope.launch {
                    cardRepository.toggleFavorite(event.cardModel.id, event.cardModel.isFavorite)
                }
            }
            is CardDetailEvent.UpdatePaletteColor -> {
                _uiState.value = _uiState.value.copy(paletteColor = event.color)
            }
            is CardDetailEvent.ShareCard -> {
                analyticsHelper.trackShare("card")
                viewModelScope.launch {
                    try {
                        shareHelper.shareCardDetails(context, event.cardModel)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context, 
                            "Failed to generate shareable image. Please try again.", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}

data class CardDetailUiState(
    val cardModel: CardModel? = null,
    val paletteColor: Color = Color.Unspecified
)

sealed class CardDetailEvent {
    data class ToggleFavorite(val cardModel: CardModel) : CardDetailEvent()
    data class UpdatePaletteColor(val color: Color) : CardDetailEvent()
    data class ShareCard(val cardModel: CardModel) : CardDetailEvent()
}