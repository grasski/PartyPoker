package com.dabi.partypoker.featureServer.view

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.view.PlayerViewServer
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.featureCore.views.GameTable
import com.dabi.partypoker.featureCore.views.LoadingAnimation
import com.dabi.partypoker.featureCore.views.ServerDrawPlayers
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.ServerState
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerPlayerViewModel
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerStatusEnum
import com.dabi.partypoker.managers.ServerType


@Composable
fun ServerOwnerView(
    navController: NavController,
    serverScreen: ServerScreen,
) {
//    val serverViewModel = if (serverScreen.serverType == ServerType.IS_TABLE.toString()){
//        hiltViewModel<ServerOwnerViewModel>()
//    } else {
//        hiltViewModel<ServerPlayerViewModel>()
//    }
    val serverViewModel: ServerOwnerViewModel = hiltViewModel()
    val serverState by serverViewModel.serverBridge.serverState.collectAsStateWithLifecycle()


    val context = LocalContext.current
    LaunchedEffect(true) {
        Log.e("", "HALOO???")
        serverViewModel.serverBridge.onServerEvent(
            ServerEvents.StartServer(context, ServerType.valueOf(serverScreen.serverType), serverScreen.serverName)
        )

//        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
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
            ServerStatusEnum.OFF -> {
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

                Column {
                    Text(text = gameState.toString())
                    
                    Text(text = serverState.toString())
                }

//                when(ServerType.valueOf(serverScreen.serverType)){
//                    ServerType.IS_PLAYER -> {
//                        val serverPlayerVM = (serverViewModel as ServerPlayerViewModel)
//                        val playerState by serverPlayerVM.playerState.collectAsStateWithLifecycle()
//                        val playerActionsState by serverPlayerVM.playerActionsState.collectAsStateWithLifecycle()
//
//                        PlayerViewServer(
//                            navController = navController,
//                            gameState = gameState,
//                            playerState = playerState,
//                            playerActionsState = playerActionsState,
//                            onPlayerEvent = serverPlayerVM::onPlayerEvent,
//                            onGameEvent = serverViewModel::onGameEvent,
//                        )
//                    }
//                    ServerType.IS_TABLE -> {
//                        ServerGameView(
//                            navController = navController,
//                            gameState = gameState,
//                            serverState = serverState,
//                            serverScreen = serverScreen,
//                            onGameEvent = serverViewModel::onGameEvent,
//                            onServerEvent = serverViewModel.serverBridge::onServerEvent
//                        )
//                    }
//                }
            }
        }
    }
}


@Composable
fun ServerGameView(
    navController: NavController,
    gameState: GameState,
    serverState: ServerState,
    serverScreen: ServerScreen,
    onGameEvent: (GameEvents) -> Unit,
    onServerEvent: (ServerEvents) -> Unit
) {
    var showPopUpMenu by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = {
        showPopUpMenu = true
    }) {
        Icon(Icons.Default.Menu, contentDescription = "Menu")
    }
    if (showPopUpMenu) {
        GamePopUpMenu(
            navController = navController,
            isPlayer = false,
            onDismissRequest = { showPopUpMenu = false },
            onLeaveRequest = {
                onGameEvent(GameEvents.CloseGame)
                navController.navigate(MenuScreen) { popUpTo(ServerScreen(serverScreen.serverType, serverScreen.serverName)) { inclusive = true } }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        var tablePosition by remember { mutableStateOf(Offset.Zero) }
        var tableSize by remember { mutableStateOf(IntSize.Zero) }

        GameTable(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 50.dp)
                .padding(horizontal = 16.dp),
            gameState = gameState,

            isServer = true,
            onGameEvent = onGameEvent,

            tableInfo = { p, s ->
                tablePosition = p
                tableSize = s
            }
        )
        ServerDrawPlayers(
            gameState = gameState
//                .copy(players = mapOf(
//                "0" to PlayerState("0", "0"),
//                "1" to PlayerState("1", "1", isReadyToPlay = true),
//                "2" to PlayerState("2", "2", isReadyToPlay = true, isPlayingNow = true),
//                "3" to PlayerState("3", "3", isReadyToPlay = true),
//                "4" to PlayerState("4", "4", isReadyToPlay = true),
//                "5" to PlayerState("5", "5", isReadyToPlay = true),
//                "6" to PlayerState("6", "6", isReadyToPlay = true),
//                "7" to PlayerState("7", "7", isReadyToPlay = true),
//                "8" to PlayerState("8", "8", isReadyToPlay = true),
//            ))
            ,
            tablePosition = tablePosition,
            tableSize = tableSize
        )
    }
}
