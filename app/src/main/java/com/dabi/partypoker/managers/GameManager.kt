package com.dabi.partypoker.managers

import android.util.Log
import androidx.compose.ui.util.fastJoinToString
import com.dabi.partypoker.R
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.MessageData
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.evaluateGame
import com.dabi.partypoker.utils.generateDeck
import com.dabi.partypoker.utils.getCards
import kotlinx.coroutines.Job


sealed class GameEvents{
    data object StartGame: GameEvents()
    data object CloseGame: GameEvents()
    data object StopAdvertising: GameEvents()
}

class GameManager {
    companion object{
        fun startGame(
            gameStateO: GameState,
//            bank: Int = 0
        ): GameState{
            Log.e("", "GAME START!!")
            var gameState = gameStateO.copy()

            val games = gameState.games
            gameState.players.forEach { (_, player) ->
                player.isFolded = false
                player.holeCards = emptyList()
                player.called = 0
                player.isPlayingNow = false
                player.isDealer = false
                player.isBigBlind = false
                player.isSmallBlind = false
            }

            val readyPlayers = gameState.players.filter { it.value.isReadyToPlay }
            gameState.gameReadyPlayers = emptyMap()
            readyPlayers.forEach { (id, _) ->
                val position = gameState.seatPositions[id]
                position?.let {
                    gameState.gameReadyPlayers += id to position
                }
            }
            if (gameState.gameReadyPlayers.size < 2){
                Log.e("", "TADY KONEC START")
                return GameState(
                    players = gameState.players,
                    seatPositions = gameState.seatPositions,
                    messages = gameState.messages,
                    bank = gameState.bank,
                    gameSettings = gameState.gameSettings
                )
            }
//            // To count correct dealer, smallBlind and bigBlind in new game rounds
            gameState.playingNow = gameState.gameReadyPlayers.toList().sortedBy { it.second.position }.toMap().keys.firstOrNull()
//            gameState.playingNow = gameState.gameReadyPlayers.firstOrNull()


            val dealerId = getPlayingNow(games, gameState)
            val smallBlind = getPlayingNow(games + 1, gameState)
            val bigBlind = getPlayingNow(games + 2, gameState)
            if (dealerId == null || smallBlind == null || bigBlind == null){
                Log.e("", "TADY KONEC START 2")
                return GameState(
                    players = gameState.players,
                    seatPositions = gameState.seatPositions,
                    messages = gameState.messages,
                    bank = gameState.bank,
                    gameSettings = gameState.gameSettings
                )
            }

            gameState.players.forEach { (_, player) ->
                player.isPlayingNow = player.id == smallBlind
                player.isBigBlind = player.id == bigBlind
                player.isSmallBlind = player.id == smallBlind
                player.isDealer = player.id == dealerId
            }

            gameState = gameState.copy(
                started = true,
                ongoing = true,
                dealerId = dealerId,
                smallBlindId = smallBlind,
                bigBlindId = bigBlind,
                cardsDeck = generateDeck().shuffled(),
                cardsTable = emptyList(),
                activeRaise = null,
//                bank = bank,
                playingNow = smallBlind,
                roundStartedId = smallBlind,
                round = 0,
                bigBlindRaised = false,

                gameSettings = gameState.gameSettings,
                gameOver = false,
                winningCards = emptySet(),

                games = games + 1
            )

            return simulateRoundZero(gameState)
        }


        private fun simulateRoundZero(gameStateO: GameState): GameState{
            val gameState = gameStateO.copy()

            gameState.gameReadyPlayers.forEach { (playerId, _) ->
                val player = gameState.players[playerId]
                player?.let {
                    if (player.isSmallBlind){
                        player.money -= gameState.gameSettings.smallBlindAmount
                        player.called = gameState.gameSettings.smallBlindAmount
                    }
                    if (player.isBigBlind){
                        player.money -= gameState.gameSettings.bigBlindAmount
                        player.called = gameState.gameSettings.bigBlindAmount
                    }
                }
            }
//            gameState.bank += gameState.smallBlindAmount + gameState.bigBlindAmount

            val playingNow = getPlayingNow(2, gameState)
            if (playingNow == null){
                return gameOver(gameState)
            }

            gameState.gameReadyPlayers.forEach { (playerId, _) ->
                val player = gameState.players[playerId]
                player?.let {
                    val holeCards = getCards(gameState.cardsDeck.toMutableList(),2)
                    player.holeCards = holeCards

                    gameState.cardsDeck = gameState.cardsDeck.toMutableList().apply { removeAll(holeCards) }
                }
            }

            gameState.round = 1
            gameState.playingNow = playingNow
            gameState.roundStartedId = playingNow

            gameState.activeRaise = Pair(gameState.bigBlindId!!, gameState.gameSettings.bigBlindAmount)

            gameState.players.forEach { (_, player) ->
                player.isPlayingNow = player.id == playingNow
            }

            return gameState
        }

        fun movePlayedCheckRound(gameStateO: GameState): GameState{
            var gameState = gameStateO.copy()
            val movePlayedBy = gameState.playingNow

            fun newRound(): GameState{
                val playingFirstAfterDealerId = getPlayingNow(1, gameState, true)
                if (playingFirstAfterDealerId == null){
                    return gameOver(gameState)
                }

                gameState = gameState.copy(
                    round = gameState.round + 1,
                    activeRaise = null,
                    playingNow = playingFirstAfterDealerId,
                    roundStartedId = playingFirstAfterDealerId,
                    bank = gameState.bank + gameState.players.values.sumOf { it.called }
                )
                gameState.players.forEach { (_, player) ->
                    player.called = 0
                    player.isPlayingNow = player.id == playingFirstAfterDealerId
                }

                if (gameState.round == 2){
                    val tableCards = getCards(gameState.cardsDeck.toMutableList(), 3)

                    gameState.cardsDeck = gameState.cardsDeck.toMutableList().apply { removeAll(tableCards) }
                    gameState.cardsTable += tableCards
                }
                if (gameState.round in 3..4){
                    val tableCards = getCards(gameState.cardsDeck.toMutableList(), 1)

                    gameState.cardsDeck = gameState.cardsDeck.toMutableList().apply { removeAll(tableCards) }
                    gameState.cardsTable += tableCards
                }
                if (gameState.round == 5){
                    return gameOver(gameState)
                }

                return gameState
            }


            val playingNextId = getPlayingNow(1, gameState)
            if (playingNextId == null){
                return gameOver(gameState)
            }

            gameState.activeRaise?.let { (raiserId, raiseAmount) ->
                if (gameState.round == 1){
                    if (gameState.bigBlindRaised){
                        if (raiserId == playingNextId){
                            return newRound()
                        }
                        gameState.playingNow = playingNextId
                    } else{
                        if (movePlayedBy == gameState.bigBlindId){
                            // Big blind CHECKED/FOLDED in round 1
                            if (raiseAmount == gameState.gameSettings.bigBlindAmount){
                                return newRound()
                            }

                            // Big blind RAISED in round 1
                            if (raiserId == gameState.bigBlindId){
                                // INFO: this can make "playingNow" player who folded and been "roundStartedId" (so basically first player after BigBlind)
                                // quick fixed in _gameState.collect (ServerOwnerViewModel) first condition '|| gameState.players[gameState.playingNow]?.isFolded == true'
                                // this will apply autoFold on the player to get new 'playingNow'

                                gameState.bigBlindRaised = true
                                gameState.playingNow = playingNextId
                            } else{
                                // Big blind CALLED in round 1
                                if (raiserId == playingNextId){
                                    return newRound()
                                }
                                gameState.playingNow = playingNextId
                            }
                        } else{
                            if (raiserId == playingNextId && movePlayedBy != gameState.smallBlindId){
                                return newRound()
                            }
                            gameState.playingNow = playingNextId
                        }
                    }
                } else{
                    if (raiserId == playingNextId){
                        return newRound()
                    }
                    gameState.playingNow = playingNextId
                }
            } ?: run {
                // No active raise

                Log.e("", "no active raise")
                if (playingNextId == gameState.roundStartedId){
                    Log.e("", "no raise - new round")
                    return newRound()
                } else{
                    Log.e("", "no raise - round continues")
                    gameState.playingNow = playingNextId
                }
            }

            gameState.players.forEach { (_, player) ->
                player.isPlayingNow = player.id == playingNextId
            }

            return gameState
        }


        private fun getPlayingNow(nextFromActualBy: Int = 1, gameState: GameState, afterDealer: Boolean = false): String? {
            var i = nextFromActualBy

            val foldedCount = gameState.gameReadyPlayers.count { (playerId, _) ->
                val player = gameState.players[playerId]
                player?.isFolded ?: true
            }

            if ((foldedCount + 1) >= gameState.gameReadyPlayers.size){
                return null
            }

            val sortedSeatsWithPlayers = gameState.gameReadyPlayers.toList().sortedBy { (_, value) -> value.position }.toMap()

            val currentPlayerIndex = sortedSeatsWithPlayers.toList().indexOfFirst { it.first == gameState.playingNow }
            val dealerId = sortedSeatsWithPlayers.toList().indexOfFirst { it.first == gameState.dealerId }
            while (true){
                val nextPlayerIndex = ((if (afterDealer) dealerId else currentPlayerIndex) + i) % sortedSeatsWithPlayers.size
                val playerId = sortedSeatsWithPlayers.keys.toList()[nextPlayerIndex]

                if (playerId == gameState.roundStartedId && !afterDealer){
                    return playerId
                }

                // If the player exists, check if he folded=(skip him) or not=(he will play next)
                gameState.players[playerId]?.let {
                    if (!it.isFolded){
                        return playerId
                    }
                } ?: run {
                    // Otherwise, he is next on the move, but not really playing, because he left the game
                    return playerId
                }
                i++
            }
        }


        fun gameOver(gameState: GameState): GameState{
            if (gameState.gameOver || !gameState.started){
                return gameState
            }
            Log.e("", "GAME OVER")

            val notFolderPlayersID = gameState.gameReadyPlayers.filter { (playerId, _) ->
                val player = gameState.players[playerId]
                player?.isFolded == false
            }.keys

            gameState.gameOver = true
            gameState.bank += gameState.players.values.sumOf { it.called }

            if (notFolderPlayersID.isEmpty()){
                Log.e("", "no one left - should not happen")
                return gameState
            } else if (notFolderPlayersID.size == 1){
                val winnerId = notFolderPlayersID.first()
                gameState.players.forEach{ (_, player) ->
                    if (player.id == winnerId){
                        player.money += gameState.bank

                        gameState.messages += MessageData(
                            messages = listOf(UiTexts.StringResource(R.string.winner_private, player.nickname, gameState.bank))
                        )
                    }
                }

                gameState.bank = 0
                return startGame(gameState)
            } else{
                val notFolderPlayers = gameState.players.filter {
                    it.key in notFolderPlayersID
                }

                val evaluation = evaluateGame(notFolderPlayers.values.toList(), gameState.cardsTable)
                val winnerPlayers = evaluation.first

                val playerEarning = gameState.bank / winnerPlayers.size
                val restBank = gameState.bank - (playerEarning * winnerPlayers.size)

                gameState.players.forEach{ (_, player) ->
                    if (player.id in winnerPlayers.map { it.id }){
                        player.money += playerEarning
                    }
                }

                gameState.messages += MessageData(
                    messages = listOf(
                        UiTexts.PluralResource(R.plurals.winner_public, winnerPlayers.size, winnerPlayers.joinToString { it.nickname }, playerEarning),
                        UiTexts.StringResource(CardsUtils.combinationsTranslationID[evaluation.second.first]!!)
                    ),
                    cards = evaluation.second.second.first()
                )
                gameState.bank = restBank
                gameState.playingNow = null
                gameState.winningCards = evaluation.second.second.flatten().toSet()

                gameState.players.forEach { (_, player) ->
                    player.isPlayingNow = false
                }
                return gameState
            }
        }
    }
}
