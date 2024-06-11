package com.dabi.partypoker.featureServer.model.data

import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.model.data.endpointID
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardsCombination
import com.dabi.partypoker.utils.UiTexts


data class GameState(
    var players: Map<endpointID, PlayerState> = mapOf(),
//    var playingPlayers: Map<endpointID, PlayerState> = mapOf(),
    var gameReadyPlayers: Set<endpointID> = emptySet(),

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

    var messages: List<UiTexts.StringResource> = emptyList(),

    var winners: List<PlayerState> = emptyList(),
    var winningCombination: CardsCombination = CardsCombination.NONE,
    var winnersCards: String? = null,
    var nextGameIn: Int = 15
)
