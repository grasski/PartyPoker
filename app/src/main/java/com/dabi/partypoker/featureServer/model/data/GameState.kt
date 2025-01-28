package com.dabi.partypoker.featureServer.model.data

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.Keep
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.dabi.easylocalgame.serverSide.data.IGameState
import com.dabi.easylocalgame.textUtils.UiTexts
import com.dabi.partypoker.R
import com.dabi.partypoker.featurePlayer.model.data.PlayerState
import com.dabi.partypoker.repository.gameSettings.GameSettings
import com.dabi.partypoker.utils.Card
import com.dabi.partypoker.utils.CardsCombination
import com.dabi.partypoker.utils.CardsUtils


@Keep
data class MessageData(
//    val filterType: FilterType,   maybe in future
    val sender: String? = null,
    val messages: List<UiTexts> = emptyList(),
    val cards: List<Card>? = null,
    val history: GameHistoryState? = null
){
    fun toString(context: Context): String {
        var string = sender?.let { "$it: " } ?: ""
        messages.forEach{
            string += " " + it.asString(context)
        }
        return string
    }

    private fun textWithCards(
        text: String,
        cards: List<Card>? = null,
        cardSize: TextUnit
    ): Pair<AnnotatedString, Map<String, InlineTextContent>?> {
        val annotatedString = buildAnnotatedString {
            append(text)
            cards?.let {
                it.forEach { card ->
                    appendInlineContent(id = card.type.name+card.value)
                }
            }
        }
        val inlineContentMap = cards?.associate { card ->
            card.type.name+card.value to InlineTextContent(
                placeholder = Placeholder(cardSize*2, cardSize*2, PlaceholderVerticalAlign.TextCenter)
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

        return Pair(annotatedString, inlineContentMap)
    }

    @Composable
    fun ShowMessage(
        modifier: Modifier = Modifier,
        maxLines: Int,
        fontSize: TextUnit,
        textColor: Color = Color.White,
    ) {
        val context = LocalContext.current
        val (annotatedString, inlineContentMap) = textWithCards(toString(context) + " ", cards, fontSize)

        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier = modifier
                .animateContentSize(),
            verticalArrangement = Arrangement.Center,
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
                history?.let { his ->
                    Row {
                        Text(
                            text = UiTexts.StringResource(R.string.game_number_finished, his.gameNumber).asString(),
                            fontSize = fontSize * 0.85f
                        )

                        Text(
                            text =
                            if (expanded)
                                UiTexts.StringResource(R.string.show_less).asString()
                            else
                                UiTexts.StringResource(R.string.show_more).asString(),
                            fontSize = fontSize * 0.85f,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = null,
                                    onClick = {
                                        expanded = !expanded
                                    }
                                )
                                .shadow(12.dp, RoundedCornerShape(8.dp))
                        )
                    }
                }
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


                history?.let { his ->
                    if (expanded){
                        Box(
                            contentAlignment = Alignment.Center
                        ){
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = UiTexts.StringResource(R.string.round_info).asString(),
                                fontSize = fontSize * 0.85f,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 6.dp)
                            )
                        }

                        val (annotatedStringTable, inlineContentMapTable) = textWithCards(
                            UiTexts.StringResource(R.string.table_cards).asString() + " ",
                            his.tableCards, fontSize)
                        Text(
                            text = annotatedStringTable,
                            maxLines = maxLines,
                            fontSize = fontSize,
                            color = textColor,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            ),
                            inlineContent = inlineContentMapTable ?: emptyMap(),
                        )

                        his.players.forEach { (nick, cards, cardsComb) ->
                            if (!cards.isNullOrEmpty()) {
                                val (annotatedStringPlayer, inlineContentMapPlayer) = textWithCards(
                                    "$nick: " + UiTexts.StringResource(CardsUtils.combinationsTranslationID.getValue(cardsComb!!)).asString() + ": ",
                                    cards, fontSize
                                )
                                Text(
                                    text = annotatedStringPlayer,
                                    maxLines = maxLines,
                                    fontSize = fontSize,
                                    color = textColor,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    ),
                                    inlineContent = inlineContentMapPlayer ?: emptyMap(),
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            } else {
                                Text(
                                    text = "$nick : " + UiTexts.StringResource(R.string.folded).asString().uppercase(),
                                    maxLines = maxLines,
                                    fontSize = fontSize,
                                    color = textColor,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(
                                            includeFontPadding = false
                                        )
                                    ),
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Keep
data class GameHistoryState(
    var gameNumber: Int = 0,
    var tableCards: List<Card> = emptyList(),
    var players: List<PlayerHistory> = emptyList(),
)
data class PlayerHistory(
    var nick: String = "",
    var cards: List<Card>? = emptyList(),
    var cardsCombination: CardsCombination? = CardsCombination.NONE
)

data class SeatPosition(@IntRange(from = 0, to = 9) val position: Int) {
    init {
        require(position in 0..9) { "Position must be in range 0 to 9" }
    }
}

data class GameState (
    override val players: Map<String, PlayerState> = mapOf(),
    var gameReadyPlayers: Map<String, SeatPosition> = emptyMap(),
    var seatPositions: Map<String, SeatPosition> = emptyMap(), // playerId, seatPosition

    var gameSettings: GameSettings = GameSettings(),

    var started: Boolean = false,
    var ongoing: Boolean = false,

    var cardsDeck: List<Card> = emptyList(),
    var cardsTable: List<Card> = emptyList(),

    var activeRaise: Pair<String, Int>? = null,   // playerId, amount
    var bigBlindRaised: Boolean = false,    // BB raised in round 1
    var bank: Int = 0,

    var dealerId: String? = null,
    var smallBlindId: String? = null,
    var bigBlindId: String? = null,
    var roundStartedId: String? = null,
    var playingNow: String? = null,

    var completeAllIn: Boolean = false,

    var messages: List<MessageData> = emptyList(),

    var round: Int = 0,
    var games: Int = 0,
    var gameOver: Boolean = false,
    var winningCards: Set<Card> = emptySet(),
): IGameState{
    fun getAvailableRandomPosition(): Int? {
        val takenPositions = seatPositions.values.map { it.position }.toSet()
        val availablePositions = (0..9).filter { it !in takenPositions }
        return if (availablePositions.isNotEmpty()) {
            availablePositions.random()
        } else {
            null
        }
    }

    fun copy(): GameState {
        val copiedPlayers = players.mapValues { (_, player) -> player.copy() }
        val copiedGameReadyPlayers = gameReadyPlayers.toMap()
        val copiedSeatPositions = seatPositions.toMap()
        val copiedCardsDeck = cardsDeck.toList()
        val copiedCardsTable = cardsTable.toList()
        val copiedMessages = messages.toList()

        return this.copy(
            players = copiedPlayers,
            gameReadyPlayers = copiedGameReadyPlayers,
            seatPositions = copiedSeatPositions,
            cardsDeck = copiedCardsDeck,
            cardsTable = copiedCardsTable,
            messages = copiedMessages
        )
    }
}
