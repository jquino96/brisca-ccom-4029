package com.ccom.uno

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.firestore.*

import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CountDownLatch
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val displayName = findViewById<EditText>(R.id.player_name)
        val roomSize = findViewById<EditText>(R.id.room_size)
        val findGameButton = findViewById<Button>(R.id.find_game_button)

        val db = FirebaseFirestore.getInstance()

        var roomRef: DocumentReference? = null
        var playerID: String? = null
        var gameID: String? = null
        findGameButton.setOnClickListener { view ->
            val player = HashMap<String, Any>()
            player["name"] = displayName.text.toString()
            player["room_size"] = roomSize.text.toString().toInt()
            Log.d("MainAct", "Button clicked")
            if (playerID == null) {
                db.collection("player")
                    .add(player)
                    .addOnSuccessListener { playerRef ->
                        Log.d("MainAct", "PlayerCreate Listener added")
                        playerID = playerRef.id
                        val roomRefLatch = CountDownLatch(1)
                        Toast.makeText(this, "Looking for game", Toast.LENGTH_LONG).show()
                        val playerRegistration = playerRef.addSnapshotListener { playerSnap, playerErr ->
                            Log.d("PlayerSnap", "New snapshot")
                            if (playerSnap != null && playerSnap.exists() && playerSnap.data != null) {
                                val roomID = playerSnap.data?.get("room") as String?
                                Log.d("PlayerSnap", "Room id: $roomID")
                                if (roomID != null) {
                                    roomRef = db.collection("room").document(roomID)
                                    roomRefLatch.countDown()
                                    Log.d("roomRefLatch", "Released")
                                }
                            }
                        }
                        val gameIDLatch = CountDownLatch(1)
                        Thread {
                            roomRefLatch.await()
                            playerRegistration.remove()
                            val roomRegistration = roomRef!!.addSnapshotListener { snap, _ ->
                                if (snap != null && snap.exists() && snap.data != null) {
                                    gameID = snap.data?.get("game") as String?
                                    if (gameID != null) {
                                        gameIDLatch.countDown()
                                        Log.d("gameID", "Game created $gameID")

                                        Toast.makeText(this, "GAME: $gameID", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            Thread {
                                Log.d("gameID", "Waiting for game id")
                                gameIDLatch.await()
                                Log.d("gameIDLatch", "Released")
                                roomRegistration.remove()
                                startActivity(
                                    Intent(this, UnoActivity::class.java)
                                        .putExtra("GAME_ID", gameID)
                                        .putExtra("PLAYER_ID", playerID)
                                )
                            }.start()
                        }.start()
                    }
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
