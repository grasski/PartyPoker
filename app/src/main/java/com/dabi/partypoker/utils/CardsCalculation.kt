package com.dabi.partypoker.utils

import com.dabi.partypoker.featurePlayer.model.data.PlayerState


fun checkEpicFlush(cards: List<Card>): Pair<Boolean, List<Card>> {
    if (cards.size < 5) { return Pair(false, emptyList()) }

    val flush = checkFlush(cards)
    if (!flush.first) { return Pair(false, emptyList()) }

    val straight = checkStraight(flush.second)
    if (!straight.first) { return Pair(false, emptyList()) }

    return Pair(true, straight.second)
}

fun checkPoker(cards: List<Card>): Pair<Boolean, List<Card>> {
    if (cards.size < 4) { return Pair(false, emptyList()) }
    return checkNSame(cards, 4)
}

fun checkFullHouse(cards: List<Card>): Pair<Boolean, List<Card>>{
    if (cards.size < 5) { return Pair(false, listOf()) }

    val trips = checkNSame(cards, 3)
    if (!trips.first){
        return Pair(false, listOf())
    }

    val cards2 = cards.toMutableList()
    cards2.removeAll(trips.second)

    val pair = checkNSame(cards2, 2)
    if (!pair.first){
        return Pair(false, listOf())
    }

    return Pair(true, trips.second + pair.second)
}

fun checkFlush(cards: List<Card>): Pair<Boolean, List<Card>> {
    if (cards.size < 5) { return Pair(false, listOf()) }

    val sorted = cards.sortedBy { it.value }

    val clubs = sorted.filter { it.type == CardType.CLUB }
    if (clubs.size >= 5){
        return Pair(true, clubs)
    } else if (clubs.size in 3..4){
        // No more possible to have Flush with other cards
        return Pair(false, listOf())
    }

    val diamonds = sorted.filter { it.type == CardType.DIAMOND }
    if (diamonds.size >= 5){
        return Pair(true, diamonds)
    } else if (diamonds.size in 3..4 || (clubs.size + diamonds.size) >= 3){
        // No more possible to have Flush with other cards
        return Pair(false, listOf())
    }

    val hearts = sorted.filter { it.type == CardType.HEART }
    if (hearts.size >= 5){
        return Pair(true, hearts)
    } else if (hearts.size in 3..4 || (clubs.size + diamonds.size + hearts.size) >= 3){
        // No more possible to have Flush with other cards
        return Pair(false, listOf())
    }

    val spades = sorted.filter { it.type == CardType.SPADE }
    if (spades.size >= 5){
        return Pair(true, spades)
    }
    return Pair(false, listOf())
}

fun checkStraight(cards: List<Card>): Pair<Boolean, List<Card>> {
    if (cards.size < 5) { return Pair(false, emptyList()) }

    fun fiveInConsecutive(cards: List<Card>): Boolean {
        for (i in 0..(cards.size-2)){
            if (cards[i].value + 1 != cards[i+1].value){
                return false
            }
        }
        return true
    }
    fun run(cards: List<Card>): Pair<Boolean, List<Card>>{
        val sorted = cards.sortedBy { it.value }.distinctBy { it.value }

        when(sorted.size){
            // 3 on table and 2 in hand
            5 -> {
                return Pair(fiveInConsecutive(sorted), sorted)
            }
            // 4 on table and 2 in hand
            6 -> {
                val firstPart = sorted.takeLast(5)  // Highest card
                if(fiveInConsecutive(firstPart)){ return Pair(true, firstPart) }

                val secondPart = sorted.take(5)
                return Pair(fiveInConsecutive(secondPart), secondPart)
            }
            // 5 on table and 2 in hand
            7 -> {
                val firstPart = sorted.takeLast(5)       // Highest card
                if(fiveInConsecutive(firstPart)){ return Pair(true, firstPart) }

                val secondPart = sorted.subList(1, 6)       // Second highest card
                if(fiveInConsecutive(secondPart)){ return Pair(true, secondPart) }

                val thirdPart = sorted.take(5)
                return Pair(fiveInConsecutive(thirdPart), thirdPart)
            }
        }
        return Pair(false, emptyList())
    }

    if (cards.any { it.value == 14 }){
        // There is straight with Ace of the top value
        val r = run(cards)
        if (r.first){
            return Pair(true, r.second)
        }

        // There is no straight with Ace of the top value, look if there is with Ace as value 1
        val sorted = cards.toMutableList().mapIndexed { _, element ->
            if (element.value == 14) {
                Card(element.type, 1)
            } else {
                element
            }
        }.sortedBy { it.value }
        return (run(sorted))
    }

    return run(cards)
}

