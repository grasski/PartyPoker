package com.dabi.partypoker.featureClient.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureCore.views.LoadingAnimation
import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.ConnectionStatusEnum
import com.dabi.partypoker.managers.ServerType


@Composable
fun PlayerView(
    navController: NavController,
    nickname: String,
) {
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val changeView by playerViewModel.viewChangeState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()
    val playerActionsState by playerViewModel.playerActionsState.collectAsStateWithLifecycle()
    val clientState by playerViewModel.clientBridge.clientState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        playerViewModel.clientBridge.onClientEvent(
            ClientEvents.Connect(context, nickname)
        )
    }


    Crossfade(
        targetState = clientState.connectionStatus,
        modifier = Modifier.paint(
            rememberAsyncImagePainter(model = R.drawable.game_background),
            contentScale = ContentScale.FillWidth
        )
    ) { connectionStatus ->
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
                            playerViewModel.onPlayerEvent(PlayerEvents.Leave)
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
                        if (changeView){
                            val gameState by playerViewModel.gameState.collectAsStateWithLifecycle()

                            PlayerGameView(
                                gameState,
                                playerState,
                                playerActionsState,
                                onPlayerEvent = playerViewModel::onPlayerEvent,

                                onGameEvent = {}
                            )
                        } else{
                            PlayerViewPrivate()
                        }
//                        PlayerViewPrivate(
//                            navController,
//                            playerState,
//                            playerActionsState,
//                            onPlayerEvent = playerViewModel::onPlayerEvent
//                        )
                    }
                    ServerType.IS_PLAYER -> {
                        val gameState by playerViewModel.gameState.collectAsStateWithLifecycle()

                        PlayerGameView(
                            gameState,
                            playerState,
                            playerActionsState,
                            onPlayerEvent = playerViewModel::onPlayerEvent,

                            onGameEvent = {}
                        )
                    }
                }
            }

            ConnectionStatusEnum.KICKED, ConnectionStatusEnum.LEFT -> {
                LaunchedEffect(key1 = connectionStatus) {
                    navController.navigate(MenuScreen) {
                        popUpTo(PlayerScreen(nickname)) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}
