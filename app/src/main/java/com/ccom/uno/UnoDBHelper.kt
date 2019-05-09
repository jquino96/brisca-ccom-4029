package com.ccom.uno

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UnoDBHelper(val gameID: String) {
    val db = FirebaseFirestore.getInstance()
    lateinit var roomSnap: DocumentSnapshot
    lateinit var room: Room
    lateinit var roomID: String

    init {
        db.collection("room")
            .whereEqualTo("game", gameID)
            .get().addOnSuccessListener { snap ->
                if (snap != null) {
                    roomSnap = snap.documents[0]
                    room = roomSnap.toObject(Room::class.java)!!
                    roomID = roomSnap.id
                }
            }
    }

    fun dbTakeTurn (dir: Int) {
        val gameRef = db.collection("game").document(gameID)
        gameRef.get().addOnSuccessListener { gameSnap ->
            if (gameSnap != null) {
                val game = gameSnap.toObject(Game::class.java)!!
                gameRef.set(UnoGameLogic.turnLogic(game))
            }
        }
    }


}