fun checkTrips(cards: List<Card>): Pair<Boolean,  List<Card>> {
    if (cards.size < 3) { return Pair(false, emptyList()) }

    return checkNSame(cards, 3)
}

fun checkTwoPairs(cards: List<Card>): Pair<Boolean, List<Card>> {
    if (cards.size < 4) { return Pair(false, listOf()) }

    val grouped = cards.groupBy { it.value }

    val nSame = grouped.values.filter { it.size >= 2 }
    if (nSame.size < 2){
        return Pair(false, listOf())
    }

    val twoPairCards = nSame.flatten().sortedByDescending { it.value }.take(4)
    return Pair(true, twoPairCards)
}

fun checkPair(cards: List<Card>): Pair<Boolean, List<Card>> {
    return checkNSame(cards, 2)
}


private fun checkNSame(cards: List<Card>, n: Int): Pair<Boolean, List<Card>> {
    val grouped = cards.groupBy { it.value }

    val nSame = grouped.values.filter { it.size >= n }
    if (nSame.isEmpty()){
        return Pair(false, emptyList())
    }

    val nCards = nSame.flatten().filter { it.value == nSame.flatten().maxOf { it.value } }
    return Pair(true, nCards)
}



fun evaluatePlayerCards(tableCards: List<Card>, holeCards: List<Card>): Pair<CardsCombination, List<Card>> {
    val cards = tableCards + holeCards
    if (cards.isEmpty()){
        return Pair(CardsCombination.NONE, emptyList())
    }

    var check = checkEpicFlush(cards)
    if (check.first){
        if (check.second.any { it.value == 14 }){
            return Pair(CardsCombination.ROYAL_FLUSH, check.second)
        }
        return Pair(CardsCombination.STRAIGHT_FLUSH, check.second)
    }

    check = checkPoker(cards)
    if (check.first){
        return Pair(CardsCombination.POKER, check.second)
    }

    var check2 = checkFullHouse(cards)
    if (check2.first){
        return Pair(CardsCombination.FULL_HOUSE, check2.second)
    }

    val check3 = checkFlush(cards)
    if (check3.first){
        return Pair(CardsCombination.FLUSH, check3.second.takeLast(5))
    }

    check = checkStraight(cards)
    if (check.first){
        return Pair(CardsCombination.STRAIGHT, check.second)
    }

    check = checkTrips(cards)
    if (check.first){
        return Pair(CardsCombination.TRIPS, check.second)
    }

    check2 = checkTwoPairs(cards)
    if (check2.first){
        return Pair(CardsCombination.TWO_PAIRS, check2.second)
    }

    check = checkPair(cards)
    if (check.first){
        return Pair(CardsCombination.PAIR, check.second)
    }

    val highestCard = holeCards.filter { it.value == holeCards.maxOf { it.value } }
    return Pair(CardsCombination.CARD, highestCard)
}


