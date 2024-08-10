package com.dabi.partypoker.featureClient.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featureCore.views.AutoSizeText
import com.dabi.partypoker.featureCore.views.CalculatePlayerBoxSize
import com.dabi.partypoker.featureCore.views.CardBox
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.featureCore.views.HandStrengthIndicator
import com.dabi.partypoker.featureCore.views.PlayerDrawItself
import com.dabi.partypoker.featureCore.views.RaiseSlider
import com.dabi.partypoker.featureCore.views.ShowMoneyAnimated
import com.dabi.partypoker.featureCore.views.animatedBorder
import com.dabi.partypoker.featureCore.views.glowItem
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.ui.theme.textColor
import com.dabi.partypoker.utils.CardsCombination
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.evaluatePlayerCards
import com.dabi.partypoker.utils.formatNumberToString
import com.dabi.partypoker.utils.handStrength


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun PlayerViewPrivate(
    playerState: PlayerState,
    playerActionsState: PlayerActionsState,
    onPlayerEvent: (PlayerEvents) -> Unit,

    gameState: GameState
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    GamePopUpMenu(
        isPlayer = true,
        onPlayerEvent = onPlayerEvent,
        playerState = playerState,

        onGameEvent = {},
    )

    val density = LocalDensity.current

    val circleSize = 150.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight(0.25f)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(with(density){ this@BoxWithConstraints.constraints.maxHeight.toDp() })
                        .animatedBorder(
                            animate = playerState.isPlayingNow,
                            durationMillis = gameState.gameSettings.playerTimerDurationMillis - 250,
                            colorStart = Color.Red,
                            colorStop = Color.Green,
                            borderPath = Path().apply {
                                addArc(
                                    oval = Rect(
                                        offset = Offset.Zero,
                                        size = with(density) {
                                            Size(
                                                constraints.minHeight.toDp().toPx(),
                                                constraints.minHeight.toDp().toPx()
                                            )
                                        }),
                                    startAngleDegrees = -90f,
                                    sweepAngleDegrees = -360f,
                                )
                            },
                            changeStateKeys = arrayOf(
                                playerState.isPlayingNow,
                                gameState.round,
                                gameState.games
                            )
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape)
                ){
                    playerState.avatarId?.let { id ->
                        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(id))
                        val progress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = 1,
                            restartOnPlay = false, isPlaying = false
                        )

                        if (composition != null){
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier
                                    .size(with(density) { this@BoxWithConstraints.constraints.maxHeight.toDp() }),
                                contentScale = ContentScale.Crop
                            )
                        } else{
                            Image(
                                painter = painterResource(id = R.drawable.player_1),
                                contentDescription = "avatar",
                                modifier = Modifier
                                    .size(with(density) { this@BoxWithConstraints.constraints.maxHeight.toDp() }),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } ?: run {
                        Image(
                            painter = painterResource(id = R.drawable.player_1),
                            contentDescription = "avatar",
                            modifier = Modifier.size(with(density) { this@BoxWithConstraints.constraints.maxHeight.toDp() }),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.inversePrimary),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AutoSizeText(text = playerState.nickname, style = MaterialTheme.typography.titleLarge)
                HorizontalDivider(modifier = Modifier.fillMaxWidth(0.9f))
                AutoSizeText(text = playerState.money.formatNumberToString(), style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.weight(1f))


        val fontSize = with(density) { 17.dp.toSp() }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ){
            val cardsCombination = evaluatePlayerCards(
                tableCards = gameState.cardsTable,
                holeCards = playerState.holeCards
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
                    Popup(
                        onDismissRequest = { showPopup = false },
                        popupPositionProvider = rememberPlainTooltipPositionProvider()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .fillMaxHeight(0.5f)
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = textColor
                            )
                        ){
                            val texts = UiTexts.ArrayResource(R.array.cards_tooltip).asArray()
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                stickyHeader {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceContainer)
                                            .padding(bottom = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ){
                                        Text(
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
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 5.dp),
                                        ){
                                            Text(
                                                text = textRow[0] + ": ",
                                                style = TextStyle(
                                                    fontSize = fontSize,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            )
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

                HandStrengthIndicator(
                    cardsCombination = cardsCombination,
                    indicatorHeight = with(density) { fontSize.toDp() * 1f }
                )
            } ?: run {
                Spacer(modifier = Modifier.height(with(density) { fontSize.toDp() * 1f }))
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            if (playerState.holeCards.isNotEmpty()){
                for (i in 0..1){
                    val card = playerState.holeCards.getOrNull(i)
                    var cardID = R.drawable.gray_back
                    card?.let {
                        cardID = CardsUtils.cardIDs[card.type.name.lowercase() + "_" + card.value] ?: R.drawable.gray_back
                    }

                    Image(
                        painter = painterResource(id = cardID),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .weight(1f, false)
                            .glowItem(
                                3.dp,
                                active = card in gameState.winningCards
                            )
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        var raiseButtonPos by remember { mutableStateOf(Offset.Zero) }
        var raiseButtonSize by remember { mutableStateOf(IntSize.Zero) }
        var raiseAmount by remember { mutableStateOf(playerActionsState.raiseAmount) }
        var raiseEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(playerState.isPlayingNow){
            raiseEnabled = false
        }
        Crossfade(
            targetState = raiseEnabled,
            modifier = Modifier
                .height(40.dp)
        ) { enabled ->
            if (enabled){
                Column(
                    modifier = Modifier
                        .height(40.dp)
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
                            valueRange = playerActionsState.raiseAmount.toFloat() .. playerState.money.toFloat(),
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
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.cancel).asString(),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
        Crossfade(
            targetState = playerState.isReadyToPlay,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(6.dp),
        ) { ready ->
            if (ready){
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
                        enabled = playerState.isPlayingNow,
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
                        AutoSizeText(
                            text = if (playerActionsState.canCheck)
                                UiTexts.StringResource(R.string.action_check).asString().uppercase()
                            else
                                UiTexts.StringResource(R.string.action_call).asString().uppercase() + "\n" + playerActionsState.callAmount.formatNumberToString(),
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

                    Button(
                        onClick = {
                            if (playerActionsState.raiseAmount <= playerState.money){
                                if (raiseEnabled){
                                    onPlayerEvent(PlayerEvents.Raise(raiseAmount))
                                    raiseEnabled = false
                                } else{
                                    raiseEnabled = true
                                }
                            } else{ raiseEnabled = false }
                        },
                        enabled = playerState.isPlayingNow && playerActionsState.raiseAmount <= playerState.money,
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
                                fontSize = 15.sp * 1.2f,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = textColor,
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
                        enabled = playerState.isPlayingNow,
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
                        AutoSizeText(
                            text = UiTexts.StringResource(R.string.action_fold).asString().uppercase(),
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
        }
    }
}