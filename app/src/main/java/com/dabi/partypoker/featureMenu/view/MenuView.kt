package com.dabi.partypoker.featureMenu.view

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerPlayerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState


enum class ViewPosition{
    MENU,
    LOCAL_SERVER,
    LOCAL_PLAYER
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MenuView(
    navController: NavController
) {
    val context = LocalContext.current
    var viewPosition by rememberSaveable { mutableStateOf(ViewPosition.MENU) }
    var nickname by rememberSaveable { mutableStateOf("") }

    var permissions: MultiplePermissionsState = rememberMultiplePermissionsState(emptyList())
    CheckPermissions(permissionsState = { permissions = it })

    Crossfade(targetState = viewPosition) {
        when (it) {
            ViewPosition.MENU -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        viewPosition = ViewPosition.LOCAL_SERVER
                    }) {
                        Text(context.getString(R.string.create_local))
                    }
                    Button(onClick = {
                        viewPosition = ViewPosition.LOCAL_PLAYER
                    }) {
                        Text(context.getString(R.string.connect_local))
                    }
                }
            }
            ViewPosition.LOCAL_SERVER -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        BackHandler(true) {
                            viewPosition = ViewPosition.MENU
                        }

                        if (permissions.allPermissionsGranted) {
                            var localServerPlaying by rememberSaveable { mutableStateOf(false) }
                            Row{
                                Text(text = "Are you playing?")
                                Checkbox(
                                    checked = localServerPlaying,
                                    onCheckedChange = { localServerPlaying = it }
                                )
                            }
                            
                            val serverPlayeriewModel: ServerPlayerViewModel = hiltViewModel()
                            val serverState by serverPlayeriewModel.serverBridge.serverState.collectAsStateWithLifecycle()
                            val gameState by serverPlayeriewModel.gameState.collectAsStateWithLifecycle()
                            val playerState by serverPlayeriewModel.playerState.collectAsStateWithLifecycle()
                            LaunchedEffect(playerState, gameState) {
                                
                            }

                            serverPlayeriewModel.serverBridge.onServerEvent(ServerEvents.StartServer(context, ServerType.IS_TABLE))
                            Column(){
                                Text(text = serverState.toString())

                                Button(onClick = {
                                    serverPlayeriewModel.onGameEvent(GameEvents.StartGame)
                                }) {
                                    Text("START")
                                }

                                Text(text = "READY = ${playerState.isReadyToPlay}, money = ${playerState.money}")
                                Button(onClick = {
                                    serverPlayeriewModel.onPlayerEvent(PlayerEvents.Ready)
                                }){
                                    Text(text = "READY")
                                }

                                Text(text = "PLAYING = ${playerState.isPlayingNow}, called = ${playerState.called}")
                                Text("ROUND = ${gameState.round}, raiser: ${gameState.activeRaise}\nBANK = ${gameState.bank}")

                                Row (
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = {
                                            serverPlayeriewModel.onPlayerEvent(PlayerEvents.Check)
                                        },
                                        enabled = serverPlayeriewModel.checkEnabled()
                                    ) {
                                        Text(text = "CHECK")
                                    }

                                    val callAmount = serverPlayeriewModel.activeCallValue()
                                    Button(
                                        onClick = {
                                            serverPlayeriewModel.onPlayerEvent(PlayerEvents.Call(callAmount))
                                        },
                                        enabled = callAmount > 0
                                    ) {
                                        Text(text = "CALL $callAmount")
                                    }

                                    val raisingAmount = 30
                                    Button(
                                        onClick = {
                                            // So that is at least 30 + smallBlindValue
                                            serverPlayeriewModel.onPlayerEvent(PlayerEvents.Raise(serverPlayeriewModel.minimalRaise() + raisingAmount))
                                        },
                                        enabled = playerState.isPlayingNow
                                    ) {
                                        Text(text = "min RAISE ${serverPlayeriewModel.minimalRaise()}\nRaising: ${serverPlayeriewModel.minimalRaise() + raisingAmount}")
                                    }

                                    Button(
                                        onClick = { /*TODO*/ },
                                        enabled = playerState.isPlayingNow
                                    ) {
                                        Text(text = "FOLD")
                                    }
                                }

                                Text(text = "BB: ${gameState.bigBlindId}, SB: ${gameState.smallBlindId}, dealer: ${gameState.dealerId}, now= ${gameState.playingNow}")

                                gameState.players.forEach{
                                    Text(text = "Hrac: ${it.value.nickname}, ${it.value.id}")
                                }
                            }


//                            Button(onClick = {
//                                navController.navigate(
//                                    Routes.LocalServer.route +
//                                            "/${
//                                                if (localServerPlaying) {
//                                                    ServerType.PLAYER
//                                                } else {
//                                                    ServerType.TABLE}
//                                            }"
//                                )
//                                { popUpTo(Routes.Menu.route){inclusive = true} }
//                            }) {
//                                Text(text = "Host a game")
//                            }
                        } else{
                            Text("Not possible to Host a game without accepting permissions")
                        }
                    }
                }
            }
            ViewPosition.LOCAL_PLAYER -> {
                val playerViewModel: PlayerViewModel = hiltViewModel()
                Column {
                    Button(onClick = {
                        playerViewModel.clientBridge.onClientEvent(ClientEvents.Connect(context, "dabi"))
                        navController.navigate(PlayerScreen)
                    }) {
                        Text("KLIK")
                    }
                }

            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPermissions(
    permissionsState: (MultiplePermissionsState) -> Unit
) {
    var permissionsList = listOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        permissionsList = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissionsList = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else{
        permissionsList = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissions = rememberMultiplePermissionsState(permissions = permissionsList)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    permissions.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    permissionsState(permissions)
}