package com.ccom.uno

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.android.synthetic.main.activity_uno.*
import java.util.concurrent.CountDownLatch

class UnoActivity : AppCompatActivity() {
    var playerID: String? = null
    var gameID: String? = null

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

        val playedCardView = findViewById<Button>(R.id.played_card)
        val player2Layout = findViewById<ConstraintLayout>(R.id.player2_layout)
        val player3Layout = findViewById<ConstraintLayout>(R.id.player3_layout)
        val player4Layout = findViewById<ConstraintLayout>(R.id.player4_layout)
        val playerLayouts = arrayListOf(player3Layout, player2Layout, player4Layout)

        val turnIndicatorView = findViewById<TextView>(R.id.turn_indicator)

        val handRecyclerView = findViewById<RecyclerView>(R.id.hand_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@UnoActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = CardAdapter(null, null)
        }

        val db = FirebaseFirestore.getInstance()
        Log.d("DB", "Connected")

        if (gameID != null && playerID != null) {
            val gameRef = db.collection("game").document(gameID!!)
            // On card played logic
            val onCardClick = View.OnClickListener {cardView ->
                db.runTransaction {trs ->
                    val game = trs.get(gameRef).toObject(Game::class.java)
                    if (game != null && game.players[game.turn] == playerID) {
                        Log.d("Turn", game.turn.toString())
                        Log.d("Player", game.players[game.turn])
                        val card = cardView.tag as UnoCard
                        if (card cmpCard UnoCard.jsonDecode(game.played.last())) {
                            game.played.add(card.jsonEncode())
                            game.hands[playerID!!]!!.remove(card.jsonEncode())
                            when (card.type) {
                                UnoCard.Type.REVERSE -> {
                                    game.dir = -(game.dir)
                                    game.turn = (game.turn + game.dir)%game.players.size
                                    if (game.turn < 0) game.turn += game.players.size
                                }
                                UnoCard.Type.SKIP -> {
                                    game.turn = (game.turn + game.dir * 2)%game.players.size
                                    if (game.turn < 0) game.turn += game.players.size
                                }
                                UnoCard.Type.DRAW_TWO -> {
                                    var drawerIndex = (game.turn + game.dir)%game.players.size
                                    if (drawerIndex < 0) drawerIndex += game.players.size
                                    game.hands[game.players[drawerIndex]]!!
                                        .addAll(game.deck.takeLast(2).also {
                                            game.deck = game.deck.dropLast(2) as MutableList
                                        })
                                    game.turn = (game.turn + game.dir * 2)%game.players.size
                                    if (game.turn < 0) game.turn += game.players.size
                                }
                                UnoCard.Type.WILD_DRAW_FOUR -> {
                                    var drawerIndex = (game.turn + game.dir)%game.players.size
                                    if (drawerIndex < 0) drawerIndex += game.players.size
                                    game.hands[game.players[drawerIndex]]!!
                                        .addAll(game.deck.takeLast(4).also {
                                            game.deck = game.deck.dropLast(4) as MutableList
                                        })
                                    game.turn = (game.turn + game.dir * 2)%game.players.size
                                    if (game.turn < 0) game.turn += game.players.size
                                }
                                else -> {
                                    game.turn = (game.turn + game.dir) % game.players.size
                                    if (game.turn < 0) game.turn += game.players.size
                                }
                            }
                            trs.set(gameRef, game)
                        } else {
                            throw FirebaseFirestoreException("Card not playable ${card.jsonEncode()} on ${game.played.last()}", FirebaseFirestoreException.Code.ABORTED)
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("CardClick", "Failed", e)
                }
            }

            // Initialize hands
            gameRef.get().addOnSuccessListener {snap ->
                val game = snap.toObject(Game::class.java)
                // Deal cards if first turn
                if (game != null && game.players[game.turn%game.players.size] == playerID) {
                    val deck = UnoCard.createDeck()
                    for (i in 1..7) {
                        for (player in game.players) {
                            game.hands[player]!!.add(deck.last().jsonEncode())
                            deck.removeAt(deck.lastIndex)
                        }
                    }
                    game.played.add(deck.last().jsonEncode())
                    deck.removeAt(deck.lastIndex)
                    game.deck = deck.map { it.jsonEncode() } as MutableList<Int>
                    gameRef.set(game)
                }
                // Initialize opponent hands
                if (game != null) {
                    var tmp = 0
                    for (i in 0 until game.players.size) {
                        if (game.players[i] != playerID) {
                            db.collection("player")
                                .document(game.players[i]).get()
                                .addOnSuccessListener {playerSnap ->
                                    val player = playerSnap.toObject(Player::class.java)
                                    if (player != null) {
                                        if (game.players.size == 2) {
                                            player2Layout.findViewById<TextView>(R.id.player2_name).text = player.name
                                            player2Layout.findViewById<Button>(R.id.player2_hand).text = game.hands[game.players[i]]!!.size.toString()
                                            player2Layout.visibility = View.VISIBLE
                                        } else {
                                            (playerLayouts[tmp].getChildAt(0) as TextView).text = player.name
                                            (playerLayouts[tmp].getChildAt(1) as Button).text = game.hands[game.players[i]]!!.size.toString()
                                            playerLayouts[tmp].visibility = View.VISIBLE
                                        }
                                    }
                                }
                            tmp++
                        }
                    }
                }
            }

            val gameFinishedLatch = CountDownLatch(1)
            // Run on game state change
            val gameThread = gameRef.addSnapshotListener { snap, e ->
                if (snap != null && snap.exists()) {
                    val game = snap.toObject(Game::class.java)!!

                    // Check if a player won
                    if (game.hands.any { it.value.isEmpty() }) {
                        Log.d("Game", "Finished")
                        gameFinishedLatch.countDown()
                    }

                    // Update player hand
                    val hand = game.hands[playerID!!]!!.map { UnoCard.jsonDecode(it) } as MutableList
                    handRecyclerView.adapter = CardAdapter(hand, if (game.players[game.turn%game.players.size] == playerID) onCardClick else null)

                    game.hands[playerID!!]!!.forEachIndexed { index, i ->
                        Log.d("Hand", "hand[$index]: $i")
                    }

                    // Update last played card
                    if (game.played.size != 0) {
                        val topCard = UnoCard.jsonDecode(game.played.last())
                        playedCardView.backgroundTintList = ColorStateList.valueOf(topCard.suit.color)
                        playedCardView.text = topCard.type.displayText
                        playedCardView.setTextColor(when (topCard.suit.color) {
                            Color.RED, Color.BLUE, Color.BLACK -> Color.WHITE
                            Color.GREEN, Color.YELLOW -> Color.BLACK
                            else -> Color.BLACK
                        })

                        // Draw if no cards playable
                        if (game.players[game.turn] == playerID) {
                            if (game.hands[playerID!!]!!.none { UnoCard.jsonDecode(it).cmpCard(UnoCard.jsonDecode(game.played.last())) }) {
                                Toast.makeText(this, "No cards playable. Drawing...", Toast.LENGTH_SHORT).show()
                                game.hands[playerID!!]!!.add(game.deck.last())
                                game.deck.removeAt(game.deck.lastIndex)
                                game.turn = ((game.turn + game.dir)%game.players.size)
                                if (game.turn < 0) game.turn += game.players.size
                                gameRef.set(game)
                            }
                        }
                    }

                    // Update opponent hands
                    var tmp = 0
                    for (i in 0 until game.players.size) {
                        if (game.players[i] != playerID) {
                            if (game.players.size == 2) {
                                player2Layout.findViewById<Button>(R.id.player2_hand).text = game.hands[game.players[i]]!!.size.toString()
                            } else {
                                (playerLayouts[tmp].getChildAt(1) as Button).text = game.hands[game.players[i]]!!.size.toString()
                            }
                            tmp++
                        }
                    }

                    // Check for an UNO
                    if (game.hands.any { it.value.size == 1 }) {
                        Toast.makeText(this, "UNO", Toast.LENGTH_SHORT).show()
                    }

                    // Wait for opponents
                    if (game.players[game.turn] == playerID) {
                        turn_indicator.text = "Your turn!"
                        // Reshuffle deck if out of cards
                        if (game.deck.isEmpty() && game.played.isNotEmpty()) {
                            game.deck = game.played.take(game.played.size - 1) as MutableList
                            game.deck.shuffle()
                            game.played = game.played.takeLast(1) as MutableList
                        }
                    } else {
                        turn_indicator.text = "Waiting for opponents . . ."
                    }
                }

            }
            Thread {
                gameFinishedLatch.await()
                gameThread.remove()
                startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            Log.e("UnoAct", "PlayerID or GameID null")
        }
    }
}
