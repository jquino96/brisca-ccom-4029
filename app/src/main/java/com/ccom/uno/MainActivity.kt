package com.ccom.uno

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.ccom.uno.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val db = FirebaseFirestore.getInstance()

        val player = HashMap<String, Any>()
        player["name"] = "Test"
        player["room_size"] = 2

        var roomID: String
        var gameID: String
        fab.setOnClickListener { view ->
            db.collection("player")
                .add(player)
                .addOnSuccessListener {playerRef ->
                    Toast.makeText(this, "Looking for game", Toast.LENGTH_SHORT)
                    val playerRegistration = playerRef.addSnapshotListener { playerSnap, playerErr ->
                        if (playerSnap != null && playerSnap.exists() && playerSnap.data != null) {
                            val isDealer = playerSnap.data?.get("dealer") as Boolean?
                            val roomID = playerSnap.data?.get("room").toString()
                            val roomRef = db.collection("room").document(roomID)
                            roomRef.addSnapshotListener { roomSnap, roomErr ->
                                if (roomSnap != null && roomSnap.exists() && roomSnap.data != null) {
                                    val gameID = roomSnap.data?.get("game")
                                    if (gameID != null) {
                                        startActivity(Intent(this, UnoActivity::class.java)
                                            .putExtra("GAME_ID", gameID.toString())
                                            .putExtra("PLAYER_ID", playerRef.id))
                                        Toast.makeText(this, "GAME: $gameID", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            roomRef.get().addOnSuccessListener {

                            }
                            Toast.makeText(this, roomID, Toast.LENGTH_SHORT).show()

                        }
                    }
                    playerRegistration.remove()
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
