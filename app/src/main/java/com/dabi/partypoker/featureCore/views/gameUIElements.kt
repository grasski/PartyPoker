package com.dabi.partypoker.featureCore.views

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featureMenu.view.ViewPosition
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.MessageData
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.ui.theme.errorContainerDark
import com.dabi.partypoker.ui.theme.inversePrimaryDark
import com.dabi.partypoker.ui.theme.primaryDark
import com.dabi.partypoker.ui.theme.textColor
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.formatNumberToString


@Composable
fun GameTable(
    modifier: Modifier = Modifier,
    gameState: GameState,

    isServer: Boolean,
    onGameEvent: (GameEvents) -> Unit,

    tableInfo: (Offset, IntSize) -> Unit
) {
    var tablePosition by remember { mutableStateOf(Offset.Zero) }
    var tableSize by remember { mutableStateOf(IntSize.Zero) }

    var playerBoxSize by remember { mutableStateOf(DpSize.Zero) }
    var fontSize by remember { mutableStateOf(20.sp) }
    CalculatePlayerBoxSize(
        playerBoxSize = { playerBoxSize = it },
        fontSize = { fontSize = it }
    )
    val density = LocalDensity.current

    Column(
        modifier = modifier
            .onGloballyPositioned {
                tablePosition = it.positionInRoot()
                tableSize = it.size

                tableInfo(tablePosition, tableSize)
            }
            .clip(RoundedCornerShape(40.dp))
            .border(1.dp, Color.White, RoundedCornerShape(40.dp))
            .border(10.dp, Color.DarkGray, RoundedCornerShape(40.dp))
            .border(11.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(40.dp))
            .paint(
                rememberAsyncImagePainter(model = R.drawable.table),
                contentScale = ContentScale.Crop
            )
            .padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        if (gameState.started || !isServer){
            var sizeOfCardsSpace by remember { mutableStateOf(IntSize.Zero) }
            Row(
                modifier = Modifier
                    .onGloballyPositioned { sizeOfCardsSpace = it.size }
                    .fillMaxWidth(.60f)
                    .animatedBorder(
                        animate = gameState.gameOver,
                        durationMillis = gameState.gameSettings.gameOverTimerDurationMillis - 250,
                        colorStart = Color.Red,
                        colorStop = Color.Green,
                        borderPath = Path().apply {
                            reset()
                            lineTo(
                                x = with(density) {
                                    sizeOfCardsSpace.width
                                        .toDp()
                                        .toPx()
                                },
                                y = 0f
                            )
                        },
                        changeStateKeys = arrayOf(gameState.gameOver)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0..4){
                    val card = gameState.cardsTable.getOrNull(i)
                    card?.let {
                        val cardId = CardsUtils.cardIDs[card.type.name.lowercase() + "_" + card.value]
                        CardBox(
                            cardId,
                            Modifier
                                .padding(5.dp)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 1f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(5.dp)
                                .weight(1f)
                                .glowItem(3.dp, card in gameState.winningCards)
                        )
                    } ?: run {
                        CardBox(null, Modifier.weight(1f))
                    }
                }
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(playerBoxSize.height / 2)
                    .padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                ){
                Row(
                    modifier = Modifier
                        .width(playerBoxSize.width - playerBoxSize.width / 3)
                        .fillMaxHeight()
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.bank),
                        contentDescription = "bank",
                        modifier = Modifier
                            .size(playerBoxSize.height / 2)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        ShowMoneyAnimated(
                            amount = gameState.bank,
                            isGameOver = gameState.gameOver,
                            spinningDuration = gameState.gameSettings.gameOverTimerDurationMillis / 2,
                            textStyle = TextStyle(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = fontSize,
                                color = Color.White,
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                }

                MessageRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(0.35f)),
                    messages = gameState.messages,
                    fontSize = fontSize
                )
            }
        } else{
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    onClick = {
                        onGameEvent(GameEvents.StartGame)
                    },
                    modifier = Modifier
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp)
                ){
                    AutoSizeText(
                        text = UiTexts.StringResource(R.string.start_game).asString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                MessageRow(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(playerBoxSize.height / 2)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(0.35f)),
                    messages = gameState.messages,
                    fontSize = fontSize
                )
            }
        }
    }
}

