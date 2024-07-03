package com.dabi.partypoker.featureClient.viewmodel

import androidx.lifecycle.ViewModel
import com.dabi.partypoker.featureClient.model.ClientBridge
import com.dabi.partypoker.featureClient.model.ClientBridgeEvents
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.interfaces.PlayerCoreInterface
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
    data object Leave: PlayerEvents()
    data object ChangeView: PlayerEvents()

    data object Check: PlayerEvents()
    data class Call(val amount: Int): PlayerEvents()
    data class Raise(val amount: Int): PlayerEvents()
    data object Fold: PlayerEvents()
}


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
): ViewModel(), PlayerCoreInterface {
    private val _playerState: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _viewChangeState = MutableStateFlow(false)
    val viewChangeState = _viewChangeState.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val _playerActionsState: MutableStateFlow<PlayerActionsState> = MutableStateFlow(PlayerActionsState())
    val playerActionsState = _playerActionsState.asStateFlow()

    val clientBridge: ClientBridge = ClientBridge(connectionsClient, this::onClientBridgeEvent)

    override fun onCleared() {
        super.onCleared()

        clientBridge.killClient()
    }

    override fun onPlayerEvent(event: PlayerEvents){
        when(event){
            PlayerEvents.Leave -> {
                clientBridge.leave()
            }
            PlayerEvents.ChangeView -> {
                _viewChangeState.update { !it }
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

    override fun getPlayerState(): PlayerState {
        return _playerState.value
    }

    override fun getGameState(): GameState {
        return _gameState.value
    }


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
                connectionsClient.stopDiscovery()
            }

            is ClientBridgeEvents.UpdateClient -> {
                _playerState.update { event.playerState }
            }
            is ClientBridgeEvents.UpdateGameState -> {
                _gameState.update { event.gameState }
            }
        }

        _playerActionsState.update { it.copy(
            canCheck = checkEnabled(),
            callAmount = activeCallValue(),
            raiseAmount = minimalRaise(),
            canFold = _playerState.value.isPlayingNow
        ) }
    }
}