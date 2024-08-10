package com.dabi.partypoker

import androidx.activity.compose.BackHandler
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.view.PlayerView
import com.dabi.partypoker.featureClient.view.PlayerViewPrivate
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureMenu.view.MenuView
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.view.ServerView
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardType
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

//            PlayerViewPrivate(
//                playerState = PlayerState(
//                    nickname = "nickname",
//                    id = "1",
//                    isPlayingNow = true,
//                    money = 1000,
//                    isReadyToPlay = false,
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
