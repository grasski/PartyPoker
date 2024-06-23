package com.dabi.partypoker.featureServer.model.data

import androidx.annotation.IntRange
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.model.data.endpointID
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardsCombination
import com.dabi.partypoker.utils.UiTexts
import org.jetbrains.annotations.Range


data class SeatPosition(@IntRange(from = 0, to = 9) val position: Int) {
    init {
        require(position in 0..9) { "Position must be in range 0 to 9" }
    }
}


data class GameState(
    var players: Map<endpointID, PlayerState> = mapOf(),
//    var playingPlayers: Map<endpointID, PlayerState> = mapOf(),
    var gameReadyPlayers: Map<endpointID, SeatPosition> = emptyMap(),
    var seatPositions: Map<endpointID, SeatPosition> = emptyMap(), // playerId, seatPosition

    var started: Boolean = false,
    var ongoing: Boolean = false,

    var cardsDeck: List<Card> = emptyList(),
    var cardsTable: List<Card> = emptyList(),

    var activeRaise: Pair<endpointID, Int>? = null,   // playerId, amount
    var bigBlindRaised: Boolean = false,    // BB raised in round 1
    var bank: Int = 0,

    var smallBlindAmount: Int = 25,
    var bigBlindAmount: Int = 50,

    var dealerId: endpointID? = null,
    var smallBlindId: endpointID? = null,
    var bigBlindId: endpointID? = null,
    var roundStartedId: endpointID? = null,
    var playingNow: endpointID? = null,

    var round: Int = 0,
    var games: Int = 0,

    var messages: List<UiTexts> = emptyList(),
    var playerTimerDuration: Int = 12,

    var nextGameIn: Int = 15
){
    fun getAvailableRandomPosition(): Int? {
        val takenPositions = seatPositions.values.map { it.position }.toSet()
        val availablePositions = (0..9).filter { it !in takenPositions }
        return if (availablePositions.isNotEmpty()) {
            availablePositions.random()
        } else {
            null
        }
    }
}
