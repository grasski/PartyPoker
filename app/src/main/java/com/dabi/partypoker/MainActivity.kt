package com.dabi.partypoker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.ui.theme.PartyPokerTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        enableEdgeToEdge()

        setContent {
            PartyPokerTheme {
                Scaffold {
                    Box(modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .paint(
                            rememberAsyncImagePainter(model = R.drawable.game_background),
                            contentScale = ContentScale.Crop
                        )
                    ){
                        Navigation()


//                        ServerGameView(
//                            gameState = GameState(
//                                players = mapOf(
//                                    "1" to PlayerState("Hráč 1", money = 1000, isReadyToPlay = true, isDealer = true, holeCards = listOf(
//                                        Card(CardType.CLUB, 2), Card(CardType.CLUB, 3)
//                                    )),
//                                    "2" to PlayerState("Hráč 2", money = 1000, isReadyToPlay = true, isSmallBlind = true, holeCards = listOf(
//                                        Card(CardType.SPADE, 5), Card(CardType.HEART, 10)
//                                    )),
//                                    "3" to PlayerState("Hráč 3", money = 1000, isReadyToPlay = true, isBigBlind = true, holeCards = listOf(
//                                        Card(CardType.DIAMOND, 14), Card(CardType.CLUB, 14)
//                                    )),
//                                ),
//                                seatPositions = mapOf(
//                                    "1" to SeatPosition(0),
//                                    "2" to SeatPosition(2),
//                                    "3" to SeatPosition(5),
//                                ),
//                                gameOver = true,
//                                started = true,
//                                ongoing = true,
//                                cardsTable = listOf(
//                                    Card(CardType.CLUB, 7),
//                                    Card(CardType.HEART, 6),
//                                    Card(CardType.DIAMOND, 7),
//                                    Card(CardType.SPADE, 4),
//                                    Card(CardType.DIAMOND, 11)
//                                )
//                            ),
//                            serverState = ServerState(),
//                            onGameEvent = {}
//                        )
                    }
                }
            }
        }
    }
}
