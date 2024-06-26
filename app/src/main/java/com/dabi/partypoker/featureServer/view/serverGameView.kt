package com.dabi.partypoker.featureServer.view

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureClient.view.PlayerGameView
import com.dabi.partypoker.featureCore.views.LoadingAnimation
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerPlayerViewModel
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerStatusEnum
import com.dabi.partypoker.managers.ServerType


@Composable
fun ServerView(
    navController: NavController,
    serverScreen: ServerScreen,
) {
    val serverViewModel = if (serverScreen.serverType == ServerType.IS_TABLE.toString()){
        hiltViewModel<ServerOwnerViewModel>()
    } else {
        hiltViewModel<ServerPlayerViewModel>()
    }
    val serverState by serverViewModel.serverBridge.serverState.collectAsStateWithLifecycle()


    val context = LocalContext.current
    LaunchedEffect(Unit) {
        serverViewModel.serverBridge.onServerEvent(
            ServerEvents.StartServer(context, ServerType.valueOf(serverScreen.serverType), serverScreen.serverName)
        )

        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    Crossfade(
        targetState = serverState.serverStatus,
        modifier = Modifier
            .fillMaxSize()
            .paint(
                rememberAsyncImagePainter(model = R.drawable.game_background),
                contentScale = ContentScale.FillWidth
            )
    ) { serverStatus ->
        when(serverStatus){
            ServerStatusEnum.NONE -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    LoadingAnimation(
                        modifier = Modifier
                            .fillMaxSize(0.4f),
                        text = stringResource(R.string.server_starting),
                        onCancelRequest = {
                            serverViewModel.onGameEvent(GameEvents.CloseGame)
                            navController.navigate(MenuScreen) { popUpTo(ServerScreen(serverScreen.serverType, serverScreen.serverName)) { inclusive = true } }
                        }
                    )
                }
            }
            ServerStatusEnum.ADVERTISING_FAILED -> {
                Text(text = "Error starting server. Maybe missing privileges etc.")
            }

            ServerStatusEnum.ADVERTISING, ServerStatusEnum.ACTIVE -> {
                val gameState by serverViewModel.gameState.collectAsStateWithLifecycle()

                when(ServerType.valueOf(serverScreen.serverType)){
                    ServerType.IS_PLAYER -> {
                        val serverPlayerVM = (serverViewModel as ServerPlayerViewModel)
                        val playerState by serverPlayerVM.playerState.collectAsStateWithLifecycle()
                        val playerActionsState by serverPlayerVM.playerActionsState.collectAsStateWithLifecycle()

                        PlayerGameView(
                            navController = navController,
                            gameState = gameState,
                            playerState = playerState,
                            playerActionsState = playerActionsState,
                            onPlayerEvent = serverPlayerVM::onPlayerEvent,
                            onGameEvent = serverViewModel::onGameEvent,
                        )
                    }
                    ServerType.IS_TABLE -> {
                        ServerGameView(
                            navController = navController,
                            gameState = gameState,
                            serverState = serverState,
                            serverScreen = serverScreen,
                            onGameEvent = serverViewModel::onGameEvent,
                            onServerEvent = serverViewModel.serverBridge::onServerEvent
                        )
                    }
                }
            }

            ServerStatusEnum.OFF -> {
                LaunchedEffect(key1 = serverStatus) {
                    navController.navigate(MenuScreen) {
                        popUpTo(ServerScreen(serverScreen.serverType, serverScreen.serverName)) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}
