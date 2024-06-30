package com.dabi.partypoker.featureCore.views


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.model.data.endpointID
import com.dabi.partypoker.featureServer.model.data.GameState
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
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.weight(1f)
        )
        Text(text = text)
        Button(onClick = { onCancelRequest() }) {
            Text(text = "Cancel")
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
    navController: NavController,
    isPlayer: Boolean,
    onDismissRequest: () -> Unit,
    onLeaveRequest: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when(isPlayer){
                    true -> {
                        //TODO: in future some settings, etc.
                        Button(onClick = {
                            onDismissRequest()
                            onLeaveRequest()
                        }) {
                            Text(text = "Leave")
                        }
                    }
                    false -> {
                        //TODO: in future some settings, kicking people, etc.
                        Button(onClick = { /*TODO*/ }) {
                            Text(text = "Stop advertising")
                        }
                        Button(onClick = {
                            onDismissRequest()
                            onLeaveRequest()
                        }) {
                            Text(text = "Leave")
                        }
                    }
                }

                Row (
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End
                ){
                    Button(onClick = { onDismissRequest() }) {
                        Text(text = "Pokračovat")
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