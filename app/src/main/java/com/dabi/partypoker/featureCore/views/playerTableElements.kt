package com.dabi.partypoker.featureCore.views

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.transform.Transformation
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featureCore.data.colors
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.utils.CardsCombination
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.evaluatePlayerCards
import com.dabi.partypoker.utils.formatNumberToString
import com.dabi.partypoker.utils.handStrength

@Composable
fun PlayerDrawItself(
    player: PlayerState,
    playerActionsState: PlayerActionsState,
    onPlayerEvent: (PlayerEvents) -> Unit,
    gameState: GameState,

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
                gameState = gameState,
                layoutDirection = PlayerLayoutDirection.BOTTOM,
                showCards = true
            )
        }

        var raiseButtonPos by remember { mutableStateOf(Offset.Zero) }
        var raiseButtonSize by remember { mutableStateOf(IntSize.Zero) }
        var raiseAmount by remember { mutableStateOf(playerActionsState.raiseAmount) }
        var raiseEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(player.isPlayingNow){
            raiseEnabled = false
        }
        if (raiseEnabled){
            Column(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = with(this) {
                                (this@BoxWithConstraints.maxWidth / 2 + (playerBoxSize.width + sizeConstant.width) / 2)
                                    .toPx()
                                    .toInt()
                            },
                            y = with(this) {
                                raiseButtonPos.y.toInt() - playerBoxSize.height
                                    .toPx()
                                    .toInt()
                            },
                        )
                    }
                    .width(this.maxWidth - (this.maxWidth / 2 + (playerBoxSize.width + sizeConstant.width) / 2))
                    .height(
                        with(density) {
                            tablePosition.y.toDp() + tableSize.height.toDp() - sizeConstant.height - (raiseButtonPos.y.toDp() - playerBoxSize.height)
                        }
                    )
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.playerButtonsColor2)
            ){
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    if (playerActionsState.raiseAmount.toFloat() > player.money.toFloat()){
                        Text(text = "Not enought money to raise")
                    } else{
                        RaiseSlider(
                            modifier = Modifier.weight(1f),
                            valueRange = playerActionsState.raiseAmount.toFloat() .. player.money.toFloat()
                        ){
                            raiseAmount = it
                        }
                        
                        Button(
                            onClick = {
                                raiseEnabled = false
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.2f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(0.dp)
                        ){
                            Text(
                                text = "CANCEL",
                                fontSize = fontSize,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                }
            }
        }

        Crossfade(
            targetState = player.isReadyToPlay,
            modifier = Modifier
                .size(
                    width = this.maxWidth - (this.maxWidth / 2 + (playerBoxSize.width + sizeConstant.width) / 2),
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
                .padding(6.dp),
        ) { ready ->
            Column {
                if(ready){
                    if (player.holeCards.isEmpty()){
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "Wait for game to start.",
                                fontSize = fontSize * 1.5f,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                ),
                            )
                        }
                    } else{
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ){
                            val cardsCombination = evaluatePlayerCards(
                                tableCards = gameState.cardsTable,
                                holeCards = player.holeCards
                            )
                            val id = if (cardsCombination.first == CardsCombination.NONE) null else CardsUtils.combinationsTranslationID[cardsCombination.first]
                            id?.let {
                                Text(
                                    text = UiTexts.StringResource(id).asString().uppercase(),
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
                                    val strength by remember(cardsCombination) { mutableStateOf(handStrength(cardsCombination)) }
                                    val animatedColor by remember(strength) {
                                        mutableStateOf(lerp(Color.Red, Color.Green, strength))
                                    }
                                    LinearProgressIndicator(
                                        progress = { strength },
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
                                                            end = Offset(
                                                                spaceBetween * i,
                                                                size.height
                                                            ),
                                                            strokeWidth = 1.dp.toPx()
                                                        )
                                                    }
                                                }
                                            }
                                            .border(
                                                1.dp,
                                                colors.calledMoneyColor,
                                                RoundedCornerShape(5.dp)
                                            )
                                            .clip(RoundedCornerShape(5.dp))
                                    )
                                }
                            } ?: run {
                                Spacer(modifier = Modifier.height(with(density) { fontSize.toDp() * 1f }))
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
                                    raiseEnabled = false
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
                                    if (playerActionsState.raiseAmount <= player.money){
                                        if (raiseEnabled){
                                            onPlayerEvent(PlayerEvents.Raise(raiseAmount))
                                            raiseEnabled = false
                                        } else{
                                            raiseEnabled = true
                                        }
                                    } else{ raiseEnabled = false }
                                },
                                enabled = player.isPlayingNow && playerActionsState.raiseAmount <= player.money,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .onGloballyPositioned {
                                        raiseButtonPos = it.positionInRoot()
                                        raiseButtonSize = it.size
                                    },
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ){
                                    val textStyle = TextStyle(
                                        fontSize = fontSize * 1.2f,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center,
                                        color = colors.calledMoneyColor,
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    )
                                    Text(
                                        text = UiTexts.StringResource(R.string.action_raise).asString().uppercase(),
                                        style = textStyle
                                    )
                                    if (raiseEnabled){
                                        ShowMoneyAnimated(
                                            raiseAmount,
                                            false,
                                            0,
                                            textStyle = textStyle
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    onPlayerEvent(PlayerEvents.Fold)
                                    raiseEnabled = false
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
                    }

                } else{
                    Button(
                        onClick = {
                            onPlayerEvent(PlayerEvents.Ready)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseSlider(
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
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
                        sliderState.value = (Math.round((sliderState.value - GameState().bigBlindAmount) / 10) * 10).toFloat()
                    }
                ),
            tint = colors.calledMoneyColor
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
                        .background(colors.calledMoneyColor),
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
                        sliderState.value = (Math.round((sliderState.value + GameState().bigBlindAmount) / 10) * 10).toFloat()
                    }
                ),
            tint = colors.calledMoneyColor
        )
    }
}