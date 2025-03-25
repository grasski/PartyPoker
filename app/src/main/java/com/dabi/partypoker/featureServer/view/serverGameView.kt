package com.dabi.partypoker.featureServer.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dabi.easylocalgame.serverSide.data.ServerState
import com.dabi.partypoker.featureCore.views.DrawPlayersByPosition
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.featureCore.views.GameTable
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.managers.GameEvents

@Composable
fun ServerGameView(
    gameState: GameState,
    serverState: ServerState,
    onGameEvent: (GameEvents) -> Unit,
) {
    GamePopUpMenu(
        isPlayer = false,
        onPlayerEvent = {  },
        playerState = null,
        onGameEvent = onGameEvent,
        serverStatus = serverState
    )

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
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
            ,
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