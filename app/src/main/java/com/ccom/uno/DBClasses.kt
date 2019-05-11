package com.ccom.uno

data class Room(val game: String?,
                     val players: List<String>?,
                     val full: Boolean?,
                     val size: Int?,
                     val max_size: Int?)

data class Player(val name: String = "",
                  val room: String = "",
                  val room_size: Int = 2)

data class Game(val players: List<String> = listOf(), // List of player IDs
                var deck: MutableList<Int> = mutableListOf(), // List of cards to draw
                var played: MutableList<Int> = mutableListOf(), // List of cards played
                var turn: Int = 0, // Index of current player
                var dir: Int = 1, // Direction to take turn [-2, -1] U [1, 2]; 2 para skips
                val hands: HashMap<String, MutableList<Int>> = hashMapOf()) // Map of player ID to array of cards