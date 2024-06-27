package com.dabi.partypoker.featureServer.viewmodel

import android.util.Log
import androidx.annotation.IntRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.partypoker.R
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.GameManager
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureServer.model.ServerBridge
import com.dabi.partypoker.featureServer.model.ServerBridgeEvents
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.MessageData
import com.dabi.partypoker.featureServer.model.data.SeatPosition
import com.dabi.partypoker.featureServer.model.data.ServerState
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.utils.ClientPayloadType
import com.dabi.partypoker.utils.ServerPayloadType
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.toServerPayload
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
open class ServerOwnerViewModel@Inject constructor(
    connectionsClient: ConnectionsClient
): ViewModel() {
    protected val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val _playerMoveTimer = MutableStateFlow(_gameState.value.playerTimerDuration)
    val playerMoveTimer = _playerMoveTimer.asStateFlow()
    private var timerJob: Job? = null

    val serverBridge = ServerBridge(connectionsClient, this::onServerBridgeEvent)

    override fun onCleared() {
        super.onCleared()

        serverBridge.killServer()
        timerJob?.cancel()
    }
    init {
        viewModelScope.launch {
            _gameState.collect { gameState ->
                if (gameState.playingNow != null && gameState.playingNow !in gameState.players.keys){
                    autoFold()
                    return@collect
                }

                gameState.players.forEach { (index, player) ->
                    Log.e("ServerOwnerViewModel", "SENDING TO Player: $player")
                    val serverPayloadType = toServerPayload(ServerPayloadType.UPDATE_CLIENT, player)
                    serverBridge.sendPayload(index, serverPayloadType)
                }


                val serverPayload = toServerPayload(ServerPayloadType.UPDATE_GAME_STATE, gameState)
                serverBridge.sendPayload(serverPayload)
            }
        }
    }

    fun onGameEvent(event: GameEvents) {
        when (event){
            GameEvents.StartGame -> {
                _gameState.update { GameManager.startGame(_gameState.value).copy() }
                if (!_gameState.value.started) {
                    Log.e("ServerOwnerViewModel", "Game not started, few players")
                    return
                }
                handlePlayingPlayer()
            }

            GameEvents.CloseGame -> {
                serverBridge.leave()
            }
        }
    }

    private fun handlePlayingPlayer(){
        _playerMoveTimer.update { _gameState.value.playerTimerDuration }

        timerJob?.cancel()
        timerJob = this.viewModelScope.launch {
            while (_playerMoveTimer.value > 0){
                if (!_gameState.value.started) {
                    timerJob?.cancel()
                    this.cancel()
                }
                _playerMoveTimer.value --
                delay(1000)

                Log.e("ServerOwnerViewModel", "Timer: " + _playerMoveTimer.value)
            }
            Log.e("ServerOwnerViewModel", "KONEC TIMER")
            timeOutAutoMove()
            return@launch
        }
    }

    private fun timeOutAutoMove(){
        _gameState.value.activeRaise?.let { (raiserId, _) ->
            if (raiserId != _gameState.value.playingNow){
                return autoFold()
            }
        }
        return autoCheck()
    }

    private fun autoFold(){
        Log.e("", "AUTO FOLD: " + _gameState.value.playingNow)
        val player = _gameState.value.players[_gameState.value.playingNow]
        player?.let { p ->
            var tempGameState = _gameState.value.copy()
            tempGameState.players.forEach { (_, player) ->
                if (player.id == _gameState.value.playingNow) {
                    player.isFolded = true
                }
            }

            tempGameState = GameManager.movePlayedCheckRound(tempGameState)
            _gameState.update { tempGameState.copy() }
            handlePlayingPlayer()
        } ?: run {
            _gameState.update { GameManager.movePlayedCheckRound(it).copy() }
            handlePlayingPlayer()
        }
    }
    private fun autoCheck(){
        Log.e("", "AUTO CHECK: " + _gameState.value.playingNow)
        _gameState.update { GameManager.movePlayedCheckRound(_gameState.value).copy() }
        handlePlayingPlayer()
    }

    protected fun onServerBridgeEvent(event: ServerBridgeEvents){
        when(event){
            is ServerBridgeEvents.ClientDisconnected -> {
                val clientID = event.endpointID

                _gameState.update { gameState ->
                    gameState.copy(
                        players = gameState.players.filter { it.key != clientID },
                        seatPositions = gameState.seatPositions.filter { it.key != clientID },

                        messages = gameState.messages.plus(
                            MessageData(
                                messages = listOf(UiTexts.StringResource(R.string.client_disconnected))
                            )
                        )
                    )
                }

                val foldedCount = _gameState.value.gameReadyPlayers.count { (playerId, _) ->
                    val player = _gameState.value.players[playerId]
                    player?.isFolded ?: true
                }

                if ((foldedCount + 1) >= _gameState.value.gameReadyPlayers.size){
                    _gameState.update {
                        GameManager.gameOver(it).copy()
                    }
                }
            }

            is ServerBridgeEvents.ClientAction -> {
                val clientID = event.endpointID
                val action = event.action
                val data = event.data

                when(action){
                    ClientPayloadType.CONNECTED -> {
                        val player = PlayerState(
                            nickname = data.toString(),
                            id = clientID,
                            money = 1000
                        )

                        if (_gameState.value.players.size >= 10){
                            Log.e("", "Too many players")
                            // TODO: Remove player from game
                            return
                        }
                        
                        val position: Int? = _gameState.value.getAvailableRandomPosition()

                        position?.let {
                            _gameState.update { gameState ->
                                gameState.copy(
                                    players = gameState.players + (clientID to player),
                                    seatPositions = gameState.seatPositions + (clientID to SeatPosition(position)),
                                    messages = gameState.messages.plus(
                                        MessageData(
                                            messages = listOf(UiTexts.StringResource(R.string.client_connected, player.nickname))
                                        )
                                    )
                                )
                            }
                        } ?: run {
                            Log.e("", "PROBLEM WITH GETTING PLAYER'S SEAT POSITION")
                            // TODO: Remove player from game
                        }
                    }
                    ClientPayloadType.ACTION_DISCONNECTED -> {
                        serverBridge.onServerEvent(ServerEvents.ClientDisconnected(clientID))
                    }

                    ClientPayloadType.ACTION_READY -> {
                        val player = _gameState.value.players[clientID]
                        player?.let {
                            val updatedPlayer = it.copy(isReadyToPlay = !it.isReadyToPlay)

                            val updatedPlayers = _gameState.value.players.toMutableMap().apply {
                                this[clientID] = updatedPlayer
                            }
                            _gameState.update { gameState ->
                                gameState.copy(
                                    players = updatedPlayers
                                )
                            }
//                            _gameState.value = _gameState.value.copy(players = updatedPlayers)
                        }
                    }

                    ClientPayloadType.ACTION_CHECK -> {
                        if (_gameState.value.playingNow != clientID){ return }

                        if (_playerMoveTimer.value > 0){
                            timerJob?.cancel()

                            _gameState.update { GameManager.movePlayedCheckRound(_gameState.value).copy() }
                            handlePlayingPlayer()
                        }
                    }
                    ClientPayloadType.ACTION_CALL -> {
                        if (_gameState.value.playingNow != clientID){ return }

                        if (_playerMoveTimer.value > 0){
                            timerJob?.cancel()

                            val player = _gameState.value.players[clientID]
                            player?.let { p ->
                                _gameState.value.activeRaise?.let {
                                    val amount = it.second.minus(p.called)
                                    if (p.called > it.second || amount > p.money){ return } // Should never happen

                                    var tempGameState = _gameState.value.copy()

                                    tempGameState.players.forEach { (_, player) ->
                                        if (player.id == _gameState.value.playingNow){
                                            player.money -= amount
                                            player.called += amount
                                        }
                                    }
                                    tempGameState.bank += amount
                                    tempGameState = GameManager.movePlayedCheckRound(tempGameState)

                                    _gameState.update { tempGameState.copy() }
                                    handlePlayingPlayer()
                                }
                            }
                        }
                    }
                    ClientPayloadType.ACTION_RAISE -> {
                        if (_gameState.value.playingNow != clientID){ return }

                        if (_playerMoveTimer.value > 0){
                            timerJob?.cancel()

                            val amount = Gson().fromJson(data.toString(), Int::class.java)
                            val player = _gameState.value.players[clientID]
                            player?.let { p ->
                                var tempGameState = _gameState.value.copy()

                                tempGameState.activeRaise?.let { (raiserId, activeRaisedAmount) ->
                                    val callAmount = activeRaisedAmount.minus(p.called) // Amount which player had to call if chose CALL option
                                    val raisedAmount = amount - callAmount
                                    if (amount < callAmount || amount > p.money){ return }

                                    tempGameState.players.forEach { (_, player) ->
                                        if (player.id == _gameState.value.playingNow){
                                            player.money -= (callAmount + raisedAmount)
                                            player.called += (callAmount + raisedAmount)
                                        }
                                    }
                                    tempGameState.bank += (callAmount + raisedAmount)
                                    tempGameState.activeRaise = Pair(clientID, (activeRaisedAmount + raisedAmount))
                                }?: run {
                                    tempGameState.players.forEach { (_, player) ->
                                        if (player.id == _gameState.value.playingNow){
                                            player.money -= amount
                                            player.called += amount
                                        }
                                    }
                                    tempGameState.bank += amount
                                    tempGameState.activeRaise = Pair(clientID, amount)
                                }

                                tempGameState = GameManager.movePlayedCheckRound(tempGameState)
                                _gameState.update { tempGameState.copy() }
                                handlePlayingPlayer()
                            }
                        }
                    }
                    ClientPayloadType.ACTION_FOLD -> {
                        if (_gameState.value.playingNow != clientID){ return }

                        if (_playerMoveTimer.value > 0) {
                            timerJob?.cancel()

                            val player = _gameState.value.players[clientID]
                            player?.let { p ->
                                var tempGameState = _gameState.value.copy()
                                tempGameState.players.forEach { (_, player) ->
                                    if (player.id == _gameState.value.playingNow) {
                                        player.isFolded = true
                                    }
                                }

                                tempGameState = GameManager.movePlayedCheckRound(tempGameState)
                                _gameState.update { tempGameState.copy() }
                                handlePlayingPlayer()
                            }
                        }
                    }
                }
            }
        }
    }
}