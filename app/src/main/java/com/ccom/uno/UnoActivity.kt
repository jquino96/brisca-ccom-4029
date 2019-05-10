package com.ccom.uno

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CountDownLatch

class UnoActivity : AppCompatActivity() {
    var playerID: String? = null
    var gameID: String? = null
    var isDealer: Boolean? = null

    var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uno)
        val extras = intent.extras
        if (extras == null) {
            Log.e("UNOact", "Arguments not received")
            throw Exception("Arguments not received")
        } else {
            playerID = extras.getString("PLAYER_ID")
            gameID = extras.getString("GAME_ID")
        }

        val handRecyclerView = findViewById<RecyclerView>(R.id.hand_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@UnoActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = CardAdapter(null)
        }

        val db = FirebaseFirestore.getInstance()
        Log.d("DB", "Connected")

        if (gameID != null && playerID != null) {
            val gameRef = db.collection("game").document(gameID!!)
            val dbHelper = UnoDBHelper(gameID!!)

            var hand: MutableList<UnoCard>
            Log.d("dbHelper", "Waiting for dealer")
            db.runTransaction {trs ->
                val game = trs.get(gameRef).toObject(Game::class.java)
                Log.d("PlayerID", playerID)
                Log.d("DealerID", game?.players?.get(game.turn))
                if (game != null && game.players[game.turn] == playerID) {
                    dbHelper.dbDealCards(UnoCard.createDeck()).addOnSuccessListener {
                        Log.d("dbHelper", "Cards dealt")
                        hand = trs.get(gameRef).toObject(Game::class.java)!!.hands[playerID!!]!!.map { UnoCard.jsonDecode(it) } as MutableList<UnoCard>
                        handRecyclerView.adapter = CardAdapter(hand)
                    }
                } else {
                    val handExistsLatch = CountDownLatch(1)
                    val gameReg = gameRef.addSnapshotListener {snap, err ->
                        if (snap != null && snap.exists() && snap.data != null) {
                            val gameSnap = snap.toObject(Game::class.java)
                            if (gameSnap != null && gameSnap.hands[playerID!!]!!.size != 0) {
                                hand = gameSnap.hands[playerID!!]!!.map { UnoCard.jsonDecode(it)} as MutableList<UnoCard>
                                handRecyclerView.adapter = CardAdapter(hand)
                                handExistsLatch.countDown()
                            }
                        }
                        if (err != null) {
                            handExistsLatch.countDown()
                        }
                    }
                    Thread {
                        Log.d("handExistsLatch", "Waiting for dealer")
                        handExistsLatch.await()
                        gameReg.remove()
                    }.start()
                }
            }


//            val deck = UnoCard.createDeck()
//            dbHelper.dbDealCards(deck).addOnSuccessListener {
//
//            }
//
//            val deckCreatedLatch = CountDownLatch(1)
//            gameRef.update("deck", jsonDeck).addOnSuccessListener { deckCreatedLatch.countDown() }
//                .addOnFailureListener { finished = true }
//            dbHelper.dbTakeTurn()
////            while (!finished) {
////                dbHelper.dbTakeTurn()
////            }
        } else {
            Log.e("UnoAct", "PlayerID or GameID null")
        }
    }
}
