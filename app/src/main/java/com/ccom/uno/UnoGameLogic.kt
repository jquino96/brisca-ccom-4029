package com.ccom.uno

class UnoGameLogic {
    // Returns true if card1 is playable on card2


    companion object {
        // Returns a new game object
        fun turnLogic(game: Game): Game {
            // TODO: Implementar logica de turno aqui
            return game
        }

        fun playCard(card: UnoCard, game: Game): Game? {
            val topCard = UnoCard.jsonDecode(game.played.last())
            if (!cmpCard(card, topCard))
                return null

            val cardVal = card.jsonEncode()
            val player = game.players[game.turn]
            game.hands[player]!!.remove(cardVal)
            game.played.add(cardVal)
            return game
        }

        fun drawCard(game: Game): Game {
            val player = game.players[game.turn]
            game.hands[player]!!.add(game.deck.last())
            game.deck.removeAt(game.deck.lastIndex)
            return game
        }

        private fun cmpCard(card1: UnoCard, card2: UnoCard) =
            card1.suit == card2.suit
                    || card1.type == card2.type
                    || card1.suit == UnoCard.Suit.WILD
    }
}