fun evaluateGame(players: List<PlayerState>, tableCards: List<Card>): Pair<List<PlayerState>, Pair<CardsCombination, List<List<Card>>>> {
    val combinations: MutableMap<PlayerState, Pair<CardsCombination, List<Card>>> = mutableMapOf()
    for (player in players){
        combinations[player] = evaluatePlayerCards(tableCards, player.holeCards)
    }

    val best = mutableMapOf<PlayerState, Pair<CardsCombination, List<Card>>>()
    for(pair in combinations){
        if (pair.value.first.ordinal == combinations.values.minOf { it.first.ordinal }){
            best[pair.key] = pair.value
        }
    }

    when (best.values.first().first){
        CardsCombination.ROYAL_FLUSH -> {
            if (best.values.all { it.second == best.values.first().second }){
                return Pair(best.keys.toList(), Pair(CardsCombination.ROYAL_FLUSH, listOf(best.values.first().second)))
            }
            // Only one possible winner
            return Pair(listOf(best.keys.first()),  Pair(CardsCombination.ROYAL_FLUSH, listOf(best.values.first().second)))
        }

        CardsCombination.STRAIGHT_FLUSH -> {
            if (best.values.all { it.second == best.values.first().second }){
                return Pair(best.keys.toList(),  Pair(CardsCombination.STRAIGHT_FLUSH, listOf(best.values.first().second)))
            }

            // Only one winner, there can be more players with STRAIGHT_FLUSH, but only highest value win's.
            val highestCardValue = best.values.flatMap { it.second }.maxBy { it.value }.value
            val winner =  best.filter { it.value == best.values.first { it.second.any { it.value == highestCardValue } } }.toList().first()
            return Pair(listOf(winner.first), Pair(CardsCombination.STRAIGHT_FLUSH, listOf(winner.second.second)))
        }

        CardsCombination.POKER -> {
            if (best.values.all { it.second == best.values.first().second }){
                // Poker is made from 4 cards on Table - Find Kicker card and compare

                val cardsToRemove = best.values.flatMap { it.second }.distinct()
                val tableC: MutableList<Card> = (tableCards).toMutableList()
                tableC.removeAll(cardsToRemove)

                var playersCards = best.map { it.key to it.key.holeCards }
                    .map {
                        val d = it.second.toMutableList()
                        d.removeAll(cardsToRemove)
                        it.first to (d + tableC)
                    }
                val bigKicker = playersCards.flatMap { it.second }.maxBy { it.value }

                playersCards = playersCards.filter { it.second.any { it.value == bigKicker.value } }
                if (playersCards.size == 1){
                    // One winner with highest BigKicker
                    return Pair(listOf(playersCards.first().first), Pair(CardsCombination.POKER, listOf(best.values.first().second + bigKicker)))
                }
                // There can be more players with same value Kicker card
                val kickerCards = playersCards.map { it.second.maxBy { it.value } }
                val cards = kickerCards.map {
                    best.values.first().second + it
                }
                return Pair(playersCards.map { it.first }.toList(), Pair(CardsCombination.POKER, cards))
            }

            // Only one winner, there can be more players with POKER, but only highest value win's.
            val bestComb = best.values.maxBy { it.second.maxOf { it.value } }
            val winner = best.filter { it.value.second[0].value == bestComb.second.first().value }.toList().first()
            return Pair(listOf(winner.first), Pair(CardsCombination.POKER, listOf(winner.second.second)))
        }

        CardsCombination.FULL_HOUSE -> {
            // Get list of all card values
            val allValues = best.values.flatMap { it.second.map { it.value } }
            // If all cards are same by its value, it means everyone has same combination -> split bank
            if (best.values.all { it.second.map { it.value }.containsAll(allValues) }){
                return Pair(best.keys.toList(),  Pair(CardsCombination.FULL_HOUSE, listOf(best.values.flatMap { it.second }.toSet().toList())))
            }

            // Take all combinations which first's card value == highest card from all first cards of combinations
            val bestT = best.values.filter { it.second.first().value == best.values.maxOf { it.second.first().value } }
            if (bestT.size == 1){
                val winner = best.filter { it.value == bestT.first() }.toList().first()
                return Pair(listOf(winner.first), Pair(CardsCombination.FULL_HOUSE, listOf(winner.second.second)))
            }

            // Take all combinations which last's card value == highest card from all last cards of combinations
            val bestP = best.values.filter { it.second[4].value == best.values.maxOf { it.second[4].value } }
            // Only one player can now win, if multiple of them has even Pair in same values, the first condition will handle it (split bank)
            val winner = best.filter { it.value == bestP.first() }.toList().first()
            return Pair(listOf(winner.first), Pair(CardsCombination.FULL_HOUSE, listOf(winner.second.second)))
        }

        CardsCombination.FLUSH -> {
            val allValues = best.values.flatMap { it -> it.second.map { it.value } }
            if (best.values.all { it -> it.second.map { it.value }.containsAll(allValues) }){
                return Pair(best.keys.toList(),  Pair(CardsCombination.FLUSH, listOf(best.values.first().second)))
            }

            // Flat cards to single list and sort it in descending order (highest first)
            val allCardsDesc = best.values.map { it.second }.flatMap { it -> it.sortedByDescending { it.value } }.sortedByDescending { it.value }
            var differentCard: Card? = null
            // Make Map from cards list grouped by card value
            val groups = allCardsDesc.groupBy { it.value }
            for (g in groups){
                // If there is only one card in the value group, it means it was first highest card found -> making its owner a winner
                if (g.value.size == 1){
                    differentCard = g.value.first()
                    break
                }
            }
            // This condition should never happen
            if (differentCard == null){
                return Pair(best.keys.toList(),  Pair(CardsCombination.FLUSH, listOf(best.values.first().second)))
            }

            val p = best.values.first { it.second.contains(differentCard) }
            val winner = best.filter { it.value == p }.toList().first()
            return Pair(listOf(winner.first), Pair(CardsCombination.FLUSH, listOf(winner.second.second)))
        }

        CardsCombination.STRAIGHT -> {
            val allValues = best.values.flatMap { it -> it.second.map { it.value } }
            if (best.values.all { it -> it.second.map { it.value }.containsAll(allValues) }){
                val cards = best.values.map { it.second }
                return Pair(best.keys.toList(),  Pair(CardsCombination.STRAIGHT, cards))
            }

            // Highest value from all straight hands
            val highestValue = best.values.flatMap { it.second }.maxOf { it.value }
            // There can be more players who hold Straight with same highest card
            val winners = best.filter { it.value.second.maxOf { it.value } == highestValue }
            val cards = winners.values.map { it.second }
            return Pair(winners.keys.toList(), Pair(CardsCombination.STRAIGHT, cards))
        }

        CardsCombination.TRIPS -> {
            // Highest card from all Trips
            val highestValue = best.values.flatMap { it.second }.maxOf { it.value }

            // There can be more players with same Trips combination
            val highestCombinations = best.filter { it.value.second.maxOf { it.value } == highestValue }
            if (highestCombinations.size == 1){
                // Only one player with highest card -> winner
                return Pair(listOf(highestCombinations.keys.first()), Pair(CardsCombination.TRIPS, listOf(highestCombinations.values.first().second)))
            }

            // More players with highest card, check BigKicker
            val cardsToRemove = highestCombinations.values.flatMap { it.second }.distinct()

            val tableC: MutableList<Card> = (tableCards).toMutableList()
            tableC.removeAll(cardsToRemove)

            var playersCards = highestCombinations.map { it.key to it.key.holeCards }
                .map {
                    val d = it.second.toMutableList()
                    d.removeAll(cardsToRemove)
                    it.first to (d + tableC)
                }
            val bigKicker = playersCards.flatMap { it.second }.maxBy { it.value }
            val bigKickerCards = playersCards.map { it.second.maxBy { it.value } }

            playersCards = playersCards.filter { it.second.any { it.value == bigKicker.value } }
            if (playersCards.size == 1){
                // One winner with highest BigKicker
                val winner = best.filter { it.key == playersCards.first().first }
                return Pair(listOf(playersCards.first().first), Pair(CardsCombination.TRIPS, listOf(winner.values.first().second + bigKicker)))
            }

            // More players have same Trips and also BigKicker, check for SmallKicker
            // Remove BigKicker from players
            playersCards = playersCards.map {
                val d = it.second.toMutableList()
                d.removeIf { it.value == bigKicker.value }
                it.first to d
            }
            val smallKicker = playersCards.flatMap { it.second }.maxBy { it.value }
            val smallKickerCards = playersCards.map { it.second.maxBy { it.value } }

            playersCards = playersCards.filter { it.second.any { it.value == smallKicker.value } }
            if (playersCards.size == 1){
                // One winner with highest SmallKicker
                val winner = best.filter { it.key == playersCards.first().first }
                return Pair(listOf(winner.keys.first()), Pair(CardsCombination.TRIPS, listOf(winner.values.first().second + bigKicker + smallKicker)))
            }

            // More players have same Trips, BigKicker and SmallKicker too -> split bank
            val winners = best.filter { playersCards.map { it.first }.contains(it.key) }
            val cards = winners.values.map { it.second }.mapIndexed { index, it ->
                val d = it.toMutableList()
                d.addAll(listOf(bigKickerCards[index], smallKickerCards[index]))
                d
            }

            // Cards are in format: 1. player's: strip cards + his bigKicker + his smallKicker
            //                      2. player's: strip cards + his bigKicker + his smallKicker
            // All together -> first 5 cards for 1. player, another 5 cards for 2. player...
            return Pair(playersCards.map { it.first }.toList(), Pair(CardsCombination.TRIPS, cards))
        }

        CardsCombination.TWO_PAIRS -> {
            // Highest card from all TwoPairs
            val highestValue = best.values.flatMap { it.second }.maxOf { it.value }

            // There can be more players with same TwoPairs combination
            val highestCombinations = best.filter { it.value.second.maxOf { it.value } == highestValue }
            if (highestCombinations.size == 1){
                // Only one player with highest pair card -> winner
                return Pair(listOf(highestCombinations.keys.first()), Pair(CardsCombination.TWO_PAIRS, listOf(highestCombinations.values.first().second)))
            }

            val secondHighestValue = highestCombinations.values.flatMap { it.second }.sortedByDescending { it.value }.first { it.value < highestValue }.value
            val secondHighestCombinations = highestCombinations.filter { it.value.second.any { it.value == secondHighestValue }}
            if (secondHighestCombinations.size == 1){
                // Only one player with highest SECOND pair card -> winner
                return Pair(listOf(secondHighestCombinations.keys.first()), Pair(CardsCombination.TWO_PAIRS, listOf(secondHighestCombinations.values.first().second)))
            }

            // More players with same pairs
            // Take a look for Kicker card from table and hands
            val cardsToRemove = secondHighestCombinations.values.flatMap { it.second }.distinct()
            val tableC: MutableList<Card> = tableCards.toMutableList()
            tableC.removeAll(cardsToRemove)

            var playersCards = secondHighestCombinations.map { it.key to it.key.holeCards }
                .map {
                    val d = it.second.toMutableList()
                    d.removeAll(cardsToRemove)
                    it.first to (d + tableC)
                }

            val kicker = playersCards.flatMap { it.second }.maxBy { it.value }
            val kickerCards = playersCards.map { it.second.maxBy { it.value } }

            playersCards = playersCards.filter { it.second.any { it.value == kicker.value } }
            if (playersCards.size == 1){
                // One winner with highest kicker
                val winner = best.filter { it.key == playersCards.first().first }
                return Pair(listOf(winner.keys.first()), Pair(CardsCombination.TWO_PAIRS, listOf(winner.values.first().second + kicker)))
            }

            // Multiple winners which has same two pairs and also same kicker card -> split bank
            val winners = best.filter { playersCards.map { it.first }.contains(it.key) }
            val cards = winners.values.map { it.second }.mapIndexed { index, it ->
                val d = it.toMutableList()
                d.addAll(listOf(kickerCards[index]))
                d
            }

            // Cards are in format: 1. player's: two pairs cards + kicker
            //                      2. player's: two pairs cards + kicker
            // All together -> first 5 cards for 1. player, another 5 cards for 2. player...
            return Pair(playersCards.map { it.first }.toList(), Pair(CardsCombination.TWO_PAIRS, cards))
        }

        CardsCombination.PAIR -> {
            // Highest card from all Pairs
            val highestValue = best.values.flatMap { it.second }.maxOf { it.value }

            // There can be more players with same Pairs combination
            val highestCombinations = best.filter { it.value.second.maxOf { it.value } == highestValue }
            if (highestCombinations.size == 1){
                // Only one player with highest pair -> winner
                return Pair(listOf(highestCombinations.keys.first()), Pair(CardsCombination.PAIR, listOf(highestCombinations.values.first().second)))
            }

            // More players with same Pair, look for first Kicker
            val cardsToRemove = highestCombinations.values.flatMap { it.second }.distinct()
            val tableC: MutableList<Card> = tableCards.toMutableList()
            tableC.removeAll(cardsToRemove)

            var playersCards = highestCombinations.map { it.key to it.key.holeCards }
                .map {
                    val d = it.second.toMutableList()
                    d.removeAll(cardsToRemove)
                    it.first to (d + tableC)
                }
            val kickerOne = playersCards.flatMap { it.second }.maxBy { it.value }
            val kickerOneCards = playersCards.map { it.second.maxBy { it.value } }

            playersCards = playersCards.filter { it.second.any { it.value == kickerOne.value } }
            if (playersCards.size == 1){
                // One winner with first highest kicker
                val winner = best.filter { it.key == playersCards.first().first }
                return Pair(listOf(winner.keys.first()), Pair(CardsCombination.PAIR, listOf(winner.values.first().second + kickerOne)))
            }

            // 2x repeat, so far compared 3 cards, 2 to 5 missing
            return findWinnerRecursion(
                best,
                playersCards,
                kickerOne,
                mutableListOf(kickerOne),
                mutableListOf(kickerOneCards),
                CardsCombination.PAIR,
                2
            )
        }

        CardsCombination.CARD -> {
            val highestValue = best.values.flatMap { it.second }.maxOf { it.value }

            // There can be more players with same Highest card combination
            val highestCombinations = best.filter { it.value.second.maxOf { it.value } == highestValue }
            if (highestCombinations.size == 1){
                // Only one player with first highest card -> winner
                return Pair(listOf(highestCombinations.keys.first()), Pair(CardsCombination.CARD, listOf(highestCombinations.values.first().second)))
            }


            // More players with same first H card, look for second highest
            val cardsToRemove = highestCombinations.values.flatMap { it.second }.distinct()
            val tableC: MutableList<Card> = tableCards.toMutableList()
            tableC.removeAll(cardsToRemove)

            var playersCards = highestCombinations.map { it.key to it.key.holeCards }
                .map {
                    val d = it.second.toMutableList()
                    d.removeAll(cardsToRemove)
                    it.first to (d + tableC)
                }
            val kickerOne = playersCards.flatMap { it.second }.maxBy { it.value }
            val kickerOneCards = playersCards.map { it.second.maxBy { it.value } }

            playersCards = playersCards.filter { it.second.any { it.value == kickerOne.value } }
            if (playersCards.size == 1){
                // One winner with second highest kicker
                val winner = best.filter { it.key == playersCards.first().first }
                return Pair(listOf(winner.keys.first()), Pair(CardsCombination.CARD, listOf(winner.values.first().second + kickerOne)))
            }

            // 3x repeat, so far compared 2 cards, 3 to 5 missing
            return findWinnerRecursion(
                best,
                playersCards,
                kickerOne,
                mutableListOf(kickerOne),
                mutableListOf(kickerOneCards),
                CardsCombination.CARD,
                3
            )
        }

        else -> {
            return Pair(emptyList(), Pair(CardsCombination.NONE, emptyList()))
        }
    }
}


