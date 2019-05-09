package com.ccom.uno

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class UnoActivity : AppCompatActivity() {
    var playerID: String? = null
    var gameID: String? = null
    var isDealer: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uno)
        val extras = intent.extras
        if (extras == null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            playerID = extras.getString("PLAYER_ID")
            gameID = extras.getString("GAME_ID")
        }

        val db = FirebaseFirestore.getInstance()

        val gameRef = db.collection("game").document(gameID!!)
        val playerRef = db.collection("player").document(playerID!!)
            .apply {
                get().addOnSuccessListener { playerSnap ->
                    isDealer = playerSnap.data?.get("dealer") as Boolean?
                    if (isDealer != null && isDealer as Boolean) {
                        val deck = UnoCard.createDeck()
                        val valueDeck = deck.map { UnoCard.jsonEncode(it) }
                        valueDeck.forEach { print(it)}
                        update("deck", valueDeck)
                    }
                    Toast.makeText(this@UnoActivity, "Dealer: " + isDealer.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }
}
