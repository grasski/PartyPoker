package com.dabi.partypoker.featureServer.view

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureCore.views.DrawPlayersByPosition
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.featureCore.views.GameTable
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.ServerState
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerEvents


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

        val players = gameState.seatPositions.entries.associate { it.value.position to gameState.players[it.key] }
        DrawPlayersByPosition(
            players = players,
            gameState = gameState,
            serverType = serverState.serverType,
            tablePosition = tablePosition,
            tableSize = tableSize
        )
    }
}