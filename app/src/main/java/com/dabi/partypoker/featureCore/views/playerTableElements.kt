package com.dabi.partypoker.featureCore.views

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.ui.theme.textColor
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardType
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
        Box(
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
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ){
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RaiseSlider(
                        modifier = Modifier.weight(1f),
                        valueRange = playerActionsState.raiseAmount.toFloat() .. player.money.toFloat(),
                        bigBlindAmount = gameState.gameSettings.bigBlindAmount
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
                            text = UiTexts.StringResource(R.string.cancel).asString(),
                            fontSize = fontSize,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
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
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(6.dp),
        ) { ready ->
            Column {
                if(ready || gameState.gameReadyPlayers.containsKey(player.id)){
                    if (player.holeCards.isEmpty()){
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.game_wait).asString(),
                                style = TextStyle(
                                    fontSize = fontSize * 1.5f,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
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

                                var showPopup by remember { mutableStateOf(false) }
                                Icon(
                                    Icons.AutoMirrored.Default.Help,
                                    contentDescription = "Help",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = null,
                                            indication = null,
                                            onClick = {
                                                showPopup = true
                                            }
                                        )
                                        .padding(start = 10.dp, end = 5.dp)
                                        .size(fontSize.value.dp * 1.2f)
                                )
                                if (showPopup){
                                    HandInfoPopUp(
                                        modifier = Modifier
                                            .fillMaxWidth(0.4f)
                                            .fillMaxHeight(0.7f)
                                            .padding(8.dp),
                                        showPopup = { showPopup = it },
                                        fontSize = fontSize
                                    )
                                }

                                HandStrengthIndicator(
                                    cardsCombination = cardsCombination,
                                    indicatorHeight = with(density) { fontSize.toDp() * 1f }
                                )
                            } ?: run {
                                Spacer(modifier = Modifier.height(with(density) { fontSize.toDp() * 1f }))
                            }
                        }
                        Spacer(modifier = Modifier.weight(0.1f))

                        if (player.allIn){
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    text = UiTexts.StringResource(R.string.all_in).asString().uppercase(),
                                    fontSize = fontSize * 1.5f,
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
                        } else{
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
                                                (if (playerActionsState.callAmount >= player.money){
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
                                                if (raiseAmount >= player.money && raiseEnabled)
                                                    UiTexts.StringResource(R.string.all_in).asString().uppercase()
                                                else if (gameState.activeRaise == null)
                                                    UiTexts.StringResource(R.string.action_bet).asString().uppercase()
                                                else UiTexts.StringResource(R.string.action_raise).asString().uppercase(),
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
                        border = BorderStroke(
                            1.dp,
                            textColor.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(5.dp)
                    ) {
                        Text(
                            text = UiTexts.StringResource(R.string.action_ready).asString().uppercase(),
                            fontSize = fontSize * 1.5f,
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
            }
        }
    }
}


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
fun HandStrengthIndicator(
    cardsCombination: Pair<CardsCombination, List<Card>>,
    indicatorHeight: Dp
) {
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
            trackColor = MaterialTheme.colorScheme.surfaceBright,
            modifier = Modifier
                .height(indicatorHeight)
                .fillMaxWidth()
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()

                        val spaceBetween = size.width / 5
                        for (i in 1..4) {
                            drawLine(
                                color = textColor,
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
                    textColor,
                    RoundedCornerShape(5.dp)
                )
                .clip(RoundedCornerShape(5.dp))
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HandInfoPopUp(
    modifier: Modifier = Modifier,
    fontSize: TextUnit,
    showPopup: (Boolean) -> Unit
) {
    Popup(
        onDismissRequest = { showPopup(false) },
        popupPositionProvider = rememberPlainTooltipPositionProvider()
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = textColor
            )
        ){
            val texts = UiTexts.ArrayResource(R.array.cards_tooltip).asArray()
            val combinations = CardsUtils.handCombinations
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ){
                        AutoSizeText(
                            text = texts[0].uppercase(),
                            style = TextStyle(
                                fontSize = fontSize,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
                itemsIndexed(texts){index, text ->
                    if (index > 0){
                        val textRow = text.split(": ")
                        HorizontalDivider(modifier = Modifier.padding(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            AutoSizeText(
                                text = index.toString() + ". " + textRow[0] + ": ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                            ) {
                                combinations.getOrNull(index-1)?.let {
                                    for (pair in it){
                                        val card = pair.first
                                        val isVisible = pair.second

                                        CardBox(cardId = card.toImageId(), modifier = Modifier
                                            .weight(1f, false)
                                            .alpha(if (isVisible) 1f else 0.5f)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = textRow[1],
                                style = TextStyle(
                                    fontSize = fontSize,
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}