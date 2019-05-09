package com.ccom.uno

import java.util.*

class UnoGameLogic {
    // Returns true if card1 is playable on card2
    fun cmpCard(card1: UnoCard, card2: UnoCard) =
        card1.suit == card2.suit
                || card1.type == card2.type
                || card1.suit == UnoCard.Suit.WILD

    companion object {
        // Returns a new game object
        fun turnLogic(game: Game): Game {
            // TODO: Implementar logica de turno aqui
            return game
        }
    }
}