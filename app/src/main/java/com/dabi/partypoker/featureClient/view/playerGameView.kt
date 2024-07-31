package com.dabi.partypoker.featureClient.view

import androidx.annotation.RawRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureCore.views.AutoSizeText
import com.dabi.partypoker.featureCore.views.LoadingAnimation
import com.dabi.partypoker.featureServer.model.data.ServerState
import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.ConnectionStatusEnum
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.ui.theme.textColor
import com.dabi.partypoker.utils.UiTexts


@Composable
fun PlayerView(
    navController: NavController,
    nickname: String,
    @RawRes avatarId: Int
) {
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val changeView by playerViewModel.viewChangeState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()
    val playerActionsState by playerViewModel.playerActionsState.collectAsStateWithLifecycle()
    val clientState by playerViewModel.clientBridge.clientState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        playerViewModel.clientBridge.onClientEvent(
            ClientEvents.Connect(context, nickname, avatarId)
        )
    }

    Crossfade(
        targetState = clientState.connectionStatus,
        modifier = Modifier
            .fillMaxSize()
            .paint(
                rememberAsyncImagePainter(model = R.drawable.game_background),
                contentScale = ContentScale.Crop
            ),
    ) { connectionStatus ->
        when (connectionStatus) {
            ConnectionStatusEnum.NONE, ConnectionStatusEnum.CONNECTING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingAnimation(
                        modifier = Modifier
                            .fillMaxSize(0.8f),
                        text = UiTexts.StringResource(R.string.client_connecting).asString(),
                        onCancelRequest = {
                            playerViewModel.onPlayerEvent(PlayerEvents.Leave)
                        }
                    )
                }
            }

            ConnectionStatusEnum.FAILED_TO_CONNECT -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            playerViewModel.onPlayerEvent(PlayerEvents.Leave)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = textColor
                        ),
                        contentPadding = PaddingValues(5.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .border(2.dp, textColor, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer.copy(0.8f))
                            .align(Alignment.TopStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "back"
                            )
                            Text("Menu")
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight()
                        ,
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.enable_location))
                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = LottieConstants.IterateForever
                        )
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier
                                .size(200.dp)
                        )

                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    2.dp,
                                    textColor,
                                    RoundedCornerShape(10.dp)
                                )
                                .background(MaterialTheme.colorScheme.surfaceContainer.copy(0.8f))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text(
                                text = UiTexts.StringResource(R.string.fail_connect).asString(),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    playerViewModel.clientBridge.onClientEvent(
                                        ClientEvents.Connect(context, nickname, avatarId)
                                    )
                                },
                                modifier = Modifier
                                    .padding(top = 10.dp),
                                shape = RoundedCornerShape(10.dp)
                            ){
                                AutoSizeText(
                                    text = "Try again",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
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
                    }
                    ServerType.IS_PLAYER -> {
                        val gameState by playerViewModel.gameState.collectAsStateWithLifecycle()

                        PlayerGameView(
                            gameState,
                            playerState,
                            playerActionsState,
                            onPlayerEvent = playerViewModel::onPlayerEvent,

                            serverState = ServerState(serverType = ServerType.IS_PLAYER),
                            onGameEvent = {}
                        )
                    }
                }
            }

            ConnectionStatusEnum.KICKED, ConnectionStatusEnum.LEFT -> {
                LaunchedEffect(key1 = connectionStatus) {
                    navController.navigate(MenuScreen) {
                        popUpTo(PlayerScreen(nickname, avatarId)) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}
