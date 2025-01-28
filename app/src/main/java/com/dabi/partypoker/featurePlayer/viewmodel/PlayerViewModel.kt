package com.dabi.partypoker.featurePlayer.viewmodel

import androidx.annotation.Keep
import com.dabi.easylocalgame.clientSide.PlayerViewmodelTemplate
import com.dabi.easylocalgame.clientSide.ServerAction
import com.dabi.easylocalgame.payloadUtils.data.ServerPayloadType
import com.dabi.easylocalgame.payloadUtils.fromServerPayload
import com.dabi.easylocalgame.payloadUtils.toClientPayload
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.interfaces.PlayerCoreInterface
import com.dabi.partypoker.featurePlayer.model.data.PlayerState
import com.dabi.partypoker.featureServer.model.data.GameState
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

@Keep
enum class MyClientPayloadType{
    ACTION_READY,

    ACTION_CHECK,
    ACTION_CALL,
    ACTION_RAISE,
    ACTION_FOLD
}


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val connectionsClient: ConnectionsClient
): PlayerViewmodelTemplate(connectionsClient), PlayerCoreInterface {
    private val _playerState: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val _viewChangeState = MutableStateFlow(false)
    val viewChangeState = _viewChangeState.asStateFlow()

    private val _playerActionsState: MutableStateFlow<PlayerActionsState> = MutableStateFlow(PlayerActionsState())
    val playerActionsState = _playerActionsState.asStateFlow()

    override fun onCleared() {
        super.onCleared()

        clientManager.disconnect()
    }

    override fun serverAction(serverAction: ServerAction) {
        when(serverAction){
            is ServerAction.UpdateGameState -> {
                val result: Pair<ServerPayloadType, GameState?> = fromServerPayload(serverAction.payload, null)

                result.second?.let { gs ->
                    _gameState.update { gs.copy() }
                }
            }
            is ServerAction.UpdatePlayerState -> {
                val result: Pair<ServerPayloadType, PlayerState?> = fromServerPayload(serverAction.payload, null)

                result.second?.let { ps ->
                    _playerState.update { ps.copy() }
                }
            }
            is ServerAction.PayloadAction -> {
                // TODO
            }
        }

        _playerActionsState.update { it.copy(
            canCheck = checkEnabled(),
            callAmount = activeCallValue(),
            raiseAmount = minimalRaise(),
            canFold = _playerState.value.isPlayingNow
        ) }
    }

    override fun getPlayerState(): PlayerState {
        return _playerState.value
    }

    override fun getGameState(): GameState {
        return _gameState.value
    }

    override fun onPlayerEvent(event: PlayerEvents){
        when(event){
            PlayerEvents.Ready -> {
                val clientPayload = toClientPayload(MyClientPayloadType.ACTION_READY.toString(), null as String?)
                clientManager.sendPayload(clientPayload)
            }
            PlayerEvents.ChangeView ->{
                _viewChangeState.update { !it }
            }
            PlayerEvents.Leave -> {
                clientManager.disconnect()
            }

            PlayerEvents.Check -> {
                val clientPayload = toClientPayload(MyClientPayloadType.ACTION_CHECK.toString(), null as String?)
                clientManager.sendPayload(clientPayload)
            }
            PlayerEvents.Fold -> {
                val clientPayload = toClientPayload(MyClientPayloadType.ACTION_FOLD.toString(), null as String?)
                clientManager.sendPayload(clientPayload)
            }
            is PlayerEvents.Call -> {
                val clientPayload = toClientPayload(MyClientPayloadType.ACTION_CALL.toString(), event.amount)
                clientManager.sendPayload(clientPayload)
            }
            is PlayerEvents.Raise -> {
                val clientPayload = toClientPayload(MyClientPayloadType.ACTION_RAISE.toString(), event.amount)
                clientManager.sendPayload(clientPayload)
            }
        }
    }
}