fun findWinnerRecursion(
    best: MutableMap<PlayerState, Pair<CardsCombination, List<Card>>>,
    playersCards: List<Pair<PlayerState, List<Card>>>,
    removeVal: Card,
    previousKickers: MutableList<Card>,
    previousAllCards: MutableList<List<Card>>,
    cardCombination: CardsCombination,
    repeats: Int = 0
): Pair<List<PlayerState>, Pair<CardsCombination, List<List<Card>>>> {
    // Remove card from players
    var newPlayersCards = playersCards.map {
        val d = it.second.toMutableList()
        d.removeIf { it.value == removeVal.value }
        it.first to d
    }
    val kicker = newPlayersCards.flatMap { it.second }.maxBy { it.value }
    val kickerCards = newPlayersCards.map { it.second.maxBy { it.value } }

    // Find players with same value kicker (highest) card
    newPlayersCards = newPlayersCards.filter { it.second.any { it.value == kicker.value } }
    if (playersCards.size == 1){
        // One winner with best (x) Kicker(s)
        val winner = best.filter { it.key == playersCards.first().first }
        return Pair(listOf(winner.keys.first()), Pair(cardCombination, listOf(winner.values.first().second + previousKickers)))
    }
    if (repeats == 0){
        val winners = best.filter { playersCards.map { it.first }.contains(it.key) }
        val cards = winners.values.map { it.second }.mapIndexed { index, it ->
            val d = it.toMutableList()
            d.addAll(previousAllCards.map { it[index] })
            d
        }
        // All together -> first 5 cards for 1. player, another 5 cards for 2. player...
        return Pair(playersCards.map { it.first }.toList(), Pair(cardCombination, cards))
    }

    previousKickers.add(kicker)
    previousAllCards.add(kickerCards)
    val r = repeats - 1
    return findWinnerRecursion(
        best, newPlayersCards, kicker, previousKickers, previousAllCards, cardCombination, r
    )
}