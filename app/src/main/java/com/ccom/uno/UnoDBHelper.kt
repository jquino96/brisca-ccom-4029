package com.ccom.uno

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.CountDownLatch

class UnoDBHelper(val gameID: String) {
    val db = FirebaseFirestore.getInstance()

    fun dbDealCards(deck: MutableList<UnoCard>): Task<Unit> {
        val gameRef = db.collection("game").document(gameID)
        return db.runTransaction { trs ->
            val game = trs.get(gameRef).toObject(Game::class.java)
            if (game != null) {
                Log.d("dbHelper", "Dealing cards")
                for (i in 1..7) {
                    for (player in game.players) {
                        game.hands[player]!!.add(deck.last().jsonEncode())
                        deck.removeAt(deck.lastIndex)
                    }
                }
                game.played.add(deck.last().jsonEncode())
                deck.removeAt(deck.lastIndex)
                game.deck = deck.map { it.jsonEncode() } as MutableList<Int>
                Log.d("dbHelper", "Updating game")
                trs.set(gameRef, game)
            }
        }
    }

    fun dbTakeTurn() {
        val gameRef = db.collection("game").document(gameID)
        db.runTransaction { trs ->
            val game = trs.get(gameRef).toObject(Game::class.java)
            if (game != null) {
                trs.set(gameRef, UnoGameLogic.turnLogic(game))
            }
        }
    }

    fun dbGetHand(): Task<DocumentSnapshot> {
        return db.collection("game").document(gameID).get()
    }


}