package com.dabi.partypoker.featureCore.interfaces

import com.dabi.partypoker.featurePlayer.model.data.PlayerState
import com.dabi.partypoker.featurePlayer.viewmodel.PlayerEvents
import com.dabi.partypoker.featureServer.model.data.GameState


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
            val toCall = amount - getPlayerState().called
            if (toCall >= getPlayerState().money){
                return getPlayerState().money
            }
            return toCall
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