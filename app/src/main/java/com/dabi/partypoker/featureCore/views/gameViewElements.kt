package com.dabi.partypoker.featureCore.views

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.window.core.layout.WindowHeightSizeClass
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featureCore.data.colors
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.formatNumberToString


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
fun GameTable(
    modifier: Modifier = Modifier,
    gameState: GameState,

    onGameEvent: (GameEvents) -> Unit
) {
    var tablePosition by remember { mutableStateOf(Offset.Zero) }
    var tableSize by remember { mutableStateOf(IntSize.Zero) }

    var playerBoxSize by remember { mutableStateOf(DpSize.Zero) }
    var fontSize by remember { mutableStateOf(20.sp) }

    Column(
        modifier = modifier
            .onGloballyPositioned {
                tablePosition = it.positionInRoot()
                tableSize = it.size
            }
            .clip(RoundedCornerShape(40.dp))
            .border(1.dp, Color.White, RoundedCornerShape(40.dp))
            .border(10.dp, Color.DarkGray, RoundedCornerShape(40.dp))
            .border(11.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(40.dp))
            .paint(
                rememberAsyncImagePainter(model = R.drawable.table),
                contentScale = ContentScale.FillBounds
            )
            .padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        if (gameState.started){
            Row(
                modifier = Modifier
                    .fillMaxWidth(.60f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0..4){
                    val card = gameState.cardsTable.getOrNull(i)
                    card?.let {
                        val cardId = CardsUtils.cardIDs[card.type.name.lowercase() + "_" + card.value]
                        CardBox(cardId, Modifier.weight(1f))
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

                ){
                Row(
                    modifier = Modifier
                        .width(playerBoxSize.width),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.bank),
                        contentDescription = "bank",
                    )
                    Text(
                        text = gameState.bank.formatNumberToString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        maxLines = 1,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(5.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(0.35f)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                    ){
                        Text(
                            text = gameState.messages.lastOrNull()?.asString() ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = fontSize,
                            color = Color.White,
                        )
                    }

                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = {
                                //TODO()
                            }),
                        tint = Color.White
                    )
                }
            }
        } else{
            Button(onClick = {
                onGameEvent(GameEvents.StartGame)
            }) {
                Text(text = "Start the game")
            }
        }
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    LaunchedEffect(Unit) {
        when (windowSizeClass.windowHeightSizeClass) {
            WindowHeightSizeClass.COMPACT -> {
                playerBoxSize = DpSize(140.dp, 70.dp)
                fontSize = 13.sp
            }
            WindowHeightSizeClass.MEDIUM -> {
                playerBoxSize = DpSize(200.dp, 100.dp)
                fontSize = 21.sp
            }
            WindowHeightSizeClass.EXPANDED -> {
                playerBoxSize = DpSize(280.dp, 250.dp)
                fontSize = 27.sp
            }
        }
    }
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
    ){
        val totalPlayersHorizontal by remember(tablePosition) {
            mutableFloatStateOf(
                with(density) {
//                    floor((tableSize.width.toDp() - 30.dp) / boxSize.width).takeIf { !it.isNaN() && !it.isInfinite() } ?: 1f
                    4f
                }
            )
        }
        val totalPlayersVertical by remember(tablePosition) {
            mutableFloatStateOf(
                with(density){
//                    floor((tableSize.height.toDp() - (boxSize.height.times(2)) - 30.dp) / boxSize.height).takeIf { !it.isNaN() && !it.isInfinite() } ?: 1f
                    1f
                }
            )
        }


        var seatIndex = 0
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
            for (i in totalPlayersVertical.toInt() - 1 downTo 0){
                gameState.players.values.toList().getOrNull(i)?.let {
                    PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = it, layoutDirection = PlayerLayoutDirection.LEFT)
                    seatIndex ++
                }
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
            for (i in seatIndex..<totalPlayersHorizontal.toInt() + totalPlayersVertical.toInt()){
                gameState.players.values.toList().getOrNull(i)?.let {
                    PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = it, layoutDirection = PlayerLayoutDirection.TOP)
                    seatIndex = i + 1
                }
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
            for (i in seatIndex..<(2*totalPlayersVertical.toInt() + totalPlayersHorizontal.toInt())){
                gameState.players.values.toList().getOrNull(i)?.let {
                    PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = it, layoutDirection = PlayerLayoutDirection.RIGHT)
                    seatIndex = i + 1
                }
            }
        }

        // Bottom side
        Row(
            modifier = Modifier
                .offset(bottomLeftHorizontal.x, bottomLeftHorizontal.y)
                .size(
                    width = with(density) { tableSize.width.toDp() + playerBoxSize.width },
                    height = playerBoxSize.height
                ),
            horizontalArrangement = Arrangement.spacedBy(playerBoxSize.width/4, Alignment.CenterHorizontally)
//            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            for (i in (2*totalPlayersHorizontal.toInt() + 2*totalPlayersVertical.toInt() -1) downTo seatIndex ){
                gameState.players.values.toList().getOrNull(i)?.let {
                    PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = it, layoutDirection = PlayerLayoutDirection.BOTTOM)
                    seatIndex ++
                }
            }
        }
    }
}


