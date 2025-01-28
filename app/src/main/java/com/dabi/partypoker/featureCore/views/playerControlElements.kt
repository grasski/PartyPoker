package com.dabi.partypoker.featureCore.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.dabi.easylocalgame.textUtils.UiTexts
import com.dabi.partypoker.R
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featurePlayer.model.data.PlayerState
import com.dabi.partypoker.featurePlayer.viewmodel.PlayerEvents
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.ui.theme.textColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseSlider(
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    bigBlindAmount: Int,
    valueF: (Int) -> Unit
) {
    val sliderState by remember { mutableStateOf(
        SliderState(
            valueRange = valueRange,
            value = valueRange.start
        )
    ) }
    valueF(sliderState.value.toInt())

    var sliderSize by remember { mutableStateOf(IntSize.Zero) }
    Row(
        modifier = modifier
            .onGloballyPositioned { sliderSize = it.size },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
    ){
        Icon(
            Icons.Filled.Remove,
            "",
            modifier = Modifier
                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = {
                        sliderState.value =
                            (Math.round((sliderState.value - bigBlindAmount) / 10) * 10).toFloat()
                    }
                ),
            tint = textColor
        )

        Slider(
            state = sliderState,
            modifier = Modifier.weight(1f),
            thumb = {
                Row(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(textColor),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        "",
                        tint = Color.White
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        "",
                        tint = Color.White
                    )
                }
            },
            track = {
                HorizontalDivider(
                    modifier = Modifier
                        .background(Color.Yellow)
                )
            }
        )

        Icon(
            Icons.Filled.Add,
            "",
            modifier = Modifier
                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = {
                        sliderState.value =
                            (Math.round((sliderState.value + bigBlindAmount) / 10) * 10).toFloat()
                    }
                ),
            tint = textColor
        )
    }
}


@Composable
fun CheckCallButton(
    modifier: Modifier = Modifier,
    gameState: GameState,
    playerState: PlayerState,
    playerActionsState: PlayerActionsState,
    fontSize: TextUnit,

    onPlayerEvent: (PlayerEvents) -> Unit,
    clicked: () -> Unit
) {
    Button(
        onClick = {
            if (playerActionsState.canCheck){
                onPlayerEvent(PlayerEvents.Check)
            } else{
                onPlayerEvent(PlayerEvents.Call(playerActionsState.callAmount))
            }

            clicked()
        },
        enabled = playerState.isPlayingNow && !gameState.completeAllIn,
        modifier = modifier,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = textColor,
        ),
        border = BorderStroke(
            1.dp,
            textColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text =
            if (playerActionsState.canCheck){
                UiTexts.StringResource(R.string.action_check).asString().uppercase()
            } else{
                (if (playerActionsState.callAmount >= playerState.money){
                    UiTexts.StringResource(R.string.all_in).asString()
                        .uppercase()
                } else{
                    UiTexts.StringResource(R.string.action_call)
                        .asString().uppercase()
                }) + "\n" + playerActionsState.callAmount.formatNumberToString()
            },
            fontSize = fontSize * 1.2f,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = textColor,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
        )
    }
}

@Composable
fun RaiseButton(
    modifier: Modifier = Modifier,
    gameState: GameState,
    playerState: PlayerState,
    playerActionsState: PlayerActionsState,
    fontSize: TextUnit,

    onPlayerEvent: (PlayerEvents) -> Unit,

    raiseAmount: Int,
    raiserEnabled: Boolean,
    onClick: (Boolean) -> Unit,
) {
    Button(
        onClick = {
            if (playerActionsState.raiseAmount >= playerState.money){
                onPlayerEvent(PlayerEvents.Raise(playerState.money))
                onClick(false)
            } else{
                if (raiserEnabled){
                    onPlayerEvent(PlayerEvents.Raise(raiseAmount))
                    onClick(false)
                } else{
                    onClick(true)
                }
            }
        },
        enabled = playerState.isPlayingNow && playerActionsState.callAmount < playerState.money && !gameState.completeAllIn,
        modifier = modifier,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = textColor,
        ),
        border = BorderStroke(
            1.dp,
            textColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            val textStyle = TextStyle(
                fontSize = fontSize * 1.2f,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = textColor,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
            Text(
                text =
                if ((raiseAmount >= playerState.money && raiserEnabled) || (playerActionsState.raiseAmount >= playerState.money && playerActionsState.callAmount < playerState.money))
                    UiTexts.StringResource(R.string.all_in).asString().uppercase()
                else if (gameState.activeRaise == null)
                    UiTexts.StringResource(R.string.action_bet).asString().uppercase()
                else UiTexts.StringResource(R.string.action_raise).asString().uppercase(),
                style = textStyle
            )
            if (raiserEnabled || (playerActionsState.raiseAmount >= playerState.money && playerActionsState.callAmount < playerState.money)){
                ShowMoneyAnimated(
                    if (playerActionsState.raiseAmount >= playerState.money) playerState.money else raiseAmount,
                    false,
                    0,
                    textStyle = textStyle
                )
            }
        }
    }
}

@Composable
fun FoldButton(
    modifier: Modifier = Modifier,
    player: PlayerState,
    gameState: GameState,
    fontSize: TextUnit,

    onPlayerEvent: (PlayerEvents) -> Unit,
    onClick: () -> Unit
) {
    Button(
        onClick = {
            onPlayerEvent(PlayerEvents.Fold)
            onClick()
        },
        enabled = player.isPlayingNow && !gameState.completeAllIn,
        modifier = modifier,
        shape = RoundedCornerShape(5.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = textColor,
        ),
        border = BorderStroke(
            1.dp,
            textColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = UiTexts.StringResource(R.string.action_fold).asString().uppercase(),
            fontSize = fontSize * 1.2f,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = textColor,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}


@Composable
fun GetReadyButton(
    modifier: Modifier = Modifier,

    onPlayerEvent: (PlayerEvents) -> Unit
) {
    Button(
        onClick = {
            onPlayerEvent(PlayerEvents.Ready)
        },
        modifier = modifier,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(
            1.dp,
            textColor.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(5.dp)
    ) {
        AutoSizeText(
            text = UiTexts.StringResource(R.string.action_ready).asString().uppercase(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = textColor,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}