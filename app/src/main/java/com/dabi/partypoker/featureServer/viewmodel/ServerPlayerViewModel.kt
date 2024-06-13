package com.dabi.partypoker.featureServer.viewmodel

import androidx.lifecycle.viewModelScope
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.interfaces.PlayerCoreInterface
import com.dabi.partypoker.featureServer.model.ServerBridgeEvents
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.utils.ClientPayloadType
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ServerPlayerViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
): ServerOwnerViewModel(connectionsClient), PlayerCoreInterface{

    private val _playerState: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _playerActionsState: MutableStateFlow<PlayerActionsState> = MutableStateFlow(
        PlayerActionsState()
    )
    val playerActionsState = _playerActionsState.asStateFlow()

    init {
        val player = PlayerState(
            nickname = "ServerPlayer",
            id = "ServerPlayer",
            isServer = true,
            money = 1000,
            isReadyToPlay = true
        )
        _gameState.update { it.copy(
            players = it.players + ("ServerPlayer" to player),
        ) }
        _playerState.update { player }

        viewModelScope.launch {
            _gameState.collect{ gs ->
                val playerUpdate = gs.players[_playerState.value.id]!!
                _playerState.update { playerUpdate }

                _playerActionsState.update { it.copy(
                    canCheck = checkEnabled(),
                    callAmount = activeCallValue(),
                    raiseAmount = minimalRaise(),
                    canFold = _playerState.value.isPlayingNow
                ) }
            }
        }
    }

    override fun onPlayerEvent(event: PlayerEvents){
        when(event){
            PlayerEvents.Disconnect -> TODO()

            PlayerEvents.Ready -> {
                onServerBridgeEvent(ServerBridgeEvents.ClientAction(_playerState.value.id, ClientPayloadType.ACTION_READY, null))
            }

            PlayerEvents.Check -> {
                onServerBridgeEvent(ServerBridgeEvents.ClientAction(_playerState.value.id, ClientPayloadType.ACTION_CHECK, null))
            }
            is PlayerEvents.Call -> {
                onServerBridgeEvent(ServerBridgeEvents.ClientAction(_playerState.value.id, ClientPayloadType.ACTION_CALL, event.amount))
            }
            is PlayerEvents.Raise -> {
                onServerBridgeEvent(ServerBridgeEvents.ClientAction(_playerState.value.id, ClientPayloadType.ACTION_RAISE, event.amount))
            }
            PlayerEvents.Fold -> {
                onServerBridgeEvent(ServerBridgeEvents.ClientAction(_playerState.value.id, ClientPayloadType.ACTION_FOLD, null))
            }
        }
    }

    override fun getPlayerState(): PlayerState {
        return _playerState.value
    }

    override fun getGameState(): GameState {
        return _gameState.value
    }
}