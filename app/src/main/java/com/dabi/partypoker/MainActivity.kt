package com.dabi.partypoker

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureCore.views.GameTable
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.ui.theme.PartyPokerTheme
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardType
import com.dabi.partypoker.utils.evaluateGame
import com.dabi.partypoker.utils.evaluatePlayerCards
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
//
//                        val players = listOf(
//                            PlayerState(
//                                nickname = "1",
//                                holeCards = listOf(
//                                    Card(CardType.SPADE, 12),
//                                    Card(CardType.HEART, 13),
//                                )
//                            ),
//                            PlayerState(
//                                nickname = "2",
//                                holeCards = listOf(
//                                    Card(CardType.SPADE, 3),
//                                    Card(CardType.DIAMOND, 5),
//                                )
//                            ),
//                            PlayerState(
//                                nickname = "3",
//                                holeCards = listOf(
//                                    Card(CardType.CLUB, 13),
//                                    Card(CardType.HEART, 12),
//                                )
//                            )
//                        )
//                        val tableCards = listOf(
//                            Card(CardType.DIAMOND, 14),
//                            Card(CardType.DIAMOND, 2),
//                            Card(CardType.DIAMOND, 10),
//                            Card(CardType.DIAMOND, 4),
//                            Card(CardType.DIAMOND, 11),
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
