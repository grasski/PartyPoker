package com.dabi.partypoker.featureClient.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dabi.partypoker.featureClient.model.ClientBridge
import com.dabi.partypoker.featureClient.model.ClientBridgeEvents
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.utils.ClientPayloadType
import com.dabi.partypoker.utils.toClientPayload
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


sealed class PlayerEvents{
    data object Ready: PlayerEvents()
    data object Disconnect: PlayerEvents()

    data object Check: PlayerEvents()
    data class Call(val amount: Int): PlayerEvents()
    data class Raise(val amount: Int): PlayerEvents()
    data object Fold: PlayerEvents()
}


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
): ViewModel() {
    private val _playerState: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()


    fun onPlayerEvent(event: PlayerEvents){
        when(event){
            PlayerEvents.Disconnect -> {
                val clientPayload = toClientPayload(ClientPayloadType.ACTION_DISCONNECTED, null)
                clientBridge.sendPayload(clientPayload)
            }

            PlayerEvents.Ready -> {
                val clientPayload = toClientPayload(ClientPayloadType.ACTION_READY, null)
                clientBridge.sendPayload(clientPayload)
            }

            PlayerEvents.Check -> {
                val clientPayload = toClientPayload(ClientPayloadType.ACTION_CHECK, null)
                clientBridge.sendPayload(clientPayload)
            }
            is PlayerEvents.Call -> {
                val clientPayload = toClientPayload(ClientPayloadType.ACTION_CALL, event.amount)
                clientBridge.sendPayload(clientPayload)
            }
            is PlayerEvents.Raise -> {
                val clientPayload = toClientPayload(ClientPayloadType.ACTION_RAISE, event.amount)
                clientBridge.sendPayload(clientPayload)
            }
            PlayerEvents.Fold -> {
                val clientPayload = toClientPayload(ClientPayloadType.ACTION_FOLD, null)
                clientBridge.sendPayload(clientPayload)
            }
        }
    }


    val clientBridge: ClientBridge = ClientBridge(connectionsClient, this::onClientBridgeEvent)
    private fun onClientBridgeEvent(event: ClientBridgeEvents){
        when(event){
            is ClientBridgeEvents.Connect -> {
                _playerState.update { it.copy(
                    nickname = event.nickname
                ) }
            }
            ClientBridgeEvents.ClientConnected -> {
                val clientPayload = toClientPayload(ClientPayloadType.CONNECTED, _playerState.value.nickname)
                clientBridge.sendPayload(clientPayload)
            }

            is ClientBridgeEvents.UpdateClient -> {
                _playerState.update { event.playerState }
            }
            is ClientBridgeEvents.UpdateGameState -> {
                _gameState.update { event.gameState }
            }
        }
    }


    fun checkEnabled(): Boolean{
        return activeCallValue() == 0 && _playerState.value.isPlayingNow
    }
    fun activeCallValue(): Int{
        if (!_playerState.value.isPlayingNow || _gameState.value.activeRaise == null || _gameState.value.round == 0){
            return 0
        }
        _gameState.value.activeRaise?.let { (playerId, amount) ->
            if (playerId != _playerState.value.id){
                return amount - _playerState.value.called
            }
        }
        return 0
    }
    fun minimalRaise(): Int{
        return activeCallValue() + _gameState.value.smallBlindAmount
//        _gameState.value.activeRaise?.let { (playerId, amount) ->
//            if (playerId == _playerState.value.id){     // Should happen only for BigBlind in the round = 1
//                return _gameState.value.smallBlindAmount
//            }
//            return amount - _playerState.value.called + _gameState.value.smallBlindAmount
//        }
//        return _gameState.value.smallBlindAmount
    }
}