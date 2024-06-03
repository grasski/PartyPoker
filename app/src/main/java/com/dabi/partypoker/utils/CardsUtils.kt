package com.dabi.partypoker.utils

import com.dabi.partypoker.R


enum class CardType{
    CLUB,
    DIAMOND,
    HEART,
    SPADE
}
data class Card(
    val type: CardType,
    val value: Int
)

enum class CardsCombination {
    ROYAL_FLUSH,
    STRAIGHT_FLUSH,
    POKER,
    FULL_HOUSE,
    FLUSH,
    STRAIGHT,
    TRIPS,
    TWO_PAIRS,
    PAIR,
    CARD,

    NONE
}

fun generateDeck(): MutableList<Card> {
    val values = 2..14
    val types = CardType.entries.toTypedArray()
    val deck = mutableListOf<Card>()
    for (type in types) {
        values.forEach { value ->
            deck.add(Card(type, value))
        }
    }
    deck.shuffle()
    return deck
}

fun getCards(cards: MutableList<Card>, number: Int): List<Card> {
    val playerCards = mutableListOf<Card>()

    repeat(number){
        val card = cards.random()
        cards.remove(card)
        playerCards.add(card)
    }

    return playerCards.toList()
}

object CardsUtils{
    val combinationsTranslationID = mapOf(
        CardsCombination.CARD to R.string.card,
        CardsCombination.PAIR to R.string.pair,
        CardsCombination.TWO_PAIRS to R.string.two_pairs,
        CardsCombination.TRIPS to R.string.trips,
        CardsCombination.STRAIGHT to R.string.straight,
        CardsCombination.FLUSH to R.string.flush,
        CardsCombination.FULL_HOUSE to R.string.full_house,
        CardsCombination.POKER to R.string.poker,
        CardsCombination.STRAIGHT_FLUSH to R.string.straight_flush,
        CardsCombination.ROYAL_FLUSH to R.string.royal_flush
    )

    val cardIDs: MutableMap<String, Int> = mutableMapOf(
        "club_2" to R.drawable.clubs_2,
        "club_3" to R.drawable.clubs_3,
        "club_4" to R.drawable.clubs_4,
        "club_5" to R.drawable.clubs_5,
        "club_6" to R.drawable.clubs_6,
        "club_7" to R.drawable.clubs_7,
        "club_8" to R.drawable.clubs_8,
        "club_9" to R.drawable.clubs_9,
        "club_10" to R.drawable.clubs_10,
        "club_11" to R.drawable.clubs_jack,
        "club_12" to R.drawable.clubs_queen,
        "club_13" to R.drawable.clubs_king,
        "club_14" to R.drawable.clubs_ace,
        "club_1" to R.drawable.clubs_ace,

        "diamond_2" to R.drawable.diamonds_2,
        "diamond_3" to R.drawable.diamonds_3,
        "diamond_4" to R.drawable.diamonds_4,
        "diamond_5" to R.drawable.diamonds_5,
        "diamond_6" to R.drawable.diamonds_6,
        "diamond_7" to R.drawable.diamonds_7,
        "diamond_8" to R.drawable.diamonds_8,
        "diamond_9" to R.drawable.diamonds_9,
        "diamond_10" to R.drawable.diamonds_10,
        "diamond_11" to R.drawable.diamonds_jack,
        "diamond_12" to R.drawable.diamonds_queen,
        "diamond_13" to R.drawable.diamonds_king,
        "diamond_14" to R.drawable.diamonds_ace,
        "diamond_1" to R.drawable.diamonds_ace,

        "heart_2" to R.drawable.hearts_2,
        "heart_3" to R.drawable.hearts_3,
        "heart_4" to R.drawable.hearts_4,
        "heart_5" to R.drawable.hearts_5,
        "heart_6" to R.drawable.hearts_6,
        "heart_7" to R.drawable.hearts_7,
        "heart_8" to R.drawable.hearts_8,
        "heart_9" to R.drawable.hearts_9,
        "heart_10" to R.drawable.hearts_10,
        "heart_11" to R.drawable.hearts_jack,
        "heart_12" to R.drawable.hearts_queen,
        "heart_13" to R.drawable.hearts_king,
        "heart_14" to R.drawable.hearts_ace,
        "heart_1" to R.drawable.hearts_ace,

        "spade_2" to R.drawable.spades_2,
        "spade_3" to R.drawable.spades_3,
        "spade_4" to R.drawable.spades_4,
        "spade_5" to R.drawable.spades_5,
        "spade_6" to R.drawable.spades_6,
        "spade_7" to R.drawable.spades_7,
        "spade_8" to R.drawable.spades_8,
        "spade_9" to R.drawable.spades_9,
        "spade_10" to R.drawable.spades_10,
        "spade_11" to R.drawable.spades_jack,
        "spade_12" to R.drawable.spades_queen,
        "spade_13" to R.drawable.spades_king,
        "spade_14" to R.drawable.spades_ace,
        "spade_1" to R.drawable.spades_ace,
    )
}