@Composable
fun CardBox(cardId: Int?, modifier: Modifier) {
    cardId?.let { id ->
        Image(
            painter = painterResource(id = id),
            contentDescription = "card",
            modifier = modifier
                .padding(5.dp)
                .border(1.dp, Color.White.copy(alpha = 1f), RoundedCornerShape(6.dp))
                .padding(5.dp)
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
    modifier: Modifier = Modifier,
    size: DpSize,
    fontSize: TextUnit,
    playerState: PlayerState,
    showCards: Boolean = false,
    layoutDirection: PlayerLayoutDirection,
) {
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

    Box(
        modifier = modifier
    ){
        when(layoutDirection){
            PlayerLayoutDirection.LEFT -> {
                HorizontalPlayerItems(
                    playerState = playerState,
                    modifier = Modifier
                        .width(size.width * 65 / 100)
                        .align(Alignment.CenterEnd)
                        .offset(x = size.width * 65 / 100 - 5.dp)
                        .height(size.height),
                    fontSize = fontSize,
                    isLeft = true,
                    buttonsWidth = size.height / 3
                )
            }
            PlayerLayoutDirection.TOP -> {
                VerticalPlayerItems(
                    playerState = playerState,
                    modifier = Modifier
                        .width(size.width)
                        .height(size.height / 3)
                        .align(Alignment.BottomCenter)
                        .offset(y = size.height / 3),
                    fontSize = fontSize
                )
            }
            PlayerLayoutDirection.RIGHT -> {
                HorizontalPlayerItems(
                    playerState = playerState,
                    modifier = Modifier
                        .width(size.width * 65 / 100)
                        .align(Alignment.CenterStart)
                        .offset(x = -(size.width * 65 / 100 - 5.dp))
                        .height(size.height),
                    fontSize = fontSize,
                    isLeft = false,
                    buttonsWidth = size.height / 3
                )
            }
            PlayerLayoutDirection.BOTTOM -> {
                VerticalPlayerItems(
                    playerState = playerState,
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
                    .align(if (layoutDirection == PlayerLayoutDirection.LEFT) Alignment.TopEnd else Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
            ){
                for (i in 0..1){
                    val card = playerState.holeCards.getOrNull(i)
                    var cardID = R.drawable.gray_back
                    if (showCards){
                        card?.let {
                            cardID = CardsUtils.cardIDs[card.type.name.lowercase() + "_" + card.value] ?: R.drawable.gray_back
                        }
                    }

                    Image(
                        painter = painterResource(id = cardID),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }

            // Player box
            Box(
                modifier = Modifier
                    .size(size)
                    .drawBehind {
                        val path = Path().apply {
                            addRoundRect(
                                if (layoutDirection == PlayerLayoutDirection.LEFT) {
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
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Black,
                                    1f to Color.White
                                )
                            ),
                        )
                    }
            ){
                // Player icon box
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(10.dp)
                        .paint(
                            painter = painterResource(id = R.drawable.player_1),
                            contentScale = ContentScale.FillBounds,
                        )
                        .align(if (layoutDirection == PlayerLayoutDirection.LEFT) Alignment.BottomStart else Alignment.BottomEnd)
                )

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
                        .align(if (layoutDirection == PlayerLayoutDirection.LEFT) Alignment.BottomEnd else Alignment.BottomStart),
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 3.dp),
                        horizontalAlignment = if (layoutDirection == PlayerLayoutDirection.LEFT) Alignment.Start else Alignment.End
                    ){
                        Text(
                            text = playerState.nickname,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                ),
                                fontSize = fontSize,
                                color = Color.Black
                            ),
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            text = playerState.money.formatNumberToString(),
                            color = Color.Green,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            overflow = TextOverflow.Clip,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                ),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = fontSize*1.4
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalPlayerItems(
    playerState: PlayerState,
    modifier: Modifier,
    fontSize: TextUnit,
    isLeft: Boolean,
    buttonsWidth: Dp
){
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
    ){
        Box(
            modifier = Modifier
                .weight(1f)
                .align(if (isLeft) Alignment.Start else Alignment.End),
        ){
            Column(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .width(buttonsWidth),
            ){
                if(playerState.isDealer){
                    Image(
                        painter = painterResource(id = R.drawable.dealer_button),
                        contentDescription = "dealer_button",
                        modifier = Modifier.weight(1f)
                    )
                }
                if(playerState.isSmallBlind){
                    Image(
                        painter = painterResource(id = R.drawable.small_blind_button),
                        contentDescription = "dealer_button",
                        modifier = Modifier.weight(1f)
                    )
                }
                if(playerState.isBigBlind){
                    Image(
                        painter = painterResource(id = R.drawable.big_blind_button),
                        contentDescription = "dealer_button",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp)
                .background(
                    if (playerState.called != 0) colors.calledMoneyColor else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ){
            if (playerState.called != 0){
                Image(
                    painter = painterResource(id = R.drawable.money_chip),
                    contentDescription = "money_chip",
                )
                Text(
                    text = playerState.called.formatNumberToString(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )
            }
        }
    }
}


@Composable
private fun VerticalPlayerItems(
    playerState: PlayerState,
    modifier: Modifier,
    fontSize: TextUnit
){
    Row(
        modifier = modifier
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth(0.35f),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
        ){
            if(playerState.isDealer){
                Image(
                    painter = painterResource(id = R.drawable.dealer_button),
                    contentDescription = "dealer_button",
                    modifier = Modifier.weight(1f)
                )
            }
            if(playerState.isSmallBlind){
                Image(
                    painter = painterResource(id = R.drawable.small_blind_button),
                    contentDescription = "dealer_button",
                    modifier = Modifier.weight(1f)
                )
            }
            if(playerState.isBigBlind){
                Image(
                    painter = painterResource(id = R.drawable.big_blind_button),
                    contentDescription = "dealer_button",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .background(
                    if (playerState.called != 0) colors.calledMoneyColor else Color.Transparent,
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 2.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ){
            if (playerState.called != 0){
                Image(
                    painter = painterResource(id = R.drawable.money_chip),
                    contentDescription = "money_chip",
                )
                Text(
                    text = playerState.called.formatNumberToString(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )
            }
        }
    }
}