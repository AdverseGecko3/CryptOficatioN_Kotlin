package com.adversegecko3.cryptofication.adapter

import androidx.recyclerview.widget.RecyclerView

interface ITHSwipe {
    fun onItemSwiped(direction: Int, viewHolder: RecyclerView.ViewHolder)
}