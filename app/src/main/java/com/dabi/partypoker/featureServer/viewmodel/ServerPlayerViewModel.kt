package com.dabi.partypoker.featureServer.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.dabi.easylocalgame.payloadUtils.toClientPayload
import com.dabi.easylocalgame.serverSide.ClientAction
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.interfaces.PlayerCoreInterface
import com.dabi.partypoker.featurePlayer.model.data.PlayerState
import com.dabi.partypoker.featurePlayer.viewmodel.MyClientPayloadType
import com.dabi.partypoker.featurePlayer.viewmodel.PlayerEvents
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.SeatPosition
import com.dabi.partypoker.repository.gameSettings.GameSettingsDatabase
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel(assistedFactory = ServerPlayerViewModel.ServerPlayerViewModelFactory::class)
class ServerPlayerViewModel @AssistedInject constructor(
    private val connectionsClient: ConnectionsClient,
    private val db: GameSettingsDatabase,
    @Assisted private val gameSettingsId: Long
): ServerOwnerViewModel(connectionsClient, db, gameSettingsId), PlayerCoreInterface {
    private val _playerState: MutableStateFlow<PlayerState> = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    @AssistedFactory
    interface ServerPlayerViewModelFactory {
        fun create(gameSettingsId: Long): ServerPlayerViewModel
    }

    private val _playerActionsState: MutableStateFlow<PlayerActionsState> = MutableStateFlow(
        PlayerActionsState()
    )
    val playerActionsState = _playerActionsState.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        Log.e("", "SERVER PLAYER KILLED")
    }

    init {
        viewModelScope.launch {
            val gameSettings = db.dao.getSettingById(gameSettingsId)
            gameSettings.firstOrNull()?.let { settings ->
                _gameState.update {
                    it.copy(
                        gameSettings = settings,
                    )
                }
            }
            val player = PlayerState(
                nickname = "ServerPlayer",
                id = "ServerPlayer",
                isServer = true,
                money = _gameState.value.gameSettings.playerMoney,
            )
            Log.e("", "PLAYER: " + player)
            _playerState.update { player }
            _gameState.update { it.copy(
                players = it.players + ("ServerPlayer" to player),
                seatPositions = it.seatPositions + (player.id to SeatPosition(0))
            ) }

            _gameState.collect{ gs ->
                val playerUpdate = gs.players[_playerState.value.id]!!
                _playerState.update { playerUpdate.copy() }

                _playerActionsState.update { it.copy(
                    canCheck = checkEnabled(),
                    callAmount = activeCallValue(),
                    raiseAmount = minimalRaise(),
                    canFold = _playerState.value.isPlayingNow
                ) }
            }
        }
    }

    fun initPlayer(
        nickname: String,
        avatarId: Int,
    ){
        _playerState.update { it.copy(
            nickname = nickname,
            avatarId = avatarId,
        ) }

        _gameState.update { it.copy(
            players = it.players + ("ServerPlayer" to _playerState.value),
        ) }
    }

    override fun onPlayerEvent(event: PlayerEvents) {
        when(event){
            PlayerEvents.Leave -> {
                serverManager.closeServer()
            }
            PlayerEvents.ChangeView -> {
                // Not for server
            }

            PlayerEvents.Ready -> {
                clientAction(ClientAction.PayloadAction(_playerState.value.id, toClientPayload(MyClientPayloadType.ACTION_READY.toString(), null)))
            }

            PlayerEvents.Check -> {
                clientAction(ClientAction.PayloadAction(_playerState.value.id, toClientPayload(MyClientPayloadType.ACTION_CHECK.toString(), null)))
            }
            is PlayerEvents.Call -> {
                clientAction(ClientAction.PayloadAction(_playerState.value.id, toClientPayload(MyClientPayloadType.ACTION_CALL.toString(), event.amount)))
            }
            is PlayerEvents.Raise -> {
                clientAction(ClientAction.PayloadAction(_playerState.value.id, toClientPayload(MyClientPayloadType.ACTION_RAISE.toString(), event.amount)))
            }
            PlayerEvents.Fold -> {
                clientAction(ClientAction.PayloadAction(_playerState.value.id, toClientPayload(MyClientPayloadType.ACTION_FOLD.toString(), null)))
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