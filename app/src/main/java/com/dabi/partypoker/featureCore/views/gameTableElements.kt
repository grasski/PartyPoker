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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.dabi.easylocalgame.serverSide.data.ServerType
import com.dabi.easylocalgame.textUtils.UiTexts
import com.dabi.partypoker.R
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featurePlayer.model.data.PlayerState
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.MessageData
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.utils.CardsUtils


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
        Image(
            painter = painterResource(id),
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


//@Preview(wallpaper = Wallpapers.NONE, device = "spec:parent=pixel_5,orientation=landscape",
//    showBackground = true
//)
//@Composable
//fun MessageViewPreview(modifier: Modifier = Modifier) {
//    val messages = listOf(
//        MessageData(
//            sender = null,
//            messages = listOf(
//                UiTexts.PluralResource(R.plurals.winner_public, 1, "PLAYER 1", 512),
//                UiTexts.StringResource(CardsUtils.combinationsTranslationID[CardsCombination.POKER]!!)
//            ),
//            cards = listOf(
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//            )
//        ),
//
//        MessageData(
//            sender = null,
//            messages = listOf(
//                UiTexts.PluralResource(R.plurals.winner_public, 1, "PLAYER 1", 512),
//                UiTexts.StringResource(CardsUtils.combinationsTranslationID[CardsCombination.POKER]!!)
//            ),
//            cards = listOf(
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//            ),
//            history = GameHistoryState(
//                gameNumber = 1,
//                tableCards = listOf(
//                    com.dabi.partypoker.utils.Card(CardType.CLUB, 10),
//                    com.dabi.partypoker.utils.Card(CardType.CLUB, 10),
//                    com.dabi.partypoker.utils.Card(CardType.CLUB, 10),
//                    com.dabi.partypoker.utils.Card(CardType.CLUB, 10),
//                    com.dabi.partypoker.utils.Card(CardType.CLUB, 10)
//                ),
//                players = mapOf(
//                    "player 1" to mapOf(CardsCombination.POKER to listOf(
//                        com.dabi.partypoker.utils.Card(CardType.SPADE, 4),
//                        com.dabi.partypoker.utils.Card(CardType.HEART, 11)
//                    )),
//                    "player 2" to null
//                )
//            )
//        ),
//
//        MessageData(
//            sender = null,
//            messages = listOf(
//                UiTexts.PluralResource(R.plurals.winner_public, 1, "PLAYER 1", 512),
//                UiTexts.StringResource(CardsUtils.combinationsTranslationID[CardsCombination.POKER]!!)
//            ),
//            cards = listOf(
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//                com.dabi.partypoker.utils.Card(CardType.CLUB, 14),
//            )
//        )
//    )
//
//    PartyPokerTheme {
//        MessagesView(
//            onDismissRequest = {  },
//            messages = messages
//        )
//    }
//}