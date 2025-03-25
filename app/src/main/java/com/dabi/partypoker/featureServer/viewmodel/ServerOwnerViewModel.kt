package com.dabi.partypoker.featureServer.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.dabi.easylocalgame.clientSide.data.PlayerConnectionState
import com.dabi.easylocalgame.payloadUtils.convertFromJsonToType
import com.dabi.easylocalgame.payloadUtils.data.ClientPayloadType
import com.dabi.easylocalgame.payloadUtils.data.ServerPayloadType
import com.dabi.easylocalgame.payloadUtils.fromPayload
import com.dabi.easylocalgame.payloadUtils.toServerPayload
import com.dabi.easylocalgame.serverSide.ClientAction
import com.dabi.easylocalgame.serverSide.ServerViewmodelTemplate
import com.dabi.easylocalgame.composeUtils.UiTexts
import com.dabi.partypoker.R
import com.dabi.partypoker.featurePlayer.model.data.PlayerState
import com.dabi.partypoker.featurePlayer.viewmodel.MyClientPayloadType
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.MessageData
import com.dabi.partypoker.featureServer.model.data.SeatPosition
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.GameManager
import com.dabi.partypoker.repository.gameSettings.GameSettingsDatabase
import com.google.android.gms.nearby.connection.ConnectionsClient
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@HiltViewModel(assistedFactory = ServerOwnerViewModel.ServerOwnerViewModelFactory::class)
open class ServerOwnerViewModel @AssistedInject constructor(
    private val connectionsClient: ConnectionsClient,
    private val db: GameSettingsDatabase,
    @Assisted private val gameSettingsId: Long
) : ServerViewmodelTemplate(connectionsClient) {
    protected val _gameState: MutableStateFlow<GameState> = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val _playerMoveTimerMillis = MutableStateFlow(_gameState.value.gameSettings.playerTimerDurationMillis)
    val playerMoveTimerMillis = _playerMoveTimerMillis.asStateFlow()
    private val _gameOverTimerMillis = MutableStateFlow(_gameState.value.gameSettings.gameOverTimerDurationMillis)
    val gameOverTimerMillis = _gameOverTimerMillis.asStateFlow()
    private var timerJob: Job? = null

    @AssistedFactory
    interface ServerOwnerViewModelFactory {
        fun create(gameSettingsId: Long): ServerOwnerViewModel
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("", "SERVER KILLED")
        serverManager.closeServer()
        timerJob?.cancel()
    }

    fun onGameEvent(event: GameEvents) {
        when(event) {
            is GameEvents.StartGame -> {
                _gameState.update { GameManager.startGame(_gameState.value).copy() }
                if (!_gameState.value.started) {
                    Log.e("ServerOwnerViewModel", "Game not started, few players")
                    return
                }
                _gameState.update {
                    it.copy(
                        messages = it.messages + MessageData(
                            messages = listOf(UiTexts.StringResource(R.string.game_start))
                        )
                    )
                }
                handlePlayingPlayer()
            }

            is GameEvents.CloseGame -> {
                serverManager.closeServer()
            }

            is GameEvents.StopAdvertising -> {
                serverManager.stopAdvertising()
            }
        }
    }

    init {
        viewModelScope.launch {
            ensureActive()
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
                ensureActive()

                if ((gameState.playingNow != null && gameState.playingNow !in gameState.players.keys) || gameState.players[gameState.playingNow]?.isFolded == true){
                    autoFold()
                    return@collect
                }

                gameState.players.forEach { (index, player) ->
                    Log.e("ServerOwnerViewModel", "SENDING TO Player: $player")
                    val serverPayloadType = toServerPayload(ServerPayloadType.UPDATE_PLAYER_STATE, player)
                    serverManager.sendPayload(index, serverPayloadType)
                }
                val serverPayload = toServerPayload(ServerPayloadType.UPDATE_GAME_STATE, gameState)
                serverManager.sendPayload(serverPayload)

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

                if (!gameState.gameOver && gameState.started){
                    if (gameState.completeAllIn){
                        Log.e("", "ALL IN")
                        timerJob?.cancel()

                        delay(1000)
                        _gameState.update { GameManager.movePlayedCheckRound(_gameState.value).copy() }
                        return@collect
                    } else{
                        val allInCount = gameState.gameReadyPlayers.count { (playerId, _) ->
                            val player = gameState.players[playerId]

                            if (player == null){
                                true
                            } else{
                                player.allIn || player.isFolded
                            }
                        }

                        // In case of 2 playing players, if one is playing now and the other left, the current one is winner
                        val playersStillReady = gameState.gameReadyPlayers.count { (playerId, _) ->
                            playerId in gameState.players.keys
                        }
                        if (playersStillReady == 1){
                            autoCheck()
                            return@collect
                        }

                        if (gameState.activeRaise == null && gameState.gameReadyPlayers.count() - allInCount <= 1){
                            // AUTOCOMPLETE of AllIn - Player's do not play anymore, so only show rest of the table cards and evaluate game
                            Log.e("", "ALL IN DRUHY")
                            timerJob?.cancel()

                            delay(1000)
                            _gameState.update { GameManager.movePlayedCheckRound(_gameState.value.copy(completeAllIn = true)).copy() }
                            return@collect
                        }
                        if (gameState.players[gameState.playingNow]?.allIn == true){
                            autoCheck()
                            return@collect
                        }
                    }
                }
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


    override fun clientAction(clientAction: ClientAction) {
        when(clientAction){
            is ClientAction.PayloadAction -> {
                val clientID = clientAction.endpointID

                val result = fromPayload<MyClientPayloadType, Any>(clientAction.payload, null)
                val clientPayloadType = result.first
                val data = result.second
                when(clientPayloadType){
                    MyClientPayloadType.ACTION_READY -> {
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
                    MyClientPayloadType.ACTION_CHECK -> {
                        if (_gameState.value.playingNow != clientID){ return }
                        timerJob?.cancel()

                        _gameState.update { GameManager.movePlayedCheckRound(_gameState.value).copy() }
                        handlePlayingPlayer()
                    }
                    MyClientPayloadType.ACTION_CALL -> {
                        if (_gameState.value.playingNow != clientID){ return }

                        val player = _gameState.value.players[clientID]
                        player?.let { p ->
                            _gameState.value.activeRaise?.let { (raiserId, raiseValue) ->
                                var amount = raiseValue.minus(p.called)
                                if (p.called > raiseValue){ return }

                                if (amount >= p.money){ // All-IN
                                    amount = p.money
                                }
                                timerJob?.cancel()

                                var tempGameState = _gameState.value.copy()
                                tempGameState.players.forEach { (_, player) ->
                                    if (player.id == _gameState.value.playingNow){
                                        player.money -= amount
                                        player.called += amount
                                        player.allIn = amount == p.money
                                    }
                                }
//                                tempGameState.bank += amount
                                tempGameState = GameManager.movePlayedCheckRound(tempGameState)

                                _gameState.update { tempGameState.copy() }
                                handlePlayingPlayer()
                            }
                        } ?: run { timerJob?.cancel() }
                    }
                    MyClientPayloadType.ACTION_RAISE -> {
                        if (_gameState.value.playingNow != clientID){ return }

                        var amount = data!!.convertFromJsonToType(Int::class.java)
                        val player = _gameState.value.players[clientID]
                        player?.let { p ->
                            var tempGameState = _gameState.value.copy()

                            tempGameState.activeRaise?.let { (raiserId, activeRaisedAmount) ->
                                val callAmount = activeRaisedAmount.minus(p.called) // Amount which player had to call if chose CALL option
                                val raisedAmount = amount - callAmount
                                Log.e("", "AMOUNT: " + amount + " RAISED: " + raisedAmount + " CALL: " + callAmount + " MONEY: " + p.money)
                                if (amount < callAmount){ return }
                                if (amount >= p.money){ // All-IN
                                    amount = p.money
                                }
                                timerJob?.cancel()

                                tempGameState.players.forEach { (_, player) ->
                                    if (player.id == _gameState.value.playingNow){
                                        player.money -= (callAmount + raisedAmount)
                                        player.called += (callAmount + raisedAmount)
                                        player.allIn = amount == p.money
                                    }
                                }
//                                tempGameState.bank += (callAmount + raisedAmount)
                                tempGameState.activeRaise = Pair(clientID, (activeRaisedAmount + raisedAmount))
                            }?: run {
                                if (amount >= p.money){ // All-IN
                                    amount = p.money
                                }
                                timerJob?.cancel()

                                tempGameState.players.forEach { (_, player) ->
                                    if (player.id == _gameState.value.playingNow){
                                        player.money -= amount
                                        player.called += amount
                                        player.allIn = amount == p.money
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
                    MyClientPayloadType.ACTION_FOLD -> {
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
            is ClientAction.EstablishConnection -> {
                val clientID = clientAction.endpointID

                val result: Pair<ClientPayloadType, PlayerConnectionState?> = fromPayload(clientAction.payload, null)
                result.second?.let {
                    val player = PlayerState(
                        nickname = it.nickname,
                        avatarId = it.avatarId,
                        id = clientID,
                        money = _gameState.value.gameSettings.playerMoney
                    )

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
                    }
                }
            }
            is ClientAction.Disconnect -> {
                val clientID = clientAction.endpointID

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
            }
        }
    }
}