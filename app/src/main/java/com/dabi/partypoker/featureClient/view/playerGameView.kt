package com.dabi.partypoker.featureClient.view

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.featureClient.model.data.ClientState
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.ConnectionStatusEnum


@Composable
fun PlayerGameView(
    navController: NavController,
    nickname: String,
    playerState: PlayerState,
    clientState: ClientState,
    onPlayerEvent: (PlayerEvents) -> Unit,
    onClientEvent: (ClientEvents) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        onClientEvent(
            ClientEvents.Connect(context, nickname)
        )

//        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }
    
    var showPopUpMenu by rememberSaveable { mutableStateOf(false) }
    
    Crossfade(targetState = clientState.connectionStatus) { connectionStatus ->
        when (connectionStatus){
            ConnectionStatusEnum.NONE -> {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp)
//                ){
//                    IconButton(onClick = {
//                        showPopUpMenu = true
//                    }) {
//                        Icon(Icons.Default.Menu, contentDescription = "Menu")
//                    }
//                }
            }
            ConnectionStatusEnum.CONNECTING -> {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp)
//                ){
//                    IconButton(onClick = {
//                        showPopUpMenu = true
//                    }) {
//                        Icon(Icons.Default.Menu, contentDescription = "Menu")
//                    }
//                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Connecting to the server, please wait.")
                        CircularProgressIndicator()
                    }
                }
            }
            ConnectionStatusEnum.FAILED_TO_CONNECT -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Failed to connect to the server, please try again.")
                        Button(onClick = {
                            onClientEvent(
                                ClientEvents.Connect(context, nickname)
                            )
                        }) {
                            Text(text = "Try again")
                        }
                    }
                }
            }
            ConnectionStatusEnum.CONNECTED -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ){
                    Column {
                        IconButton(onClick = {
                            showPopUpMenu = true
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }

                        Text(text = "Connected to the server!")

                        Button(onClick = {
                            onPlayerEvent(PlayerEvents.Ready)
                        }){
                            Text(text = "READY")
                        }

                        Button(onClick = {
                            onPlayerEvent(PlayerEvents.Disconnect)
                        }) {
                            Text(text = "Disconnect")
                        }
                    }
                }
            }

            ConnectionStatusEnum.DISCONNECTED -> {
                LaunchedEffect(key1 = connectionStatus) {
                    navController.navigate(MenuScreen) { popUpTo(PlayerScreen(nickname)) { inclusive = true } }
                }
            }
        }

        if (showPopUpMenu) {
            GamePopUpMenu(
                navController,
                true,
                onDismissRequest = { showPopUpMenu = false },
                onLeaveRequest = { onPlayerEvent(PlayerEvents.Disconnect) }
            )
        }
    }


//    val gameState by playerViewModel.gameState.collectAsStateWithLifecycle()
//    LaunchedEffect(key1 = gameState) {}
//
//    Column {
//        Text("Player Name: ${playerState.nickname} and ID: ${playerState.id}")
//        Text("Server: ${clientState.serverID}, status: ${clientState.connectionStatus}")
//
//        Button(onClick = {
//            playerViewModel.onPlayerEvent(PlayerEvents.Disconnect)
//        }) {
//            Text(text = "DISCONNECT")
//        }
//
//        Spacer(modifier = Modifier.height(10.dp))
//
//        Text(text = "READY = ${playerState.isReadyToPlay}, money = ${playerState.money}")
//        Button(onClick = {
//            playerViewModel.onPlayerEvent(PlayerEvents.Ready)
//        }){
//            Text(text = "READY")
//        }
//
//        Text(text = "PLAYING = ${playerState.isPlayingNow}, called = ${playerState.called}")
//        Text("ROUND = ${gameState.round}, raiser: ${gameState.activeRaise}\nBANK = ${gameState.bank}")
//
//        Row (
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
//        ) {
//            Button(
//                onClick = {
//                    playerViewModel.onPlayerEvent(PlayerEvents.Check)
//                },
//                enabled = playerViewModel.checkEnabled()
//            ) {
//                Text(text = "CHECK")
//            }
//
//            val callAmount = playerViewModel.activeCallValue()
//            Button(
//                onClick = {
//                    playerViewModel.onPlayerEvent(PlayerEvents.Call(callAmount))
//                },
//                enabled = callAmount > 0
//            ) {
//                Text(text = "CALL $callAmount")
//            }
//
//            val raisingAmount = 30
//            Button(
//                onClick = {
//                    // So that is at least 30 + bigBlindValue
//                    playerViewModel.onPlayerEvent(PlayerEvents.Raise(playerViewModel.minimalRaise() + raisingAmount))
//                },
//                enabled = playerState.isPlayingNow
//            ) {
//                Text(text = "min RAISE ${playerViewModel.minimalRaise()}\nRaising: ${playerViewModel.minimalRaise() + raisingAmount}")
//            }
//
//            Button(
//                onClick = {
//                    playerViewModel.onPlayerEvent(PlayerEvents.Fold)
//                },
//                enabled = playerState.isPlayingNow
//            ) {
//                Text(text = "FOLD")
//            }
//        }
//    }
}