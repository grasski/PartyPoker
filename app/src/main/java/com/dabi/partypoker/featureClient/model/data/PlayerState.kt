package com.dabi.partypoker.featureClient.model.data

import com.dabi.partypoker.utils.Card

typealias endpointID = String

data class PlayerState(
    var nickname: String = "",
    var id: endpointID = "",

    var isReadyToPlay: Boolean = false,
    var isFolded: Boolean = false,

    var isPlayingNow: Boolean = false,
    var isBigBlind: Boolean = false,
    var isSmallBlind: Boolean = false,
    var isDealer: Boolean = false,

    var money: Int = 0,
    var holeCards: List<Card> = emptyList(),
    var called: Int = 0
)
