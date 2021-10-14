package com.kotlin.cryptofication.adapter

import androidx.recyclerview.widget.RecyclerView

interface ItemTouchHelperAdapter {
    fun onItemSwiped(direction: Int, viewHolder: RecyclerView.ViewHolder)
}