package com.dabi.partypoker.featurePlayer.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dabi.easylocalgame.composeUtils.UiTexts
import com.dabi.partypoker.R
import com.dabi.partypoker.featureCore.data.PlayerActionsState
import com.dabi.partypoker.featureCore.viewModel.PlayerSettingsViewModel
import com.dabi.partypoker.featureCore.views.AutoSizeText
import com.dabi.partypoker.featureCore.views.CheckCallButton
import com.dabi.partypoker.featureCore.views.FoldButton
import com.dabi.partypoker.featureCore.views.GamePopUpMenu
import com.dabi.partypoker.featureCore.views.GetReadyButton
import com.dabi.partypoker.featureCore.views.HandInfoPopUp
import com.dabi.partypoker.featureCore.views.HandStrengthIndicator
import com.dabi.partypoker.featureCore.views.MessageRow
import com.dabi.partypoker.featureCore.views.RaiseButton
import com.dabi.partypoker.featureCore.views.RaiseSlider
import com.dabi.partypoker.featureCore.views.ShowMoneyAnimated
import com.dabi.partypoker.featureCore.views.animatedBorder
import com.dabi.partypoker.featureCore.views.glowItem
import com.dabi.partypoker.featurePlayer.model.data.PlayerState
import com.dabi.partypoker.featurePlayer.viewmodel.PlayerEvents
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.ui.theme.textColor
import com.dabi.partypoker.utils.CardsCombination
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.evaluatePlayerCards


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


    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    val playerSettingsVM = hiltViewModel<PlayerSettingsViewModel>()
    val playerSettingsState by playerSettingsVM.playerSettingsState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = playerState.isPlayingNow) {
        if (playerSettingsState.vibration && playerState.isPlayingNow){
            if (vibrator.hasVibrator()){
                vibrator.vibrate(
                    VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        }
    }


    GamePopUpMenu(
        isPlayer = true,
        onPlayerEvent = onPlayerEvent,
        playerState = playerState,

        onGameEvent = {},
    )


    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ){
        Column(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var avatarSize by remember { mutableStateOf(DpSize.Zero) }
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.25f)
                    .onGloballyPositioned {
                        with(density) {
                            avatarSize = DpSize(
                                it.size.width.toDp(),
                                it.size.height.toDp()
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize.height)
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
                                                avatarSize.height.toPx(),
                                                avatarSize.height.toPx()
                                            )
                                        }),
                                    startAngleDegrees = -90f,
                                    sweepAngleDegrees = -360f,
                                )
                            },
                            borderSize = 7.dp,
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
                                    .size(avatarSize.height),
                                contentScale = ContentScale.Crop
                            )
                        } else{
                            Image(
                                painter = painterResource(id = R.drawable.player_1),
                                contentDescription = "avatar",
                                modifier = Modifier
                                    .size(avatarSize.height),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } ?: run {
                        Image(
                            painter = painterResource(id = R.drawable.player_1),
                            contentDescription = "avatar",
                            modifier = Modifier.size(avatarSize.height),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(avatarSize.width)
                    .height(70.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.inversePrimary),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AutoSizeText(text = playerState.nickname, style = MaterialTheme.typography.titleLarge)
                HorizontalDivider(modifier = Modifier.fillMaxWidth(0.9f))
                ShowMoneyAnimated(
                    amount = playerState.money,
                    isGameOver = gameState.gameOver,
                    spinningDuration = gameState.gameSettings.gameOverTimerDurationMillis / 2,
                    textStyle = MaterialTheme.typography.titleMedium
                )
            }
        }
        val fontSize = with(density) { 17.dp.toSp() }

        MessageRow(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .height(40.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(0.35f)),
            messages = gameState.messages,
            fontSize = fontSize
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(10000f)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var cardsWidth by remember { mutableStateOf(0.dp) }
            Row(
                modifier = Modifier
                    .width(cardsWidth),
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
                        HandInfoPopUp(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .fillMaxHeight(0.5f)
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

            var showCards by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .onGloballyPositioned {
                        with(density) {
                            cardsWidth = it.size.width.toDp()
                        }
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()

                                if (event.type == PointerEventType.Press){
                                    showCards = true
                                }
                                if (event.type == PointerEventType.Release){
                                    showCards = false
                                }
                            }
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                if (playerState.holeCards.isNotEmpty()){
                    for (i in 0..1){
                        val card = playerState.holeCards.getOrNull(i)
                        var cardID = R.drawable.card_back_side
                        if (showCards || gameState.gameOver){
                            card?.let {
                                cardID = CardsUtils.cardIDs[card.type.name.lowercase() + "_" + card.value] ?: R.drawable.card_back_side
                            }
                        }

                        val painter = painterResource(cardID)
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .weight(1f, false)
                                .aspectRatio(painter.intrinsicSize.width / painter.intrinsicSize.height)
                                .glowItem(
                                    3.dp,
                                    active = card in gameState.winningCards
                                )
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.weight(1f))
        var raiseButtonPos by remember { mutableStateOf(Offset.Zero) }
        var raiseButtonSize by remember { mutableStateOf(IntSize.Zero) }
        var raiseAmount by remember { mutableStateOf(playerActionsState.raiseAmount) }
        var raiserEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(playerState.isPlayingNow){
            raiserEnabled = false
        }
        Crossfade(
            targetState = raiserEnabled,
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
                                raiserEnabled = false
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
            if(ready || gameState.gameReadyPlayers.containsKey(playerState.id)){
                if (playerState.allIn){
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
                        CheckCallButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            gameState = gameState,
                            playerState = playerState,
                            playerActionsState = playerActionsState,
                            fontSize = fontSize,
                            onPlayerEvent = onPlayerEvent,
                            clicked = { raiserEnabled = false }
                        )

                        RaiseButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .onGloballyPositioned {
                                    raiseButtonPos = it.positionInRoot()
                                    raiseButtonSize = it.size
                                },
                            gameState = gameState,
                            playerState = playerState,
                            playerActionsState = playerActionsState,
                            fontSize = fontSize,
                            onPlayerEvent = onPlayerEvent,
                            raiseAmount = raiseAmount,
                            raiserEnabled = raiserEnabled,
                            onClick = { raiserEnabled = it }
                        )

                        FoldButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            player = playerState,
                            gameState = gameState,
                            fontSize = fontSize,
                            onPlayerEvent = onPlayerEvent,
                            onClick = { raiserEnabled = false }
                        )
                    }
                }
            } else{
                GetReadyButton(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    onPlayerEvent = onPlayerEvent
                )
            }
        }
    }
}