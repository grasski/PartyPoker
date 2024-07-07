package com.dabi.partypoker

import android.util.Log
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
import com.google.android.gms.nearby.connection.ConnectionsClient
import kotlinx.serialization.Serializable
import javax.inject.Inject


@Composable
fun Navigation() {
    val navController = rememberNavController()

    lateinit var connectionsClient: ConnectionsClient

    NavHost(navController = navController, startDestination = MenuScreen) {

        composable<MenuScreen> {
            MenuView(navController = navController)
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
    @RawRes val avatarId: Int
)
