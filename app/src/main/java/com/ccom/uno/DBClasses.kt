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

data class Game(val room: String?,
                val players: List<String>?,
                val deck: List<Int>?,
                val played: List<Int>?,
                val turn: Int?,
                val hands: HashMap<String, Any>?)