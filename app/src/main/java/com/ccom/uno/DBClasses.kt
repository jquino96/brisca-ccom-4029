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

data class Game(val players: List<String>, // List of player IDs
                var deck: MutableList<Int>, // List of cards to draw
                var played: MutableList<Int>, // List of cards played
                var turn: Int, // Index of current player
                var dir: Int, // Direction to take turn [-2, 2]; 2 para skips
                val hands: HashMap<String, MutableList<Int>>) // Map of player ID to array of cards