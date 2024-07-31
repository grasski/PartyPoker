package com.dabi.partypoker.featureClient.view

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.views.DrawPlayersByPosition
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.featureCore.views.GameTable
import com.dabi.partypoker.featureCore.views.PlayerDrawItself
import com.dabi.partypoker.featureCore.views.calculatePlayersPosition
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.ServerState
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerType


@Composable
fun PlayerGameView(
    gameState: GameState,
    playerState: PlayerState,
    playerActionsState: PlayerActionsState,
    onPlayerEvent: (PlayerEvents) -> Unit,

    onGameEvent: (GameEvents) -> Unit = { },
    serverState: ServerState = ServerState()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    GamePopUpMenu(
        isPlayer = !playerState.isServer,
        onPlayerEvent = onPlayerEvent,
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
                .padding(horizontal = 16.dp),
            gameState = gameState.copy(),

            isServer = playerState.isServer,
            onGameEvent = onGameEvent,

            tableInfo = { p, s ->
                tablePosition = p
                tableSize = s
            }
        )

        val calculatePlayersPosition = calculatePlayersPosition(
            gameState = gameState.copy(),
            currentPlayerId = playerState.id
        )
        if (calculatePlayersPosition.first != null && calculatePlayersPosition.second.isNotEmpty()){
            val myPosition = calculatePlayersPosition.first!!
            val players = calculatePlayersPosition.second

            DrawPlayersByPosition(
                players = players,
                gameState = gameState,
                myPosition = myPosition,
                serverType = ServerType.IS_PLAYER,
                tablePosition = tablePosition,
                tableSize = tableSize
            )
        }

        PlayerDrawItself(
            player = playerState,
            playerActionsState = playerActionsState,
            onPlayerEvent = onPlayerEvent,
            gameState = gameState,
            tablePosition = tablePosition,
            tableSize = tableSize
        )
    }
}
