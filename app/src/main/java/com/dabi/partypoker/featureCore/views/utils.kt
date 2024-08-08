package com.dabi.partypoker.featureCore.views


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.model.data.endpointID
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.ServerState
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerStatusEnum
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.ui.theme.textColor
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.formatNumberToString


@Composable
fun CalculatePlayerBoxSize(
    playerBoxSize: (DpSize) -> Unit,
    fontSize: (TextUnit) -> Unit
) {
    val configuration = LocalConfiguration.current
    LaunchedEffect(Unit) {
        when(configuration.screenWidthDp){
            in 0..750 -> {
                playerBoxSize(DpSize(100.dp, 50.dp))
                fontSize(10.sp)
            }
            in 751..1100 -> {
                playerBoxSize(DpSize(140.dp, 70.dp))
                fontSize(13.sp)
            }
            in 1101..1550 -> {
                playerBoxSize(DpSize(180.dp, 90.dp))
                fontSize(18.sp)
            }
            else -> {
                playerBoxSize(DpSize(220.dp, 110.dp))
                fontSize(23.sp)
            }
        }
    }
}


fun calculatePlayersPosition(
    gameState: GameState,
    currentPlayerId: endpointID
): Pair<Int?, Map<Int, PlayerState?>> {
    val sortedPlayers = gameState.seatPositions.toList().sortedBy { it.second.position }.toMap()
    val meIndex = sortedPlayers.keys.indexOf(currentPlayerId)
    val myPosition = sortedPlayers[currentPlayerId]?.position
    if (meIndex <= -1 || myPosition == null){
        return Pair(null, emptyMap())
    }
    val playersBeforeMe = sortedPlayers.toList().take(meIndex).toMap()

    return Pair(myPosition, sortedPlayers
        .minus(playersBeforeMe.keys)
        .plus(playersBeforeMe)
        .minus(currentPlayerId)
        .entries.associate { it.value.position to gameState.players[it.key] })
}


