package com.dabi.partypoker.featureCore.interfaces

import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureServer.model.data.GameState

interface PlayerCoreInterface {
    fun onPlayerEvent(event: PlayerEvents)

    fun getPlayerState(): PlayerState
    fun getGameState(): GameState

    fun checkEnabled(): Boolean{
        return activeCallValue() == 0 && getPlayerState().isPlayingNow
    }
    fun activeCallValue(): Int{
        if (!getPlayerState().isPlayingNow || getGameState().activeRaise == null || getGameState().round == 0){
            return 0
        }
        getGameState().activeRaise?.let { (playerId, amount) ->
            return amount - getPlayerState().called
        }
        return 0
    }
    fun minimalRaise(): Int{
        return activeCallValue() + getGameState().bigBlindAmount
    }
}