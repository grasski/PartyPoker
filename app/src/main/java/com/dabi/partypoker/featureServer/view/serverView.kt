package com.dabi.partypoker.featureServer.view

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dabi.easylocalgame.serverSide.data.ServerConfiguration
import com.dabi.easylocalgame.serverSide.data.ServerStatusEnum
import com.dabi.easylocalgame.serverSide.data.ServerType
import com.dabi.easylocalgame.textUtils.UiTexts
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureCore.views.StateContent
import com.dabi.partypoker.featurePlayer.view.PlayerGameView
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerPlayerViewModel
import com.dabi.partypoker.managers.GameEvents
import kotlinx.coroutines.delay


@Composable
fun ServerView(
    navController: NavController,
    serverScreen: ServerScreen,
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val serverViewModel = if (serverScreen.serverType == ServerType.IS_TABLE.toString()){
//        hiltViewModel<ServerOwnerViewModel>()
        hiltViewModel(
            creationCallback = { factory : ServerOwnerViewModel.ServerOwnerViewModelFactory ->
                factory.create(gameSettingsId = serverScreen.serverGameSettingsId)
            }
        )
    } else {
//        hiltViewModel<ServerPlayerViewModel>()
        hiltViewModel(
            creationCallback = { factory : ServerPlayerViewModel.ServerPlayerViewModelFactory ->
                factory.create(gameSettingsId = serverScreen.serverGameSettingsId)
            }
        )
    }
    val serverState by serverViewModel.serverManager.serverState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var rotation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!rotation){
            Log.e("", "PRED: " + serverState.serverStatus)
            serverViewModel.serverManager.startServer(context.packageName, ServerConfiguration(ServerType.valueOf(serverScreen.serverType), 10))

            Log.e("", "OTOCKA")
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        rotation = true
    }


    val mappedState = when (serverState.serverStatus) {
        ServerStatusEnum.ADVERTISING, ServerStatusEnum.ACTIVE -> ServerStatusEnum.ACTIVE    // So the change between Advertising to Active doesn't recompose whole screen
        ServerStatusEnum.NONE -> ServerStatusEnum.NONE
        ServerStatusEnum.ADVERTISING_FAILED -> ServerStatusEnum.ADVERTISING_FAILED
        ServerStatusEnum.CLOSED -> ServerStatusEnum.CLOSED
    }
    Crossfade(
        targetState = mappedState,
        modifier = Modifier
            .fillMaxSize()
            .paint(
                rememberAsyncImagePainter(model = R.drawable.game_background),
                contentScale = ContentScale.Crop
            )
    ) { serverStatus ->
        when(serverStatus){
            ServerStatusEnum.NONE -> {
                StateContent(
                    text = UiTexts.StringResource(R.string.server_starting).asString(),
                    animationID = R.raw.loading_animation,
                    isPortrait = isPortrait,
                    onClickTry = {
                        serverViewModel.onGameEvent(GameEvents.CloseGame)
                    },
                    onClickBack = {
                        serverViewModel.onGameEvent(GameEvents.CloseGame)
                    },
                    tryIsCancel = true
                )
            }
            ServerStatusEnum.ADVERTISING_FAILED -> {
                StateContent(
                    text = UiTexts.StringResource(R.string.fail_start).asString(),
                    animationID = R.raw.error,
                    isPortrait = isPortrait,
                    onClickTry = {
                        serverViewModel.serverManager.startServer(context.packageName, ServerConfiguration(ServerType.valueOf(serverScreen.serverType), 10))
                    },
                    onClickBack = {
                        serverViewModel.onGameEvent(GameEvents.CloseGame)
                    }
                )
            }

            ServerStatusEnum.ADVERTISING, ServerStatusEnum.ACTIVE -> {
                val gameState by serverViewModel.gameState.collectAsStateWithLifecycle()

                when(ServerType.valueOf(serverScreen.serverType)){
                    ServerType.IS_PLAYER -> {
                        val serverPlayerVM = (serverViewModel as ServerPlayerViewModel)
                        serverPlayerVM.initPlayer(serverScreen.serverName, serverScreen.avatarId)

                        val playerState by serverPlayerVM.playerState.collectAsStateWithLifecycle()
                        val playerActionsState by serverPlayerVM.playerActionsState.collectAsStateWithLifecycle()

                        PlayerGameView(
                            gameState = gameState,
                            playerState = playerState,
                            playerActionsState = playerActionsState,
                            onPlayerEvent = serverPlayerVM::onPlayerEvent,
                            onGameEvent = serverViewModel::onGameEvent,
                            serverState = serverState
                        )
                    }
                    ServerType.IS_TABLE -> {
                        ServerGameView(
                            gameState = gameState,
                            serverState = serverState,
                            onGameEvent = serverViewModel::onGameEvent,
                        )
                    }
                }
            }

            ServerStatusEnum.CLOSED -> {
                LaunchedEffect(key1 = serverStatus) {
                    navController.navigate(MenuScreen) {
                        popUpTo(serverScreen) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}
