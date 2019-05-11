package com.ccom.uno

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(private val hand: List<UnoCard>?, private val onCardClick: View.OnClickListener?) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {
    inner class CardViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var cardView: Button = view.findViewById<Button>(R.id.player2).apply { setOnClickListener(onCardClick) }
    }

    override fun getItemCount(): Int = hand?.size ?: 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val card = LayoutInflater.from(parent.context)
            .inflate(R.layout.card, parent, false) as ConstraintLayout
        return CardViewHolder(card)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        if (hand == null) {
            holder.cardView.text = "UNO"
            return
        }
        holder.cardView.tag = hand[position]
        holder.cardView.text = hand[position].type.displayText
        holder.cardView.backgroundTintList = ColorStateList.valueOf(hand[position].suit.color)
        if (hand[position].suit == UnoCard.Suit.BLUE
            || hand[position].suit == UnoCard.Suit.WILD
            || hand[position].suit == UnoCard.Suit.RED)
            holder.cardView.setTextColor(Color.WHITE)
        else
            holder.cardView.setTextColor(Color.BLACK)
    }
}