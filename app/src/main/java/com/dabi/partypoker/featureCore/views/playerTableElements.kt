package com.dabi.partypoker.featureCore.views

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featureCore.data.colors
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardType
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.formatNumberToString


@Composable
fun PlayerDrawItself(
    player: PlayerState,
    playerActionsState: PlayerActionsState,
    onPlayerEvent: (PlayerEvents) -> Unit,

    tablePosition: Offset,
    tableSize: IntSize,
) {
    var playerBoxSize by remember { mutableStateOf(DpSize.Zero) }
    var fontSize by remember { mutableStateOf(20.sp) }
    CalculatePlayerBoxSize(
        playerBoxSize = { playerBoxSize = it },
        fontSize = { fontSize = it }
    )

    val sizeConstant = playerBoxSize / 4
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (this@BoxWithConstraints.maxWidth / 2 - (playerBoxSize.width + sizeConstant.width) / 2)
                            .toPx()
                            .toInt(),
                        y = (tablePosition.y + tableSize.height - sizeConstant.height.toPx()).toInt(),
                    )
                }
                .size(
                    width = playerBoxSize.width + sizeConstant.width,
                    height = playerBoxSize.height + sizeConstant.height
                )
        ) {
            PlayerBox(
                size = playerBoxSize + sizeConstant,
                fontSize = (fontSize.value + (fontSize/4).value).sp,
                playerState = player,
                layoutDirection = PlayerLayoutDirection.BOTTOM,
                showCards = true
            )
        }

        Log.e("","PLAYER READY: " + player.isReadyToPlay)

        Crossfade(
            targetState = player.isReadyToPlay
        ) { ready ->
            Column(
                modifier = Modifier
                    .size(
                        width = this@BoxWithConstraints.maxWidth - (this@BoxWithConstraints.maxWidth / 2 + (playerBoxSize.width + sizeConstant.width) / 2),
                        height = playerBoxSize.height + sizeConstant.height
                    )
                    .offset {
                        IntOffset(
                            x = (this@BoxWithConstraints.maxWidth / 2 + (playerBoxSize.width + sizeConstant.width) / 2)
                                .toPx()
                                .toInt(),
                            y = (tablePosition.y + tableSize.height - sizeConstant.height.toPx()).toInt()
                        )
                    }
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.playerButtonsColor2)
                    .padding(5.dp),
                horizontalAlignment = Alignment.Start
            ){
                if (ready) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ){
                        Text(
                            text = "Full house".uppercase(),
                            fontSize = fontSize,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                        Icon(
                            Icons.AutoMirrored.Default.Help,
                            contentDescription = "Help",
                            tint = Color.White,
                            modifier = Modifier
                                .clickable(
                                    interactionSource = null,
                                    indication = null,
                                    onClick = {
                                        // TODO()
                                    }
                                )
                                .padding(start = 10.dp, end = 5.dp)
                                .size(fontSize.value.dp * 1.2f)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ){
                            val strength = 30f
                            val animatedColor by remember {
                                derivedStateOf { lerp(Color.Red, Color.Green, strength / 100) }
                            }
                            LinearProgressIndicator(
                                progress = { strength / 100 },
                                color = animatedColor,
                                trackColor = colors.playerButtonsColor,
                                modifier = Modifier
                                    .height(with(density) { fontSize.toDp() * 1f })
                                    .fillMaxWidth()
                                    .drawWithCache {
                                        onDrawWithContent {
                                            drawContent()

                                            val spaceBetween = size.width / 5
                                            for (i in 1..4) {
                                                drawLine(
                                                    color = colors.calledMoneyColor,
                                                    start = Offset(spaceBetween * i, 0f),
                                                    end = Offset(spaceBetween * i, size.height),
                                                    strokeWidth = 1.dp.toPx()
                                                )
                                            }
                                        }
                                    }
                                    .border(1.dp, colors.calledMoneyColor, RoundedCornerShape(5.dp))
                                    .clip(RoundedCornerShape(5.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(0.1f))
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                    ){
                        Button(
                            onClick = {
                                if (playerActionsState.canCheck){
                                    onPlayerEvent(PlayerEvents.Check)
                                } else{
                                    onPlayerEvent(PlayerEvents.Call(playerActionsState.callAmount))
                                }
                            },
                            enabled = player.isPlayingNow,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonColor,
                                contentColor = colors.calledMoneyColor
                            ),
                            border = BorderStroke(
                                1.dp,
                                colors.calledMoneyColor.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text =
                                if (playerActionsState.canCheck)
                                    UiTexts.StringResource(R.string.action_check).asString().uppercase()
                                else
                                    UiTexts.StringResource(R.string.action_call).asString().uppercase() + "\n" + playerActionsState.callAmount.formatNumberToString(),
                                fontSize = fontSize * 1.2f,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = colors.calledMoneyColor,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                ),
                            )
                        }

                        Button(
                            onClick = {
                                // TODO
                            },
                            enabled = player.isPlayingNow,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonColor,
                                contentColor = colors.calledMoneyColor
                            ),
                            border = BorderStroke(
                                1.dp,
                                colors.calledMoneyColor.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = UiTexts.StringResource(R.string.action_raise).asString().uppercase(),
                                fontSize = fontSize * 1.2f,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = colors.calledMoneyColor,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }
                        Button(
                            onClick = {
                                onPlayerEvent(PlayerEvents.Fold)
                            },
                            enabled = player.isPlayingNow,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonColor,
                                contentColor = colors.calledMoneyColor
                            ),
                            border = BorderStroke(
                                1.dp,
                                colors.calledMoneyColor.copy(alpha = 0.5f)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = UiTexts.StringResource(R.string.action_fold).asString().uppercase(),
                                fontSize = fontSize * 1.2f,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = colors.calledMoneyColor,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }
                    }
                } else{
                    Button(
                        onClick = {
                            onPlayerEvent(PlayerEvents.Ready)
                        },
                        modifier = Modifier
                            .fillMaxSize(),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.buttonColor,
                            contentColor = colors.calledMoneyColor
                        ),
                        border = BorderStroke(
                            1.dp,
                            colors.calledMoneyColor.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(5.dp)
                    ) {
                        Text(
                            text = UiTexts.StringResource(R.string.action_ready).asString().uppercase(),
                            fontSize = fontSize * 1.5f,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = colors.calledMoneyColor,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}
