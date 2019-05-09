package com.ccom.uno

import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList

data class UnoCard(var suit: Suit, var type: Type) {
    enum class Suit(val value: Int) {
        RED(100),
        GREEN(200),
        BLUE(300),
        YELLOW(400),
        WILD(500)
    }

    enum class Type(val value: Int) {
        ZERO(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        DRAW_TWO(10),
        SKIP(11),
        REVERSE(12),
        WILD(13),
        WILD_DRAW_FOUR(14)
    }

    companion object {
        fun jsonEncode(card: UnoCard): Int = card.suit.value + card.type.value
        fun jsonDecode(value: Int): UnoCard {
            val suit = when (value / 100) {
                1 -> Suit.RED
                2 -> Suit.GREEN
                3 -> Suit.BLUE
                4 -> Suit.YELLOW
                5 -> Suit.WILD
                else -> throw IllegalArgumentException("Invalid card suit")
            }
            val type = when (value % 100) {
                0 -> Type.ZERO
                1 -> Type.ONE
                2 -> Type.TWO
                3 -> Type.THREE
                4 -> Type.FOUR
                5 -> Type.FIVE
                6 -> Type.SIX
                7 -> Type.SEVEN
                8 -> Type.EIGHT
                9 -> Type.NINE
                10 -> Type.DRAW_TWO
                11 -> Type.SKIP
                12 -> Type.REVERSE
                13 -> Type.WILD
                14 -> Type.WILD_DRAW_FOUR
                else -> throw IllegalArgumentException("Invalid card type")
            }
            return UnoCard(suit, type)
        }

        fun createDeck(): ArrayList<UnoCard> {
            val cards: MutableList<UnoCard> = ArrayList()
            Type.values().forEach { type ->
                when (type) {
                    Type.ZERO -> {
                        Suit.values().forEach { suit ->
                            if (suit !== Suit.WILD) cards.add(UnoCard(suit, type))
                        }
                    }
                    Type.ONE..Type.REVERSE -> {
                        Suit.values().forEach { suit ->
                            if (suit !== Suit.WILD) repeat(2) { cards.add(UnoCard(suit, type)) }
                        }
                    }
                    Type.WILD, Type.WILD_DRAW_FOUR -> {
                        repeat(4) { cards.add(UnoCard(Suit.WILD, type)) }
                    }
                    else -> throw IllegalArgumentException("Invalid card type")
                }
            }
            cards.shuffle()
            return cards as ArrayList<UnoCard>
        }
    }
}