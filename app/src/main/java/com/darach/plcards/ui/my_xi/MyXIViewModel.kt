package com.darach.plcards.ui.my_xi

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.plcards.domain.model.CardModel
import com.darach.plcards.domain.repository.CardRepository
import com.darach.plcards.util.AnalyticsHelper
import com.darach.plcards.util.ShareHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyXIViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val shareHelper: ShareHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyXIUiState())
    val uiState: StateFlow<MyXIUiState> = _uiState.asStateFlow()

    init {
        analyticsHelper.trackScreenView("MyXI")
        analyticsHelper.trackMyXIViewed()
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            cardRepository.getFavorites().collect { favorites ->
                val playersInFormation = mutableMapOf<Int, CardModel>()
                val availablePlayers = mutableListOf<CardModel>()

                favorites.forEach { card ->
                    if (card.positionInFormation != null) {
                        playersInFormation[card.positionInFormation] = card
                    } else {
                        availablePlayers.add(card)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    playersInFormation = playersInFormation,
                    availablePlayers = availablePlayers
                )
            }
        }
    }

    fun onEvent(event: MyXIEvent) {
        when (event) {
            is MyXIEvent.ChangeFormation -> {
                _uiState.value = _uiState.value.copy(formation = event.formation)
            }

            is MyXIEvent.SlotClicked -> {
                _uiState.value = _uiState.value.copy(slotToFill = event.slotIndex)
            }

            is MyXIEvent.AssignPlayerToSlot -> {
                assignPlayerToSlot(event.player)
            }

            is MyXIEvent.DismissPlayerSelection -> {
                _uiState.value = _uiState.value.copy(slotToFill = null)
            }

            is MyXIEvent.RemovePlayerFromSlot -> {
                removePlayerFromSlot(event.cardModel)
            }
        }
    }

    private fun removePlayerFromSlot(cardModel: CardModel) {
        viewModelScope.launch {
            analyticsHelper.trackMyXIUpdated()
            cardRepository.updateCardPosition(cardModel.id, null)
        }
    }

    private fun assignPlayerToSlot(player: CardModel) {
        val currentState = _uiState.value
        val slotIndex = currentState.slotToFill

        if (slotIndex != null) {
            viewModelScope.launch {
                analyticsHelper.trackMyXIUpdated()
                val existingPlayerInSlot = currentState.playersInFormation[slotIndex]

                if (existingPlayerInSlot != null) {
                    // Move existing player to available players (remove from formation)
                    cardRepository.updateCardPosition(existingPlayerInSlot.id, null)
                }

                // Place selected player in the slot
                cardRepository.updateCardPosition(player.id, slotIndex)

                // Clear slot selection
                _uiState.value = _uiState.value.copy(slotToFill = null)
            }
        }
    }

    fun shareFormation(context: Context) {
        val currentState = _uiState.value
        //analyticsHelper.trackMyXIShared()
        viewModelScope.launch {
            try {
                shareHelper.shareTeamFormation(
                    context = context,
                    players = currentState.playersInFormation,
                    formation = currentState.formation
                )
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

data class MyXIUiState(
    val formation: Formation = Formation.F442,
    val playersInFormation: Map<Int, CardModel> = emptyMap(),
    val availablePlayers: List<CardModel> = emptyList(),
    val slotToFill: Int? = null
)


sealed class MyXIEvent {
    data class ChangeFormation(val formation: Formation) : MyXIEvent()
    data class SlotClicked(val slotIndex: Int) : MyXIEvent()
    data class AssignPlayerToSlot(val player: CardModel) : MyXIEvent()
    data class RemovePlayerFromSlot(val cardModel: CardModel) : MyXIEvent()
    object DismissPlayerSelection : MyXIEvent()
}