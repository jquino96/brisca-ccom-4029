package com.ccom.uno

data class Room(val game: String?,
                     val players: List<String>?,
                     val full: Boolean?,
                     val size: Int?,
                     val max_size: Int?)

data class Player(val dealer: Boolean?,
                  val name: String?,
                  val room: String?,
                  val room_size: Int?)

data class Game(val players: List<String>?, // List of player IDs
                val deck: List<Int>?, // List of cards to draw
                val played: List<Int>?, // List of cards played
                val turn: Int?, // Index of current player
                val dir: Int?, // Direction to take turn [-2, 2]; 2 para skips
                val hands: HashMap<String, Any>?) // Map of player ID to array of cards