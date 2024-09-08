package com.dabi.partypoker

import androidx.activity.compose.BackHandler
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dabi.partypoker.featureClient.view.PlayerView
import com.dabi.partypoker.featureMenu.view.MenuView
import com.dabi.partypoker.featureServer.view.ServerView
import kotlinx.serialization.Serializable


@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MenuScreen) {
        composable<MenuScreen> {
            MenuView(navController = navController)
        }

        composable<PlayerScreen> {
            BackHandler(true) {  }

//            PlayerGameView(
//                playerState = PlayerState(
//                    nickname = "nickname",
//                    id = "1",
//                    isPlayingNow = true,
//                    money = 1000,
//                    isReadyToPlay = true,
//                    holeCards = listOf(
//                        Card(CardType.CLUB, 14),
//                        Card(CardType.HEART, 14)
//                    ),
//                    allIn = false, called = 58
//                ),
//                playerActionsState = PlayerActionsState(canCheck = false, callAmount = 5, raiseAmount = 50),
//                onPlayerEvent = {},
//                gameState = GameState(
//                    players = mapOf(
//                        "1" to PlayerState("1", allIn = false, called = 58),
//                        "2" to PlayerState("2", called = 4),
//                        "3" to PlayerState("3", isSmallBlind = true, called = 156),
//                        "4" to PlayerState("4", allIn = true),
//                        "5" to PlayerState("5", allIn = true, called = 4516),
//                        "6" to PlayerState("6", allIn = true, called = 156, isDealer = true),
//                        "7" to PlayerState("7", allIn = true, isDealer = true, called = 185),
//                        "8" to PlayerState("8", allIn = true, isBigBlind = true),
//                        "9" to PlayerState("9", allIn = true, isDealer = true, isSmallBlind = true, called = 853)
//                    ),
//                    seatPositions = mapOf(
//                        "1" to SeatPosition(0),
//                        "2" to SeatPosition(2),
//                        "3" to SeatPosition(5),
//                        "4" to SeatPosition(8),
//                        "5" to SeatPosition(7),
//                        "6" to SeatPosition(3),
//                        "7" to SeatPosition(1),
//                        "8" to SeatPosition(6),
//                        "9" to SeatPosition(4)
//                    )
//                )
//            )

//            PlayerViewPrivate(
//                playerState = PlayerState(
//                    nickname = "nickname",
//                    id = "1",
//                    isPlayingNow = true,
//                    money = 1000,
//                    isReadyToPlay = true,
//                    holeCards = listOf(
//                        Card(CardType.CLUB, 14),
//                        Card(CardType.HEART, 14)
//                    )
//                ),
//                gameState = GameState(),
//                onPlayerEvent = {},
//                playerActionsState = PlayerActionsState(canCheck = false, callAmount = 5, raiseAmount = 50)
//            )

            PlayerView(
                navController,
                it.toRoute<PlayerScreen>().nickname,
                it.toRoute<PlayerScreen>().avatarId,
            )
        }

        composable<ServerScreen> {
            BackHandler(true) {  }

            val serverScreen: ServerScreen = it.toRoute()
            ServerView(
                navController = navController,
                serverScreen = serverScreen
            )
        }
    }
}

@Serializable
object MenuScreen

@Serializable
data class PlayerScreen(
    val nickname: String,
    @RawRes val avatarId: Int
)

@Serializable
data class ServerScreen(
    val serverType: String,
    val serverName: String,
    @RawRes val avatarId: Int,
    val serverGameSettingsId: Long
)
