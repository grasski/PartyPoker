package com.dabi.partypoker.featureServer.viewmodel

import android.util.Log
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
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.repository.gameSettings.GameSettingsDatabase
import com.dabi.partypoker.utils.ClientPayloadType
import com.dabi.partypoker.utils.ServerPayloadType
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.UiTextsAdapter
import com.dabi.partypoker.utils.toServerPayload
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel(assistedFactory = ServerOwnerViewModel.ServerOwnerViewModelFactory::class)
open class ServerOwnerViewModel @AssistedInject constructor(
    private val connectionsClient: ConnectionsClient,
    private val db: GameSettingsDatabase,
    @Assisted private val gameSettingsId: Long
) : ViewModel() {
    protected val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val _playerMoveTimerMillis = MutableStateFlow(_gameState.value.gameSettings.playerTimerDurationMillis)
    val playerMoveTimerMillis = _playerMoveTimerMillis.asStateFlow()
    private val _gameOverTimerMillis = MutableStateFlow(_gameState.value.gameSettings.gameOverTimerDurationMillis)
    val gameOverTimerMillis = _gameOverTimerMillis.asStateFlow()
    private var timerJob: Job? = null

    val serverBridge = ServerBridge(connectionsClient, this::onServerBridgeEvent)

    @AssistedFactory
    interface ServerOwnerViewModelFactory {
        fun create(gameSettingsId: Long): ServerOwnerViewModel
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("", "SERVER KILLED")
        serverBridge.killServer()
        timerJob?.cancel()
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
            _gameState.update {
                it.copy(
                    messages = it.messages.plus(
                        MessageData(
                            messages = listOf(UiTexts.StringResource(R.string.game_created))
                        )
                    )
                )
            }
            _gameOverTimerMillis.update { _gameState.value.gameSettings.gameOverTimerDurationMillis }
            _playerMoveTimerMillis.update { _gameState.value.gameSettings.playerTimerDurationMillis }

            _gameState.collect { gameState ->
                if ((gameState.playingNow != null && gameState.playingNow !in gameState.players.keys) || gameState.players[gameState.playingNow]?.isFolded == true){
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

                if (gameState.gameOver && _gameOverTimerMillis.value >= gameState.gameSettings.gameOverTimerDurationMillis){
                    timerJob?.cancel()
                    async {
                        _gameOverTimerMillis.update { _gameState.value.gameSettings.gameOverTimerDurationMillis }
                        while (_gameOverTimerMillis.value > 0){
                            Log.e("", "GAME OVER TIMER: " + _gameOverTimerMillis.value)
                            delay(1000)
                            _gameOverTimerMillis.value -= 1000
                        }
                        _gameState.update { GameManager.startGame(_gameState.value).copy() }
                        handlePlayingPlayer()
                        _gameOverTimerMillis.update { _gameState.value.gameSettings.gameOverTimerDurationMillis }
                    }
                }
            }
        }

        Log.e("", "GAME: " + _gameState.value)
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

            GameEvents.StopAdvertising -> {
                serverBridge.stopAdvertising()
            }
        }
    }

    private fun handlePlayingPlayer(){
        _playerMoveTimerMillis.update { _gameState.value.gameSettings.playerTimerDurationMillis }

        timerJob?.cancel()
        timerJob = this.viewModelScope.launch {
            while (_playerMoveTimerMillis.value > 0){
                if (!_gameState.value.started || _gameState.value.gameOver) {
                    timerJob?.cancel()
                    this.cancel()
                }
                delay(1030)
                _playerMoveTimerMillis.value -= 1000
                Log.e("", "TIMER: " + _playerMoveTimerMillis.value)
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
                    val nick = gameState.players[clientID]?.nickname
                    gameState.copy(
                        bank = gameState.bank + (gameState.players[clientID]?.called ?: 0),
                        players = gameState.players.filter { it.key != clientID },
                        seatPositions = gameState.seatPositions.filter { it.key != clientID },

                        messages = gameState.messages.plus(
                            MessageData(
                                messages = listOf(UiTexts.StringResource(R.string.client_disconnected, nick ?: ""))
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
                    handlePlayingPlayer()
                }
            }

            is ServerBridgeEvents.ClientAction -> {
                val clientID = event.endpointID
                val action = event.action
                val data = event.data

                when(action){
                    ClientPayloadType.CONNECTED -> {
                        val gson = GsonBuilder()
                            .registerTypeAdapter(UiTexts::class.java, UiTextsAdapter())
                            .create()
                        val pairType = object : TypeToken<Pair<String, Int>>() {}.type
                        val pair: Pair<String, Int> = gson.fromJson(gson.toJson(data), pairType)

                        val player = PlayerState(
                            nickname = pair.first,
                            avatarId = pair.second,
                            id = clientID,
                            money = _gameState.value.gameSettings.playerMoney
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
                                    players = updatedPlayers,

                                    messages = gameState.messages.plus(
                                        MessageData(
                                            messages = listOf(
                                                if (updatedPlayer.isReadyToPlay){
                                                    UiTexts.StringResource(R.string.player_ready, player.nickname)
                                                } else{
                                                    UiTexts.StringResource(R.string.player_unready, player.nickname)
                                                }
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    }

                    ClientPayloadType.ACTION_CHECK -> {
                        if (_gameState.value.playingNow != clientID){ return }
                        timerJob?.cancel()

                        _gameState.update { GameManager.movePlayedCheckRound(_gameState.value).copy() }
                        handlePlayingPlayer()
                    }
                    ClientPayloadType.ACTION_CALL -> {
                        if (_gameState.value.playingNow != clientID){ return }

                        val player = _gameState.value.players[clientID]
                        player?.let { p ->
                            _gameState.value.activeRaise?.let {
                                val amount = it.second.minus(p.called)
                                if (p.called > it.second || amount > p.money){ return }
                                timerJob?.cancel()

                                var tempGameState = _gameState.value.copy()

                                tempGameState.players.forEach { (_, player) ->
                                    if (player.id == _gameState.value.playingNow){
                                        player.money -= amount
                                        player.called += amount
                                    }
                                }
//                                tempGameState.bank += amount
                                tempGameState = GameManager.movePlayedCheckRound(tempGameState)

                                _gameState.update { tempGameState.copy() }
                                handlePlayingPlayer()
                            }
                        } ?: run { timerJob?.cancel() }
                    }
                    ClientPayloadType.ACTION_RAISE -> {
                        if (_gameState.value.playingNow != clientID){ return }

                        val amount = Gson().fromJson(data.toString(), Int::class.java)
                        val player = _gameState.value.players[clientID]
                        player?.let { p ->
                            var tempGameState = _gameState.value.copy()

                            tempGameState.activeRaise?.let { (raiserId, activeRaisedAmount) ->
                                val callAmount = activeRaisedAmount.minus(p.called) // Amount which player had to call if chose CALL option
                                val raisedAmount = amount - callAmount
                                Log.e("", "AMOUNT: " + amount + " RAISED: " + raisedAmount + " CALL: " + callAmount)
                                if (amount < callAmount || amount > p.money){ return }
                                timerJob?.cancel()

                                tempGameState.players.forEach { (_, player) ->
                                    if (player.id == _gameState.value.playingNow){
                                        player.money -= (callAmount + raisedAmount)
                                        player.called += (callAmount + raisedAmount)
                                    }
                                }
//                                tempGameState.bank += (callAmount + raisedAmount)
                                tempGameState.activeRaise = Pair(clientID, (activeRaisedAmount + raisedAmount))
                            }?: run {
                                if (amount > p.money){ return }
                                timerJob?.cancel()

                                tempGameState.players.forEach { (_, player) ->
                                    if (player.id == _gameState.value.playingNow){
                                        player.money -= amount
                                        player.called += amount
                                    }
                                }
//                                tempGameState.bank += amount
                                tempGameState.activeRaise = Pair(clientID, amount)
                            }

                            tempGameState = GameManager.movePlayedCheckRound(tempGameState)
                            _gameState.update { tempGameState.copy() }
                            handlePlayingPlayer()
                        } ?: run { timerJob?.cancel() }
                    }
                    ClientPayloadType.ACTION_FOLD -> {
                        if (_gameState.value.playingNow != clientID){ return }
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