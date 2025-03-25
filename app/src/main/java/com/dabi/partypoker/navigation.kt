package com.dabi.partypoker

import androidx.activity.compose.BackHandler
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dabi.partypoker.featureMenu.view.MenuView
import com.dabi.partypoker.featurePlayer.view.PlayerView
import com.dabi.partypoker.featureServer.view.ServerView
import kotlinx.serialization.Serializable


@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MenuScreen) {
        composable<MenuScreen> {
            MenuView(navController)
        }

        composable<PlayerScreen> {
            BackHandler(true) {  }

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
