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


sealed class GameEvents{
    data object StartGame: GameEvents()
    data object CloseGame: GameEvents()
}

class GameManager {
    companion object{
        fun startGame(gameStateO: GameState, bank: Int = 0): GameState{
            Log.e("", "GAME START!!")
            var gameState = gameStateO.copy()

            val games = gameState.games
            gameState.players.forEach { (_, player) ->
                player.isFolded = false
                player.holeCards = emptyList()
                player.called = 0
                player.isPlayingNow = false
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
                    bank = gameState.bank
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
                    bank = gameState.bank
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
                bank = bank,
                playingNow = smallBlind,
                roundStartedId = smallBlind,
                round = 0,
                nextGameIn = 0,
                bigBlindRaised = false,

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
                        player.money -= gameState.smallBlindAmount
                        player.called = gameState.smallBlindAmount
                    }
                    if (player.isBigBlind){
                        player.money -= gameState.bigBlindAmount
                        player.called = gameState.bigBlindAmount
                    }
                }
            }
            gameState.bank += gameState.smallBlindAmount + gameState.bigBlindAmount

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

            gameState.activeRaise = Pair(gameState.bigBlindId!!, gameState.bigBlindAmount)

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
                    roundStartedId = playingFirstAfterDealerId
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
                Log.e("", "RAISER: " + raiserId + " AMOUNT: " + raiseAmount)
                if (gameState.round == 1){
                    Log.e("", "RAISER: " + raiserId + " AMOUNT: " + raiseAmount)
                    if (movePlayedBy == gameState.bigBlindId && raiseAmount == gameState.bigBlindAmount){
                        // BB checked in his move in round 1, so the round ends
                        return newRound()
//                    } else if (movePlayedBy == gameState.bigBlindId && raiserId != gameState.bigBlindId){
//                        return newRound()
                    } else if (movePlayedBy == gameState.bigBlindId && raiserId == playingNextId){
                        return newRound()
                    } else if (raiserId == playingNextId && gameState.bigBlindRaised){
                        return newRound()
                    } else {
                        if (movePlayedBy == gameState.bigBlindId){ gameState.bigBlindRaised = true }
                        // Someone else played, or BB called someone's raise, or BB raised -> round continues
                        gameState.playingNow = playingNextId
                    }
                } else{
                    if (raiserId == playingNextId){
                        // If the next player is the one who raised in current round, we makes new round as you can not re-raise yourself in one round
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

//                if (playerId == gameState.activeRaise?.first){
//                    return playerId
//                }

                gameState.players[playerId]?.let { player ->
                    if (!player.isFolded){
                        return playerId
                    }
                }
                i++
            }
        }


        private fun gameOver(gameState: GameState): GameState{
            Log.e("", "GAME OVER")

            val notFolderPlayersID = gameState.gameReadyPlayers.filter { (playerId, _) ->
                val player = gameState.players[playerId]
                player?.isFolded == false
            }.keys

            if (notFolderPlayersID.isEmpty()){
                Log.e("", "no one left - should not happen")
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

                return startGame(gameState, restBank)
            }

            return startGame(gameState)
        }
    }
}
