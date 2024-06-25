package com.dabi.partypoker

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureCore.views.CalculatePlayerBoxSize
import com.dabi.partypoker.featureCore.views.GameTable
import com.dabi.partypoker.featureCore.views.MessagesView
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.featureServer.model.data.MessageData
import com.dabi.partypoker.ui.theme.PartyPokerTheme
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardType
import com.dabi.partypoker.utils.CardsCombination
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.evaluateGame
import com.dabi.partypoker.utils.evaluatePlayerCards
import com.dabi.partypoker.utils.formatNumberToString
import com.dabi.partypoker.utils.generateDeck
import com.dabi.partypoker.utils.getCards
import com.dabi.partypoker.utils.handStrength
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            PartyPokerTheme {
                Scaffold (){
                    Box(modifier = Modifier.padding(it).fillMaxSize()){
                        Navigation()


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
//                                    Card(CardType.HEART, 7),
//                                    Card(CardType.DIAMOND, 13),
//                                )
//                            ),
//                            PlayerState(
//                                nickname = "2",
//                                holeCards = listOf(
//                                    Card(CardType.SPADE, 7),
//                                    Card(CardType.HEART, 12),
//                                )
//                            ),
//                        )
//                        val tableCards = listOf(
//                            Card(CardType.HEART, 14),
//                            Card(CardType.DIAMOND, 9),
//                            Card(CardType.DIAMOND, 5),
//                            Card(CardType.DIAMOND, 6),
//                            Card(CardType.SPADE, 8),
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
