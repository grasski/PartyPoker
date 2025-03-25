package com.dabi.partypoker.featurePlayer.view

import android.content.res.Configuration
import androidx.annotation.RawRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dabi.easylocalgame.clientSide.data.ConnectionStatusEnum
import com.dabi.easylocalgame.clientSide.data.PlayerConnectionState
import com.dabi.easylocalgame.serverSide.data.ServerState
import com.dabi.easylocalgame.serverSide.data.ServerType
import com.dabi.easylocalgame.composeUtils.UiTexts
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.featureCore.views.StateContent
import com.dabi.partypoker.featurePlayer.viewmodel.PlayerEvents
import com.dabi.partypoker.featurePlayer.viewmodel.PlayerViewModel


@Composable
fun PlayerView(
    navController: NavController,
    nickname: String,
    @RawRes avatarId: Int
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val playerViewModel: PlayerViewModel = hiltViewModel()
    val changeView by playerViewModel.viewChangeState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()
    val playerActionsState by playerViewModel.playerActionsState.collectAsStateWithLifecycle()
    val clientState by playerViewModel.clientManager.clientState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        playerViewModel.clientManager.connect(context.packageName, PlayerConnectionState(nickname, avatarId))
    }

    val mappedState = when (clientState.connectionStatus) {
        ConnectionStatusEnum.NONE, ConnectionStatusEnum.CONNECTING -> ConnectionStatusEnum.CONNECTING
        ConnectionStatusEnum.CONNECTING_FAILED -> ConnectionStatusEnum.CONNECTING_FAILED
        ConnectionStatusEnum.CONNECTING_REJECTED, ConnectionStatusEnum.ROOM_IS_FULL -> ConnectionStatusEnum.CONNECTING_REJECTED
        ConnectionStatusEnum.ENDPOINT_LOST -> ConnectionStatusEnum.ENDPOINT_LOST
        ConnectionStatusEnum.CONNECTED, ConnectionStatusEnum.CONNECTION_ESTABLISHED -> ConnectionStatusEnum.CONNECTION_ESTABLISHED // So the change between CONNECTED to CONNECTION_ESTABLISHED doesn't recompose whole screen
        ConnectionStatusEnum.DISCONNECTED -> ConnectionStatusEnum.DISCONNECTED
    }
    Crossfade(
        targetState = mappedState,
        modifier = Modifier
            .fillMaxSize()
            .paint(
                rememberAsyncImagePainter(model = R.drawable.game_background),
                contentScale = ContentScale.Crop
            ),
    ) { connectionStatus ->
        when (connectionStatus) {
            ConnectionStatusEnum.NONE, ConnectionStatusEnum.CONNECTING -> {
                StateContent(
                    text = UiTexts.StringResource(R.string.client_connecting).asString(),
                    animationID = R.raw.loading_animation,
                    isPortrait = isPortrait,
                    onClickTry = {
                        playerViewModel.onPlayerEvent(PlayerEvents.Leave)
                    },
                    onClickBack = {
                        playerViewModel.onPlayerEvent(PlayerEvents.Leave)
                    },
                    tryIsCancel = true
                )
            }
            ConnectionStatusEnum.CONNECTING_FAILED -> {
                StateContent(
                    text = UiTexts.StringResource(R.string.fail_connect).asString(),
                    animationID = R.raw.enable_location,
                    isPortrait = isPortrait,
                    onClickTry = {
                        playerViewModel.clientManager.connect(context.packageName, PlayerConnectionState(nickname, avatarId))
                    },
                    onClickBack = {
                        playerViewModel.onPlayerEvent(PlayerEvents.Leave)
                    }
                )
            }
            ConnectionStatusEnum.CONNECTING_REJECTED, ConnectionStatusEnum.ROOM_IS_FULL -> {
                StateContent(
                    text = UiTexts.StringResource(R.string.reject_connect).asString(),
                    animationID = R.raw.rejected_animation,
                    isPortrait = isPortrait,
                    onClickTry = {
                        playerViewModel.clientManager.connect(context.packageName, PlayerConnectionState(nickname, avatarId))
                    },
                    onClickBack = {
                        playerViewModel.onPlayerEvent(PlayerEvents.Leave)
                    }
                )
            }
            ConnectionStatusEnum.ENDPOINT_LOST -> {
                StateContent(
                    text = UiTexts.StringResource(R.string.lost_connection).asString(),
                    animationID = R.raw.rejected_animation,
                    isPortrait = isPortrait,
                    onClickTry = {
                        playerViewModel.clientManager.connect(context.packageName, PlayerConnectionState(nickname, avatarId))
                    },
                    onClickBack = {
                        playerViewModel.onPlayerEvent(PlayerEvents.Leave)
                    }
                )
            }
            ConnectionStatusEnum.CONNECTED, ConnectionStatusEnum.CONNECTION_ESTABLISHED -> {
                when(clientState.serverType){
                    ServerType.IS_TABLE -> {
                        val gameState by playerViewModel.gameState.collectAsStateWithLifecycle()

                        if (changeView){
                            PlayerGameView(
                                playerState = playerState,
                                playerActionsState = playerActionsState,
                                onPlayerEvent = playerViewModel::onPlayerEvent,

                                gameState = gameState,
                                onGameEvent = {}
                            )
                        } else{
                            PlayerViewPrivate(
                                playerState = playerState,
                                playerActionsState = playerActionsState,
                                onPlayerEvent = playerViewModel::onPlayerEvent,

                                gameState = gameState
                            )
                        }
                    }
                    ServerType.IS_PLAYER -> {
                        val gameState by playerViewModel.gameState.collectAsStateWithLifecycle()

                        PlayerGameView(
                            playerState = playerState,
                            playerActionsState = playerActionsState,
                            onPlayerEvent = playerViewModel::onPlayerEvent,

                            gameState = gameState,
                            onGameEvent = {},
                            serverState = ServerState(serverType = ServerType.IS_PLAYER),
                        )
                    }
                }
            }
            ConnectionStatusEnum.DISCONNECTED -> {
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
