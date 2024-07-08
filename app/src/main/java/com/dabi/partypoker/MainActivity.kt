package com.dabi.partypoker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottiePainter
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureCore.views.CardBox
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.SeatPosition
import com.dabi.partypoker.managers.GameManager
import com.dabi.partypoker.repository.gameSettings.GameSettings
import com.dabi.partypoker.repository.gameSettings.GameSettingsDatabase
import com.dabi.partypoker.ui.theme.PartyPokerTheme
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardType
import com.dabi.partypoker.utils.evaluateGame
import com.dabi.partypoker.utils.evaluatePlayerCards
import com.dabi.partypoker.utils.handStrength
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

//        val db = Room.databaseBuilder(
//            applicationContext,
//            GameSettingsDatabase::class.java,
//            "game_settings.db"
//        ).build()

        setContent {
            PartyPokerTheme {
                Scaffold (){
                    Box(modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                    ){
//                        val scope = rememberCoroutineScope()
//                        LaunchedEffect(Unit) {
//                            scope.launch {
//                                db.dao.upsertSetting(GameSettings(
//                                    title = "Prvni nastaveni",
//                                    playerMoney = 1000,
//                                    smallBlindAmount = 25,
//                                    bigBlindAmount = 50,
//                                    playerTimerDurationMillis = 8000,
//                                    nextGameInMillis = 8000,
//                                ))
//                                db.dao.upsertSetting(GameSettings(
//                                    title = "druhe nastaveni",
//                                    playerMoney = 2000,
//                                    smallBlindAmount = 50,
//                                    bigBlindAmount = 100,
//                                    playerTimerDurationMillis = 8000,
//                                    nextGameInMillis = 8000,
//                                ))
//                            }
//                        }

                        Navigation()
//                        PlayerViewPrivate()


//                        var playerBoxSize by remember { mutableStateOf(DpSize.Zero) }
//                        var fontSize by remember { mutableStateOf(20.sp) }
//                        CalculatePlayerBoxSize(
//                            playerBoxSize = { playerBoxSize = it },
//                            fontSize = { fontSize = it }
//                        )
//                        var tablePosition by remember { mutableStateOf(Offset.Zero) }
//                        var tableSize by remember { mutableStateOf(IntSize.Zero) }
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth(0.8f)
//                                .padding(vertical = 50.dp)
//                                .padding(horizontal = 16.dp)
//                                .onGloballyPositioned {
//                                    tablePosition = it.positionInRoot()
//                                    tableSize = it.size
//                                }
//                                .clip(RoundedCornerShape(40.dp))
//                                .border(1.dp, Color.White, RoundedCornerShape(40.dp))
//                                .border(10.dp, Color.DarkGray, RoundedCornerShape(40.dp))
//                                .border(11.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(40.dp))
//                                .paint(
//                                    rememberAsyncImagePainter(model = R.drawable.table),
//                                    contentScale = ContentScale.FillBounds
//                                )
//                                .padding(11.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center
//                        ) {
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth(0.6f)
//                                    .height(playerBoxSize.height / 2)
//                                    .padding(horizontal = 5.dp),
//                                horizontalArrangement = Arrangement.spacedBy(
//                                    5.dp,
//                                    Alignment.CenterHorizontally
//                                )
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .width(playerBoxSize.width - playerBoxSize.width / 3)
//                                        .fillMaxHeight(),
//                                    horizontalArrangement = Arrangement.spacedBy(
//                                        5.dp,
//                                        Alignment.CenterHorizontally
//                                    ),
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Image(
//                                        painter = painterResource(R.drawable.bank),
//                                        contentDescription = "bank",
//                                        modifier = Modifier
//                                            .size(playerBoxSize.height / 2)
//                                    )
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxSize()
//                                            .background(Color.Black, RoundedCornerShape(8.dp)),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text(
//                                            text = 50.formatNumberToString(),
//                                            fontWeight = FontWeight.Bold,
//                                            textAlign = TextAlign.Center,
//                                            fontSize = fontSize,
//                                            maxLines = 1,
//                                            color = Color.White,
//                                            style = TextStyle(
//                                                platformStyle = PlatformTextStyle(
//                                                    includeFontPadding = false
//                                                )
//                                            )
//                                        )
//                                    }
//                                }
//
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
//                                        .clip(RoundedCornerShape(8.dp))
//                                        .background(Color.Black.copy(0.35f)),
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.SpaceBetween
//                                ) {
//                                    val gameState = GameState()
//                                    gameState.messages += MessageData(
//                                        messages = listOf(
//                                            UiTexts.PluralResource(
//                                                R.plurals.winner_public,
//                                                1,
//                                                "Jirka",
//                                                500
//                                            ),
//                                            UiTexts.StringResource(CardsUtils.combinationsTranslationID[CardsCombination.ROYAL_FLUSH]!!)
//                                        ),
//                                        cards = listOf(
//                                            Card(CardType.HEART, 14),
//                                            Card(CardType.DIAMOND, 9),
//                                            Card(CardType.DIAMOND, 5),
//                                            Card(CardType.DIAMOND, 6),
//                                            Card(CardType.SPADE, 8),
//                                        )
//                                    )
//
//                                    var showMessages by remember { mutableStateOf(false) }
//                                    if (showMessages){
//                                        MessagesView(
//                                            onDismissRequest = { showMessages = false },
//                                            messages = gameState.messages
//                                        )
//                                    }
//
//                                    Box(
//                                        modifier = Modifier
//                                            .weight(1f)
//                                            .padding(horizontal = 5.dp)
//                                    ) {
//                                        gameState.messages.lastOrNull()?.ShowMessage(
//                                            maxLines = 1,
//                                            fontSize = fontSize,
//                                            textColor = Color.White
//                                        )
//                                    }
//
//                                    Icon(
//                                        Icons.Default.MoreVert,
//                                        contentDescription = "",
//                                        modifier = Modifier
//                                            .clip(CircleShape)
//                                            .clickable(onClick = {
//                                                showMessages = true
//                                            }),
//                                        tint = Color.White
//                                    )
//                                }
//                            }
//                        }


//                        val players = listOf(
//                            PlayerState(
//                                nickname = "1",
//                                holeCards = listOf(
//                                    Card(CardType.CLUB, 2),
//                                    Card(CardType.SPADE, 9),
//                                )
//                            ),
//                            PlayerState(
//                                nickname = "2",
//                                holeCards = listOf(
//                                    Card(CardType.CLUB, 9),
//                                    Card(CardType.DIAMOND, 13),
//                                )
//                            ),
//                        )
//                        val tableCards = listOf(
//                            Card(CardType.HEART, 7),
//                            Card(CardType.CLUB, 7),
//                            Card(CardType.DIAMOND, 3),
//                            Card(CardType.DIAMOND, 7),
//                            Card(CardType.DIAMOND, 9),
//                        )
//                        val e = evaluateGame(
//                            players = players,
//                            tableCards = tableCards
//                        )
//                        Log.e("", "EVALUATE: " + e)
//
//                        players.forEach {
//                            val playerEval = evaluatePlayerCards(tableCards, it.holeCards)
//                            val evalStrength = handStrength(playerEval)
//
//                            Log.e("", it.nickname + " has : " + playerEval + " with strength: " + evalStrength)
//                        }
//                        val gameState = GameState(
//                            players = mapOf(
//                                "1" to PlayerState(
//                                    nickname = "1",
//                                    holeCards = listOf(
//                                        Card(CardType.CLUB, 2),
//                                        Card(CardType.SPADE, 9),
//                                    )
//                                ),
//                                "2" to PlayerState(
//                                    nickname = "2",
//                                    holeCards = listOf(
//                                        Card(CardType.CLUB, 9),
//                                        Card(CardType.DIAMOND, 13),
//                                    )
//                                ),
//                            ),
//                            cardsTable = tableCards,
//                            gameReadyPlayers = mapOf(
//                                "1" to SeatPosition(1),
//                                "2" to SeatPosition(2)
//                            )
//                        )
//
//                        val g = GameManager.gameOver(gameState = gameState)
//                        Log.e("", "GAME OVER: " + g.winningCards)
//                        g.messages.last().ShowMessage(maxLines = 5, fontSize = 20.sp, textColor = Color.Green)


//                        val deck = generateDeck()
//                        val tableCards = getCards(deck, 5)
//                        deck.removeAll(tableCards)
//                        Log.e("", "Table cards: " + tableCards)
//
//                        val players: MutableList<PlayerState> = mutableListOf()
//                        for (i in 0..5){
//                            val handCards = getCards(deck, 2)
//                            deck.removeAll(tableCards)
//
//                            players.add(
//                                PlayerState(
//                                    nickname = "Player $i",
//                                    holeCards = handCards
//                                )
//                            )
//                            Log.e("", "Player $i: " + handCards + " " + handStrength(evaluatePlayerCards(tableCards, handCards)) + " " + evaluatePlayerCards(tableCards, handCards))
//                        }
//
//                        val evaluation = evaluateGame(players, tableCards)
//                        Log.e("", "EVALUATION: " + evaluation.first.map { it.nickname } + " " + evaluation.second.first)
                    }
                }
            }
        }
    }
}
