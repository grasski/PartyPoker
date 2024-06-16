package com.dabi.partypoker.featureClient.view

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureClient.model.data.ClientState
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.views.DrawPlayersByPosition
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.featureCore.views.GameTable
import com.dabi.partypoker.featureCore.views.LoadingAnimation
import com.dabi.partypoker.featureCore.views.PlayerDrawItself
import com.dabi.partypoker.featureCore.views.PlayerDrawPlayers
import com.dabi.partypoker.featureCore.views.ServerDrawPlayers
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.SeatPosition
import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.ConnectionStatusEnum
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardType


@Composable
fun PlayerGameView(
    navController: NavController,
    nickname: String,
) {
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()
    val playerActionsState by playerViewModel.playerActionsState.collectAsStateWithLifecycle()
    val clientState by playerViewModel.clientBridge.clientState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        playerViewModel.clientBridge.onClientEvent(
            ClientEvents.Connect(context, nickname)
        )
    }

    var showPopUpMenu by rememberSaveable { mutableStateOf(false) }

    Crossfade(targetState = clientState.connectionStatus) { connectionStatus ->
        when (connectionStatus) {
            ConnectionStatusEnum.NONE, ConnectionStatusEnum.CONNECTING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingAnimation(
                        modifier = Modifier
                            .fillMaxSize(0.4f),
                        text = stringResource(R.string.client_connecting),
                        onCancelRequest = {
                            playerViewModel.onPlayerEvent(PlayerEvents.Disconnect)
                        }
                    )
                }
            }

            ConnectionStatusEnum.FAILED_TO_CONNECT -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Failed to connect to the server, please try again.")
                        Button(onClick = {
                            playerViewModel.clientBridge.onClientEvent(
                                ClientEvents.Connect(context, nickname)
                            )
                        }) {
                            Text(text = "Try again")
                        }
                    }
                }
            }

            ConnectionStatusEnum.CONNECTED -> {
                when(clientState.serverType){
                    ServerType.IS_TABLE -> {
                        PlayerViewPrivate(
                            navController,
                            playerState,
                            playerActionsState,
                            onPlayerEvent = playerViewModel::onPlayerEvent
                        )
                    }
                    ServerType.IS_PLAYER -> {
                        val gameState by playerViewModel.gameState.collectAsStateWithLifecycle()
                        PlayerViewServer(
                            navController,
                            gameState,
                            playerState,
                            playerActionsState,
                            onPlayerEvent = playerViewModel::onPlayerEvent,
                            onGameEvent = {}
                        )
                    }
                }
            }

            ConnectionStatusEnum.DISCONNECTED -> {
                LaunchedEffect(key1 = connectionStatus) {
                    navController.navigate(MenuScreen) {
                        popUpTo(PlayerScreen(nickname)) {
                            inclusive = true
                        }
                    }
                }
            }
        }

        if (showPopUpMenu) {
            GamePopUpMenu(
                navController,
                true,
                onDismissRequest = { showPopUpMenu = false },
                onLeaveRequest = { playerViewModel.onPlayerEvent(PlayerEvents.Disconnect) }
            )
        }
    }
}


@Composable
fun PlayerViewPrivate(
    navController: NavController,
    playerState: PlayerState,
    playerActionsState: PlayerActionsState,
    onPlayerEvent: (PlayerEvents) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            IconButton(onClick = {
//                showPopUpMenu = true
                // TODO()
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }

            Text(text = "Connected to the server!")

            Button(onClick = {
                onPlayerEvent(PlayerEvents.Ready)
            }) {
                Text(text = "READY")
            }

            Button(onClick = {
                onPlayerEvent(PlayerEvents.Disconnect)
            }) {
                Text(text = "Disconnect")
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        onPlayerEvent(PlayerEvents.Check)
                    },
                    enabled = playerActionsState.canCheck
                ) {
                    Text(text = "CHECK")
                }
                val callAmount = playerActionsState.callAmount
                Button(
                    onClick = {
                        onPlayerEvent(PlayerEvents.Call(callAmount))
                    },
                    enabled = callAmount > 0
                ) {
                    Text(text = "CALL $callAmount")
                }
                val raisingAmount = 30
                Button(
                    onClick = {
                        // So that is at least 30 + bigBlindValue
                        onPlayerEvent(PlayerEvents.Raise(playerActionsState.raiseAmount + raisingAmount))
                    },
                    enabled = playerState.isPlayingNow
                ) {
                    Text(text = "min RAISE ${playerActionsState.raiseAmount}\nRaising: ${playerActionsState.raiseAmount + raisingAmount}")
                }
                Button(
                    onClick = {
                        onPlayerEvent(PlayerEvents.Fold)
                    },
                    enabled = playerActionsState.canFold
                ) {
                    Text(text = "FOLD")
                }
            }
        }
    }
}


@Composable
fun PlayerViewServer(
    navController: NavController,
    gameState: GameState,
    playerState: PlayerState,
    playerActionsState: PlayerActionsState,
    onPlayerEvent: (PlayerEvents) -> Unit,

    onGameEvent: (GameEvents) -> Unit
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
                navController.navigate(MenuScreen) { popUpTo(navController.currentDestination?.route ?: "") { inclusive = true } }
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

            isServer = playerState.isServer,
            onGameEvent = onGameEvent,

            tableInfo = { p, s ->
                tablePosition = p
                tableSize = s
            }
        )

        PlayerDrawItself(
            player = playerState,
            playerActionsState = playerActionsState,
            onPlayerEvent = onPlayerEvent,
            tablePosition = tablePosition,
            tableSize = tableSize
        )

        val sortedPlayers = gameState.seatPositions.toList().sortedBy { it.second.position }.toMap()
        val meIndex = sortedPlayers.keys.indexOf(playerState.id)
        val myPosition = sortedPlayers[playerState.id]?.position
        if (meIndex <= -1 || myPosition == null){
            return
        }
        val playersBeforeMe = sortedPlayers.toList().take(meIndex).toMap()
        val players = sortedPlayers
            .minus(playersBeforeMe.keys)
            .plus(playersBeforeMe)
            .minus(playerState.id)
            .entries.associate { it.value.position to gameState.players[it.key] }


        DrawPlayersByPosition(
            players = players,
            myPosition = myPosition,
            serverType = ServerType.IS_PLAYER,
            tablePosition = tablePosition,
            tableSize = tableSize
        )
    }
}
