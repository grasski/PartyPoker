package com.dabi.partypoker.featureCore.interfaces

import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureServer.model.data.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PlayerCoreInterface {
    fun onPlayerEvent(event: PlayerEvents)

    fun getPlayerState(): PlayerState
    fun getGameState(): GameState


    fun checkEnabled(): Boolean{
        return activeCallValue() == 0
    }
    fun activeCallValue(): Int{
        if (!getPlayerState().isPlayingNow || getGameState().activeRaise == null || getGameState().round == 0){
            return 0
        }
        getGameState().activeRaise?.let { (_, amount) ->
            return amount - getPlayerState().called
        }
        return 0
    }
    fun minimalRaise(): Int{
        if (activeCallValue() == 0){
            return getGameState().gameSettings.bigBlindAmount
        }
        if (activeCallValue() == getGameState().gameSettings.smallBlindAmount){
            return getGameState().gameSettings.smallBlindAmount + getGameState().gameSettings.bigBlindAmount
        }
        return activeCallValue() * 2
    }
}