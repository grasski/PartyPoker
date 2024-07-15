package com.dabi.partypoker.featureServer.view

import android.app.Activity
import android.content.pm.ActivityInfo
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
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureClient.view.PlayerGameView
import com.dabi.partypoker.featureCore.data.colors
import com.dabi.partypoker.featureCore.views.AutoSizeText
import com.dabi.partypoker.featureCore.views.LoadingAnimation
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerPlayerViewModel
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerStatusEnum
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.utils.UiTexts


@Composable
fun ServerView(
    navController: NavController,
    serverScreen: ServerScreen,
) {
    val serverViewModel = if (serverScreen.serverType == ServerType.IS_TABLE.toString()){
//        hiltViewModel<ServerOwnerViewModel>()
        hiltViewModel(
            creationCallback = { factory : ServerOwnerViewModel.ServerOwnerViewModelFactory ->
                factory.create(gameSettingsId = serverScreen.serverGameSettingsId)
            }
        )
    } else {
//        hiltViewModel<ServerPlayerViewModel>()
        hiltViewModel(
            creationCallback = { factory : ServerPlayerViewModel.ServerPlayerViewModelFactory ->
                factory.create(gameSettingsId = serverScreen.serverGameSettingsId)
            }
        )
    }
    val serverState by serverViewModel.serverBridge.serverState.collectAsStateWithLifecycle()
    val mappedState = when (serverState.serverStatus) {
        ServerStatusEnum.ADVERTISING, ServerStatusEnum.ACTIVE -> ServerStatusEnum.ACTIVE    // So the change between Advertising to Active doesn't recompose whole screen
        ServerStatusEnum.NONE -> ServerStatusEnum.NONE
        ServerStatusEnum.ADVERTISING_FAILED -> ServerStatusEnum.ADVERTISING_FAILED
        ServerStatusEnum.OFF -> ServerStatusEnum.OFF
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        serverViewModel.serverBridge.onServerEvent(
            ServerEvents.StartServer(context, ServerType.valueOf(serverScreen.serverType), serverScreen.serverName)
        )

        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }


    Crossfade(
        targetState = mappedState,
        modifier = Modifier
            .fillMaxSize()
            .paint(
                rememberAsyncImagePainter(model = R.drawable.game_background),
                contentScale = ContentScale.Crop
            )
    ) { serverStatus ->
        when(serverStatus){
            ServerStatusEnum.NONE -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    LoadingAnimation(
                        modifier = Modifier
                            .fillMaxSize(0.8f),
                        text = UiTexts.StringResource(R.string.server_starting).asString(),
                        onCancelRequest = {
                            serverViewModel.onGameEvent(GameEvents.CloseGame)
                        }
                    )
                }
            }
            ServerStatusEnum.ADVERTISING_FAILED -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            serverViewModel.onGameEvent(GameEvents.CloseGame)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = colors.calledMoneyColor
                        ),
                        contentPadding = PaddingValues(5.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .border(2.dp, colors.calledMoneyColor, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.playerButtonsColor2.copy(0.8f))
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
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.error))
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
                                    colors.calledMoneyColor,
                                    RoundedCornerShape(10.dp)
                                )
                                .background(colors.playerButtonsColor2.copy(0.8f))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Text(
                                text = UiTexts.StringResource(R.string.fail_start).asString(),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )

                            Button(
                                onClick = {
                                    serverViewModel.serverBridge.onServerEvent(
                                        ServerEvents.StartServer(context, ServerType.valueOf(serverScreen.serverType), serverScreen.serverName)
                                    )
                                },
                                modifier = Modifier
                                    .padding(top = 10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.playerBoxColor1,
                                    contentColor = Color.White
                                ),
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

            ServerStatusEnum.ADVERTISING, ServerStatusEnum.ACTIVE -> {
                val gameState by serverViewModel.gameState.collectAsStateWithLifecycle()

                when(ServerType.valueOf(serverScreen.serverType)){
                    ServerType.IS_PLAYER -> {
                        val serverPlayerVM = (serverViewModel as ServerPlayerViewModel)
                        serverPlayerVM.initPlayer(serverScreen.serverName, serverScreen.avatarId)

                        val playerState by serverPlayerVM.playerState.collectAsStateWithLifecycle()
                        val playerActionsState by serverPlayerVM.playerActionsState.collectAsStateWithLifecycle()

                        PlayerGameView(
                            gameState = gameState,
                            playerState = playerState,
                            playerActionsState = playerActionsState,
                            onPlayerEvent = serverPlayerVM::onPlayerEvent,
                            onGameEvent = serverViewModel::onGameEvent,
                            serverState = serverState
                        )
                    }
                    ServerType.IS_TABLE -> {
                        ServerGameView(
                            gameState = gameState,
                            serverState = serverState,
                            onGameEvent = serverViewModel::onGameEvent,
                        )
                    }
                }
            }

            ServerStatusEnum.OFF -> {
                LaunchedEffect(key1 = serverStatus) {
                    navController.navigate(MenuScreen) {
                        popUpTo(serverScreen) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}
