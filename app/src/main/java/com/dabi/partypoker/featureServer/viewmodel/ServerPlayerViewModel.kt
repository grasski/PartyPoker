package com.dabi.partypoker.featureServer.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureServer.model.ServerBridgeEvents
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.managers.GameManager
import com.dabi.partypoker.utils.ClientPayloadType
import com.dabi.partypoker.utils.toClientPayload
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ServerPlayerViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
): ServerOwnerViewModel(connectionsClient){

    private val _playerState: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        val player = PlayerState(
            nickname = "ServerPlayer",
            id = "ServerPlayer",
            money = 1000,
            isReadyToPlay = true
        )
        _gameState.update { it.copy(
            players = it.players + ("ServerPlayer" to player),
        ) }
        _playerState.update { player }

//        viewModelScope.launch {
//            _playerState.collect {
//                val updatedPlayers = _gameState.value.players.toMutableMap().apply {
//                    this[_playerState.value.id] = _playerState.value
//                }
//                _gameState.update {
//                    it.copy(
//                        players = updatedPlayers
//                    )
//                }
//            }
//        }
        viewModelScope.launch {
            _gameState.collect{ gs ->
                val playerUpdate = gs.players[_playerState.value.id]!!
                _playerState.update { playerUpdate }
            }
        }
    }

    fun onPlayerEvent(event: PlayerEvents){
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


    fun checkEnabled(): Boolean{
        return activeCallValue() == 0 && _playerState.value.isPlayingNow
    }
    fun activeCallValue(): Int{
        if (!_playerState.value.isPlayingNow || _gameState.value.activeRaise == null || _gameState.value.round == 0){
            return 0
        }
        _gameState.value.activeRaise?.let { (playerId, amount) ->
//            if (playerId != _playerState.value.id){
                return amount - _playerState.value.called
//            }
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