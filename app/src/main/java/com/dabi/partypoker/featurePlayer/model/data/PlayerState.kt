package com.dabi.partypoker.featurePlayer.model.data

import androidx.annotation.RawRes
import com.dabi.easylocalgame.clientSide.data.IPlayerState
import com.dabi.partypoker.utils.Card


data class PlayerState(
    override var nickname: String = "",
    override var id: String = "",
    @RawRes override var avatarId: Int? = null,

    override var isServer: Boolean = false,

    var isReadyToPlay: Boolean = false,
    var isFolded: Boolean = false,

    var isPlayingNow: Boolean = false,
    var isBigBlind: Boolean = false,
    var isSmallBlind: Boolean = false,
    var isDealer: Boolean = false,

    var money: Int = 0,
    var holeCards: List<Card> = emptyList(),
    var called: Int = 0,
    var allIn: Boolean = false,
): IPlayerState