@Composable
fun MessageRow(
    modifier: Modifier = Modifier,
    messages: List<MessageData>,
    fontSize: TextUnit
) {
    var showMessages by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .clickable(
                onClick = {
                    showMessages = true
                },
                interactionSource = null,
                indication = null
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        if (showMessages){
            MessagesView(
                onDismissRequest = { showMessages = false },
                messages = messages
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 5.dp)
        ){
            messages.lastOrNull()?.ShowMessage(
                maxLines = 1,
                fontSize = fontSize,
                textColor = Color.White
            )
        }

        Icon(
            Icons.Default.MoreVert,
            contentDescription = "",
            tint = Color.White
        )
    }
}


@Composable
fun CardBox(cardId: Int?, modifier: Modifier) {
    cardId?.let { id ->
        AsyncImage(
            model = id,
            contentDescription = "card",
            modifier = modifier
                .clip(RoundedCornerShape(3.dp))
        )
    } ?: run {
        Image(
            painter = painterResource(id = R.drawable.clubs_2),
            contentDescription = "card",
            modifier = modifier
                .padding(5.dp)
                .border(1.dp, Color.White.copy(alpha = 1f), RoundedCornerShape(6.dp))
                .padding(5.dp)
                .alpha(0f)
        )
    }
}


@Composable
fun PlayerBox(
    size: DpSize,
    fontSize: TextUnit,
    playerState: PlayerState?,
    gameState: GameState,

    showCards: Boolean = false,
    layoutDirection: PlayerLayoutDirection,
) {
    if (playerState == null){
        Box(
            modifier = Modifier
                .size(size)
        )
        return
    }

    val density = LocalDensity.current
    val circleSize by remember(size) {
        mutableStateOf(
            with(density) { (65 * with(density){size.height.toPx()} / 100).toDp() })    // 65% of total height
    }
    val boxSize by remember(size, circleSize) {
        mutableStateOf(
            DpSize(
                (size.width - circleSize/2),
                with(density) { (60 * with(density){size.height.toPx()} / 100).toDp() }    // 60% of total height
            )
        )
    }

    Box{
        when(layoutDirection){
            PlayerLayoutDirection.LEFT -> {
                VerticalPlayerItems(
                    playerState = playerState.copy(),
                    modifier = Modifier
                        .width(size.width * 65 / 100)
                        .align(Alignment.CenterEnd)
                        .offset(x = size.width * 65 / 100 - 5.dp)
                        .height((size.height / 3).times(3)),  // (size.height / 3).times(2)
                    fontSize = fontSize,
                    isLeft = true
                )
            }
            PlayerLayoutDirection.TOP -> {
                HorizontalPlayerItems(
                    playerState = playerState.copy(),
                    modifier = Modifier
                        .width(size.width)
                        .height(size.height / 3)
                        .align(Alignment.BottomCenter)
                        .offset(y = size.height / 3 + 1.dp),
                    fontSize = fontSize
                )
            }
            PlayerLayoutDirection.RIGHT -> {
                VerticalPlayerItems(
                    playerState = playerState.copy(),
                    modifier = Modifier
                        .width(size.width * 65 / 100)
                        .align(Alignment.CenterStart)
                        .offset(x = -(size.width * 65 / 100 - 5.dp))
                        .height((size.height / 3).times(3)),  // (size.height / 3).times(2)
                    fontSize = fontSize,
                    isLeft = false
                )
            }
            PlayerLayoutDirection.BOTTOM -> {
                HorizontalPlayerItems(
                    playerState = playerState.copy(),
                    modifier = Modifier
                        .width(size.width)
                        .height(size.height / 3)
                        .align(Alignment.TopCenter)
                        .offset(y = -size.height / 3),
                    fontSize = fontSize
                )
            }
        }

        Box(
            modifier = Modifier.alpha(if(playerState.isFolded) 0.6f else 1f)
        ){
            Crossfade(targetState = playerState.isReadyToPlay) { ready ->
                if (!ready && gameState.gameReadyPlayers.containsKey(playerState.id)){
                    val iconSize = circleSize.div(3)
                    Icon(
                        Icons.Default.TrackChanges,
                        "",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    if (layoutDirection == PlayerLayoutDirection.LEFT || layoutDirection == PlayerLayoutDirection.BOTTOM) {
                                        circleSize
                                            .div(2)
                                            .toPx()
                                            .toInt() - iconSize
                                            .toPx()
                                            .toInt() / 2
                                    } else {
                                        size.width
                                            .toPx()
                                            .toInt() - circleSize
                                            .div(2)
                                            .toPx()
                                            .toInt() - iconSize
                                            .toPx()
                                            .toInt() / 2
                                    },
                                    iconSize
                                        .toPx()
                                        .toInt()
                                        .div(2)
                                )
                            }
                            .size(iconSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer)
                    )
                }
                if (!ready && !gameState.gameReadyPlayers.containsKey(playerState.id)){
                    val labelSize = DpSize(
                        width = size.width - circleSize.div(2),
                        height = (size.height - boxSize.height) / 1.4f + 16.dp
                    )

                    Box(
                        modifier = Modifier
                            .size(labelSize)
                            .offset {
                                IntOffset(
                                    if (layoutDirection == PlayerLayoutDirection.LEFT || layoutDirection == PlayerLayoutDirection.BOTTOM) {
                                        circleSize
                                            .div(2)
                                            .toPx()
                                            .toInt()
                                    } else {
                                        0
                                    },
                                    ((size.height - boxSize.height) - labelSize.height + 16.dp)
                                        .toPx()
                                        .toInt()
                                )
                            }
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ){
                        Box(
                            modifier = Modifier
                                .offset(
                                    0.dp,
                                    (-8).dp
                                )
                        ){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.unready).asString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Cards row
            Row(
                modifier = Modifier
                    .size(
                        boxSize.copy(
                            width = boxSize.width - with(density) {
                                (circleSize / 2)
                                    .toPx()
                                    .toDp()
                            },
                            height = size.height
                        )
                    )
                    .padding(horizontal = 8.dp)
                    .align(if (layoutDirection == PlayerLayoutDirection.LEFT || layoutDirection == PlayerLayoutDirection.BOTTOM) Alignment.TopEnd else Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
            ){
                if (playerState.holeCards.isNotEmpty()){
                    for (i in 0..1){
                        val card = playerState.holeCards.getOrNull(i)
                        var cardID = R.drawable.card_back_side
                        if (showCards){
                            card?.let {
                                cardID = CardsUtils.cardIDs[card.type.name.lowercase() + "_" + card.value] ?: R.drawable.card_back_side
                            }
                        }

                        Image(
                            painter = painterResource(id = cardID),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .weight(1f)
                                .glowItem(
                                    3.dp,
                                    active = card in gameState.winningCards
                                )
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // Player box
            Box(
                modifier = Modifier
                    .size(size)
                    .drawBehind {
                        val path = Path().apply {
                            addRoundRect(
                                if (layoutDirection == PlayerLayoutDirection.LEFT || layoutDirection == PlayerLayoutDirection.BOTTOM) {
                                    RoundRect(
                                        rect = Rect(
                                            offset = Offset(
                                                (circleSize / 2).toPx(),
                                                size.height.toPx()
                                            ),
                                            size = Size(
                                                boxSize.width.toPx(),
                                                -boxSize.height.toPx()
                                            ),
                                        ),
                                        bottomRight = CornerRadius(16.dp.toPx()),
                                        topRight = CornerRadius(16.dp.toPx())
                                    )
                                } else {
                                    RoundRect(
                                        rect = Rect(
                                            offset = Offset(0f, size.height.toPx()),
                                            size = Size(
                                                boxSize.width.toPx(),
                                                -boxSize.height.toPx()
                                            ),
                                        ),
                                        bottomLeft = CornerRadius(16.dp.toPx()),
                                        topLeft = CornerRadius(16.dp.toPx())
                                    )
                                }
                            )
                        }
                        drawPath(
                            path,
                            color = inversePrimaryDark
                        )
                        drawPath(
                            path = path,
                            style = Stroke(
                                width = 3.5f,
                                cap = StrokeCap.Round
                            ),
                            color = primaryDark
                        )
                    }
            ){
                // Player icon box
                Box(
                    modifier = Modifier
                        .size(circleSize)
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
                                                circleSize.toPx(),
                                                circleSize.toPx()
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
                        .align(if (layoutDirection == PlayerLayoutDirection.LEFT || layoutDirection == PlayerLayoutDirection.BOTTOM) Alignment.BottomStart else Alignment.BottomEnd)
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
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else{
                            Image(
                                painter = painterResource(id = R.drawable.player_1),
                                contentDescription = "avatar",
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } ?: run {
                        Image(
                            painter = painterResource(id = R.drawable.player_1),
                            contentDescription = "avatar",
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Text box
                Box(
                    modifier = Modifier
                        .size(
                            boxSize.copy(
                                width = boxSize.width - with(density) {
                                    (circleSize / 2)
                                        .toPx()
                                        .toDp()
                                },
                            )
                        )
                        .align(if (layoutDirection == PlayerLayoutDirection.LEFT || layoutDirection == PlayerLayoutDirection.BOTTOM) Alignment.BottomEnd else Alignment.BottomStart),
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 3.dp, horizontal = 5.dp),
                        horizontalAlignment = if (layoutDirection == PlayerLayoutDirection.LEFT || layoutDirection == PlayerLayoutDirection.BOTTOM) Alignment.Start else Alignment.End
                    ){
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                            AutoSizeText(
                                text = playerState.nickname,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    ),
                                    fontSize = fontSize,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                ),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                            ShowMoneyAnimated(
                                amount = playerState.money,
                                isGameOver = gameState.gameOver,
                                spinningDuration = gameState.gameSettings.gameOverTimerDurationMillis / 2,
                                textStyle = TextStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    fontSize = fontSize * 1.2f,
                                    color = Color.Green,

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
}


@Composable
private fun VerticalPlayerItems(
    playerState: PlayerState,
    modifier: Modifier,
    fontSize: TextUnit,
    isLeft: Boolean
){
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
    ){
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 5.dp, vertical = 2.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(
                5.dp,
                if (isLeft) Alignment.Start else Alignment.End
            ),
            verticalAlignment = Alignment.CenterVertically
        ){
            if(playerState.isDealer){
                Image(
                    painter = painterResource(id = R.drawable.dealer_button),
                    contentDescription = "dealer_button",
                    modifier = Modifier.fillMaxHeight(0.85f)
                )
            }
            if(playerState.isSmallBlind){
                Image(
                    painter = painterResource(id = R.drawable.small_blind_button),
                    contentDescription = "dealer_button",
                    modifier = Modifier.fillMaxHeight(0.85f)
                )
            }
            if(playerState.isBigBlind){
                Image(
                    painter = painterResource(id = R.drawable.big_blind_button),
                    contentDescription = "dealer_button",
                    modifier = Modifier.fillMaxHeight(0.85f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp)
                .background(
                    if (playerState.called != 0) textColor else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 2.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ){
            if (playerState.called != 0){
                Image(
                    painter = painterResource(id = R.drawable.money_chip),
                    contentDescription = "money_chip",
                    modifier = Modifier.fillMaxHeight(0.7f)
                )
                Text(
                    text = playerState.called.formatNumberToString(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 2.dp)
                .background(
                    if (playerState.allIn) textColor else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .weight(1f),
            contentAlignment = Alignment.Center
        ){
            if (playerState.allIn){
                AutoSizeText(
                    text = UiTexts.StringResource(R.string.all_in).asString(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        color = Color.Black,
                        fontSize = fontSize
                    )
                )
            }
        }
    }
}


@Composable
private fun HorizontalPlayerItems(
    playerState: PlayerState,
    modifier: Modifier,
    fontSize: TextUnit
){
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ){
        if(playerState.isDealer){
            Image(
                painter = painterResource(id = R.drawable.dealer_button),
                contentDescription = "dealer_button",
                modifier = Modifier.fillMaxHeight(0.85f)
            )
        }
        if(playerState.isSmallBlind){
            Image(
                painter = painterResource(id = R.drawable.small_blind_button),
                contentDescription = "dealer_button",
                modifier = Modifier.fillMaxHeight(0.85f)
            )
        }
        if(playerState.isBigBlind){
            Image(
                painter = painterResource(id = R.drawable.big_blind_button),
                contentDescription = "dealer_button",
                modifier = Modifier.fillMaxHeight(0.85f)
            )
        }

        if (playerState.called != 0){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp)
                    .background(
                        if (playerState.called != 0) textColor else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 2.dp)
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ){

                Image(
                    painter = painterResource(id = R.drawable.money_chip),
                    contentDescription = "money_chip",
                    modifier = Modifier.fillMaxHeight(0.7f)
                )
                Box(
                    modifier = Modifier
                        .weight(1f, false)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ){
                    AutoSizeText(
                        text = playerState.called.formatNumberToString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            color = Color.Black,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                }
            }
        }
        if (playerState.allIn){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 1.dp)
                    .background(
                        textColor,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(2.dp)
                    .weight(0.8f),
                contentAlignment = Alignment.Center
            ){
                AutoSizeText(
                    text = UiTexts.StringResource(R.string.all_in).asString(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        color = Color.Black,
                        fontSize = fontSize
                    )
                )
            }
        }
    }
}


@Composable
fun DrawPlayersByPosition(
    players: Map<Int, PlayerState?>,
    gameState: GameState,
    myPosition: Int = 0,
    serverType: ServerType,
    tablePosition: Offset,
    tableSize: IntSize,
) {
    var playerBoxSize by remember { mutableStateOf(DpSize.Zero) }
    var fontSize by remember { mutableStateOf(20.sp) }
    CalculatePlayerBoxSize(
        playerBoxSize = { playerBoxSize = it },
        fontSize = { fontSize = it }
    )

    val density = LocalDensity.current
    val topLeftHorizontal = with(density) { DpOffset(
        x = tablePosition.x.toDp() - playerBoxSize.width / 2,
        y = tablePosition.y.toDp() - playerBoxSize.height / 2 - playerBoxSize.height / 3
    ) }
    val bottomLeftHorizontal = with(density) { DpOffset(
        x = tablePosition.x.toDp() - playerBoxSize.width / 2,
        y = tablePosition.y.toDp() + tableSize.height.toDp() - playerBoxSize.height / 2 + playerBoxSize.height / 3
    ) }


    val topLeftVertical = with(density) { DpOffset(
        x = tablePosition.x.toDp() - playerBoxSize.width / 2 - playerBoxSize.width / 5,
        y = tablePosition.y.toDp() + playerBoxSize.height / 2
    ) }
    val topRightVertical = with(density) { DpOffset(
        x = tablePosition.x.toDp() + tableSize.width.toDp() - playerBoxSize.width / 2 + playerBoxSize.width / 5,
        y = tablePosition.y.toDp() + playerBoxSize.height / 2
    ) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val totalPlayersHorizontal = 4
        val totalPlayersVertical = if (serverType == ServerType.IS_TABLE) 1 else 2

        val originalIndex = if (serverType == ServerType.IS_TABLE) 0 else (myPosition + 2)
        var seatIndex = originalIndex

        // Left side
        Column(
            modifier = Modifier
                .offset(topLeftVertical.x, topLeftVertical.y)
                .size(
                    width = playerBoxSize.width,
                    height = with(density) { tableSize.height.toDp() - playerBoxSize.height }
                ),
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            for (i in (seatIndex + totalPlayersVertical-1) downTo (seatIndex)){
                PlayerBox(
                    playerBoxSize,
                    fontSize = fontSize,
                    playerState = players[i % 10],
                    gameState = gameState,
                    layoutDirection = PlayerLayoutDirection.LEFT,
                    showCards = gameState.gameOver && players[i % 10]?.isFolded == false
                )

                seatIndex ++
            }
        }

        // Top side
        Row(
            modifier = Modifier
                .offset(topLeftHorizontal.x, topLeftHorizontal.y)
                .size(
                    width = with(density) { tableSize.width.toDp() + playerBoxSize.width },
                    height = playerBoxSize.height
                ),
            horizontalArrangement = Arrangement.spacedBy(playerBoxSize.width/4, Alignment.CenterHorizontally)
        ){
            for (i in seatIndex..<(seatIndex + totalPlayersHorizontal)){
                PlayerBox(
                    size = playerBoxSize,
                    fontSize = fontSize,
                    playerState = players[i % 10],
                    gameState = gameState,
                    layoutDirection = PlayerLayoutDirection.TOP,
                    showCards = gameState.gameOver && players[i % 10]?.isFolded == false
                )

                seatIndex ++
            }
        }

        // Right side
        Column(
            modifier = Modifier
                .offset(topRightVertical.x, topRightVertical.y)
                .size(
                    width = playerBoxSize.width,
                    height = with(density) { tableSize.height.toDp() - playerBoxSize.height }
                ),
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            for (i in (seatIndex)..<(seatIndex + totalPlayersVertical)){
                PlayerBox(
                    size = playerBoxSize,
                    fontSize = fontSize,
                    playerState = players[i % 10],
                    gameState = gameState,
                    layoutDirection = PlayerLayoutDirection.RIGHT,
                    showCards = gameState.gameOver && players[i % 10]?.isFolded == false
                )

                seatIndex ++
            }
        }

        // Bottom side
        Row(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (bottomLeftHorizontal.x + if (serverType == ServerType.IS_TABLE) 0.dp else playerBoxSize.width / 1.5f)
                            .toPx()
                            .toInt(),
                        bottomLeftHorizontal.y
                            .toPx()
                            .toInt()
                    )
                }
                .size(
                    width = with(density) { tableSize.width.toDp() + playerBoxSize.width },
                    height = playerBoxSize.height
                ),
            horizontalArrangement =
            if (serverType == ServerType.IS_TABLE)
                Arrangement.spacedBy(playerBoxSize.width/4, Alignment.CenterHorizontally)
            else
                Arrangement.Start
        ){
            if (serverType == ServerType.IS_TABLE){
                for (i in (seatIndex + totalPlayersHorizontal-1) downTo (seatIndex)){
                    PlayerBox(
                        size = playerBoxSize,
                        fontSize = fontSize,
                        playerState = players[i % 10],
                        gameState = gameState,
                        layoutDirection = PlayerLayoutDirection.BOTTOM,
                        showCards = gameState.gameOver && players[i % 10]?.isFolded == false
                    )
                    seatIndex ++
                }
            } else{
                PlayerBox(
                    size = playerBoxSize,
                    fontSize = fontSize,
                    playerState = players[(seatIndex + 1) % 10],
                    gameState = gameState,
                    layoutDirection = PlayerLayoutDirection.BOTTOM,
                    showCards = gameState.gameOver && players[(seatIndex + 1) % 10]?.isFolded == false
                )
            }
        }
    }
}


@Composable
fun MessagesView(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    messages: List<MessageData>
) {
    var playerBoxSize by remember { mutableStateOf(DpSize.Zero) }
    var fontSize by remember { mutableStateOf(20.sp) }
    CalculatePlayerBoxSize(
        playerBoxSize = { playerBoxSize = it },
        fontSize = { fontSize = it }
    )

    val lazyState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()){
            lazyState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth(0.6f)
                    .padding(24.dp),
                state = lazyState
            ) {
                item {
                    Text(
                        text = UiTexts.StringResource(R.string.message_center).asString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = fontSize * 2.5f
                    )
                }

                messages.forEachIndexed { index, msgData ->
                    item(index){
                        msgData.ShowMessage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            maxLines = 4,
                            fontSize = fontSize * 1.5f
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
}