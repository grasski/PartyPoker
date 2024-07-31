package com.dabi.partypoker.featureServer.model.data

import android.content.Context
import android.util.Log
import androidx.annotation.IntRange
import androidx.annotation.Keep
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.room.Room
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.model.data.endpointID
import com.dabi.partypoker.repository.gameSettings.GameSettings
import com.dabi.partypoker.repository.gameSettings.GameSettingsDatabase
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.UiTexts
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import javax.inject.Inject


data class SeatPosition(@IntRange(from = 0, to = 9) val position: Int) {
    init {
        require(position in 0..9) { "Position must be in range 0 to 9" }
    }
}

@Keep
data class MessageData(
//    val filterType: FilterType,   maybe in future
    val sender: String? = null,
    val messages: List<UiTexts> = emptyList(),
    val cards: List<Card>? = null
){
    fun toString(context: Context): String {
        var string = sender?.let { "$it: " } ?: ""
        messages.forEach{
            string += " " + it.asString(context)
        }
        return string
    }

    @Composable
    fun ShowMessage(
        modifier: Modifier = Modifier,
        maxLines: Int,
        fontSize: TextUnit,
        textColor: Color = Color.White,
    ) {
        val context = LocalContext.current

        val annotatedString = buildAnnotatedString {
            append(toString(context) + " ")
            cards?.let {
                it.forEach { card ->
                    appendInlineContent(id = card.type.name+card.value)
                }
            }
        }
        val inlineContentMap = cards?.associate { card ->
            card.type.name+card.value to InlineTextContent(
                placeholder = Placeholder(fontSize*2, fontSize*2, PlaceholderVerticalAlign.TextCenter)
            ){
                val cardId = CardsUtils.cardIDs[card.type.name.lowercase() + "_" + card.value]
                cardId?.let { id ->
                    Image(
                        painter = painterResource(id = id),
                        contentDescription = card.type.name+card.value,
                    )
                }
            }
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (maxLines == 1){
                var oldText by remember {
                    mutableStateOf(annotatedString)
                }
                SideEffect {
                    oldText = annotatedString
                }
                AnimatedContent(
                    targetState = oldText,
                    transitionSpec = {
                        slideInVertically { it } togetherWith slideOutVertically { -it }
                    }
                ) { text ->
                    Text(
                        text = text,
                        maxLines = maxLines,
                        fontSize = fontSize,
                        color = textColor,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                        inlineContent = inlineContentMap ?: emptyMap()
                    )
                }

            } else{
                Text(
                    text = annotatedString,
                    maxLines = maxLines,
                    fontSize = fontSize,
                    color = textColor,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        )
                    ),
                    inlineContent = inlineContentMap ?: emptyMap()
                )
            }
        }
    }
}


data class GameState(
    var players: Map<endpointID, PlayerState> = mapOf(),
    var gameReadyPlayers: Map<endpointID, SeatPosition> = emptyMap(),
    var seatPositions: Map<endpointID, SeatPosition> = emptyMap(), // playerId, seatPosition

    var gameSettings: GameSettings = GameSettings(),

    var started: Boolean = false,
    var ongoing: Boolean = false,

    var cardsDeck: List<Card> = emptyList(),
    var cardsTable: List<Card> = emptyList(),

    var activeRaise: Pair<endpointID, Int>? = null,   // playerId, amount
    var bigBlindRaised: Boolean = false,    // BB raised in round 1
    var bank: Int = 0,

    var dealerId: endpointID? = null,
    var smallBlindId: endpointID? = null,
    var bigBlindId: endpointID? = null,
    var roundStartedId: endpointID? = null,
    var playingNow: endpointID? = null,

    var messages: List<MessageData> = emptyList(),

    var round: Int = 0,
    var games: Int = 0,
    var gameOver: Boolean = false,
    var winningCards: Set<Card> = emptySet(),
){
    fun getAvailableRandomPosition(): Int? {
        val takenPositions = seatPositions.values.map { it.position }.toSet()
        val availablePositions = (0..9).filter { it !in takenPositions }
        return if (availablePositions.isNotEmpty()) {
            availablePositions.random()
        } else {
            null
        }
    }
}