@Composable
fun LoadingAnimation(
    modifier: Modifier,
    text: String,
    onCancelRequest: () -> Unit
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxHeight(0.4f)
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
            AutoSizeText(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            )

            Button(
                onClick = {
                    onCancelRequest()
                },
                modifier = Modifier
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(10.dp)
            ){
                AutoSizeText(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}


@Composable
fun Modifier.glowItem(
    itemCornerRadius: Dp,
    active: Boolean
): Modifier{
    if (!active) return this

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val colorAnimation by infiniteTransition.animateColor(
        initialValue = Color(0x99FFDF00),
        targetValue = Color(0x99DAA520),
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(1000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = ""
    )

    return this.drawWithContent {
        drawContent()
        drawRoundRect(
            color = colorAnimation.copy(alpha = 0.5f),
            cornerRadius = CornerRadius(itemCornerRadius.toPx(), itemCornerRadius.toPx())
        )
    }
}


@Composable
fun GamePopUpMenu(
    isPlayer: Boolean,
    onPlayerEvent: (PlayerEvents) -> Unit,
    playerState: PlayerState?,

    onGameEvent: (GameEvents) -> Unit,
    serverStatus: ServerState = ServerState()
) {
    var showPopUpMenu by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = {
        showPopUpMenu = true
    }) {
        Icon(Icons.Default.Menu, contentDescription = "Menu")
    }

    if (showPopUpMenu) {
        Dialog(
            onDismissRequest = { showPopUpMenu = false },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (isPlayer) {
                        true -> {
                            //TODO: in future some settings, change view, etc.
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .weight(1f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item{
                                    if (serverStatus.serverType == ServerType.IS_TABLE){
                                        Button(
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = {
                                                showPopUpMenu = false
                                                onPlayerEvent(PlayerEvents.ChangeView)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondary
                                            ),
                                        ) {
                                            AutoSizeText(
                                                text = "Change view",
                                                style = TextStyle(
                                                    fontSize = 20.sp,
                                                )
                                            )
                                        }
                                    }

                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            showPopUpMenu = false
                                            onPlayerEvent(PlayerEvents.Leave)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        ),
                                    ) {
                                        AutoSizeText(
                                            text = UiTexts.StringResource(R.string.leave_game).asString(),
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        false -> {
                            //TODO: in future some settings, kicking people, etc.
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ){
                                    Box(modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (serverStatus.serverStatus == ServerStatusEnum.ADVERTISING)
                                                Color.Green
                                            else Color.Yellow
                                        )
                                    )
                                    AutoSizeText(
                                        text = UiTexts.StringResource(R.string.server_status).asString(),
                                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    )
                                }
                                AutoSizeText(
                                    text =
                                    if (serverStatus.serverStatus == ServerStatusEnum.ADVERTISING)
                                        UiTexts.StringResource(R.string.server_status_advertising).asString()
                                    else
                                        UiTexts.StringResource(R.string.server_status_active).asString(),
                                    style = TextStyle(fontSize = 20.sp),
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .weight(1f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item {
                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            onGameEvent(GameEvents.StopAdvertising)
                                        },
                                        enabled = serverStatus.serverStatus == ServerStatusEnum.ADVERTISING,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        ),
                                    ) {
                                        AutoSizeText(
                                            text = UiTexts.StringResource(R.string.stop_advertising)
                                                .asString(),
                                            style = TextStyle(
                                                fontSize = 20.sp
                                            )
                                        )
                                    }

                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            // TODO()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        ),
                                    ) {
                                        AutoSizeText(
                                            text = "PLAYERS",
                                            style = TextStyle(
                                                fontSize = 20.sp
                                            )
                                        )
                                    }

                                    Button(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = {
                                            showPopUpMenu = false
                                            onGameEvent(GameEvents.CloseGame)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        ),
                                    ) {
                                        AutoSizeText(
                                            text = UiTexts.StringResource(R.string.leave_game)
                                                .asString(),
                                            style = TextStyle(
                                                fontSize = 20.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row (
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ){
                        playerState?.let {
                            Button(
                                modifier = Modifier,
                                onClick = {
                                    showPopUpMenu = false
                                    onPlayerEvent(PlayerEvents.Ready)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                enabled = playerState.isReadyToPlay
                            ) {
                                AutoSizeText(
                                    text = UiTexts.StringResource(R.string.unready).asString(),
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                    )
                                )
                            }
                        }

                        Button(
                            modifier = Modifier,
                            onClick = { showPopUpMenu = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                        ) {
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.continue_game).asString(),
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ShowMoneyAnimated(
    amount: Int,
    isGameOver: Boolean,
    spinningDuration: Int,
    textStyle: TextStyle
) {
    val moneyAmount by animateIntAsState(targetValue = amount, animationSpec = tween(spinningDuration))
    if (isGameOver) {
        Text(
            text = moneyAmount.formatNumberToString(),
            style = textStyle
        )
    } else{
        var oldAmount by remember {
            mutableStateOf(amount)
        }
        SideEffect {
            oldAmount = amount
        }
        Row {
            val countString = amount.formatNumberToString()
            val oldCountString = oldAmount.formatNumberToString()
            for(i in countString.indices) {
                val oldChar = oldCountString.getOrNull(i)
                val newChar = countString[i]
                val char = if(oldChar == newChar) {
                    oldCountString[i]
                } else {
                    countString[i]
                }
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        slideInVertically { it } togetherWith slideOutVertically { -it }
                    }
                ) { char ->
                    Text(
                        text = char.toString(),
                        style = textStyle
                    )
                }
            }
        }
    }
}


@Composable
fun Modifier.animatedBorder(
    animate: Boolean,
    durationMillis: Int,
    colorStart: Color,
    colorStop: Color,
    borderPath: Path,
    vararg changeStateKeys: Any?
): Modifier {
    val animationTimer = remember {
        Animatable(
            initialValue = if (animate) 360f else 0f
        )
    }
    LaunchedEffect(*changeStateKeys) {
        animationTimer.snapTo(360f)

        animationTimer.animateTo(
            targetValue = if (!animate) 360f else 0f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            )
        )
    }

    val animatedColor by remember {
        derivedStateOf { lerp(colorStart, colorStop, animationTimer.value/360) }
    }

    // help of: https://github.com/SmartToolFactory/Jetpack-Compose-Tutorials/blob/master/Tutorial1-1Basics/src/main/java/com/smarttoolfactory/tutorial1_1basics/chapter6_graphics/Tutorial6_13BorderProgressTimer.kt
    val pathWithProgress by remember { mutableStateOf(Path()) }
    val pathMeasure by remember { mutableStateOf(PathMeasure()) }
    val path = borderPath
    return this
        .drawBehind {
            pathWithProgress.reset()

            pathMeasure.setPath(path, forceClosed = false)
            pathMeasure.getSegment(
                startDistance = 0f,
                stopDistance = pathMeasure.length * animationTimer.value/360,
                pathWithProgress,
                startWithMoveTo = true
            )

            drawPath(
                path = pathWithProgress,
                style = Stroke(
                    7.dp.toPx(),
                    cap = StrokeCap.Round,
                ),
                color = if (animate) animatedColor else Color.Transparent
            )
        }
}


@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
) {
    var textStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }
    Text(
        text = text,
        style = textStyle,
        maxLines = 4,
        softWrap = false,
        modifier = Modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                textStyle = textStyle.copy(fontSize = textStyle.fontSize * 0.9)
            } else {
                readyToDraw = true
            }
        }
    )
}