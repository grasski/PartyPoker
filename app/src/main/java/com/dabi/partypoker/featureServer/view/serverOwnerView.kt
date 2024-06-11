package com.dabi.partypoker.featureServer.view

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.featureCore.views.GameTable
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.ServerState
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerStatusEnum
import com.dabi.partypoker.managers.ServerType


@Composable
fun ServerOwnerView(
    navController: NavController,
    serverScreen: ServerScreen
) {
    val serverOwnerModel: ServerOwnerViewModel = hiltViewModel()
    val serverState by serverOwnerModel.serverBridge.serverState.collectAsStateWithLifecycle()
    val gameState by serverOwnerModel.gameState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        serverOwnerModel.serverBridge.onServerEvent(
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
            .padding(16.dp)
    ) { serverStatus ->
        when(serverStatus){
            ServerStatusEnum.OFF -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Server is starting, please wait.")
                        CircularProgressIndicator()
                    }
                }
            }
            ServerStatusEnum.ADVERTISING_FAILED -> {
                Text(text = "Error starting server. Maybe missing privileges etc.")
            }

            ServerStatusEnum.ADVERTISING, ServerStatusEnum.ACTIVE -> {
                ServerGameView(
                    navController = navController,
                    gameState = gameState,
                    serverState = serverState,
                    serverScreen = serverScreen,
                    onGameEvent = serverOwnerModel::onGameEvent,
                    onServerEvent = serverOwnerModel.serverBridge::onServerEvent
                )
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
        GameTable(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 50.dp)
                .padding(horizontal = 16.dp),
            gameState = gameState,
            onGameEvent = onGameEvent,
        )
    }
}
