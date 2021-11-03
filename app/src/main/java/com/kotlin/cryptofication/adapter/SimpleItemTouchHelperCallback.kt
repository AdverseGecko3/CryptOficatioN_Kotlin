package com.kotlin.cryptofication.adapter

import android.graphics.Canvas
import android.os.Build
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp.Companion.mResources
import com.kotlin.cryptofication.utilities.doHaptic
import kotlin.math.roundToInt

class SimpleItemTouchHelperCallback(
    private val mAdapterSwipe: ITHSwipe,
    private val selectedChangeEvent: SelectedChangeListener,
    private val fragment: String
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    private var enteredMoreSwipe = false

    interface SelectedChangeListener {
        fun onSelectedChange(swipingState: Boolean)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapterSwipe.onItemSwiped(direction, viewHolder)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        selectedChangeEvent.onSelectedChange(actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        var rwSwipeBackground: ColorDrawable? = null
        var rwSwipeIcon: Drawable? = null
        when (fragment) {
            "market" -> {
                rwSwipeBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ColorDrawable(
                        CryptOficatioNApp.mAppContext.getColor(R.color.yellow_favorite)
                    )
                } else {
                    ColorDrawable(
                        mResources.getColor(R.color.yellow_favorite)
                    )
                }
                rwSwipeIcon = ContextCompat.getDrawable(
                    CryptOficatioNApp.mAppContext, R.drawable.ic_star
                )!!
            }
            "alerts" -> {
                rwSwipeBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ColorDrawable(
                        CryptOficatioNApp.mAppContext.getColor(R.color.red_delete)
                    )
                } else {
                    ColorDrawable(
                        mResources.getColor(R.color.red_delete)
                    )
                }
                rwSwipeIcon = ContextCompat.getDrawable(
                    CryptOficatioNApp.mAppContext, R.drawable.ic_delete
                )!!
            }
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get the item swiped and the margin size of the icon
            val itemView = viewHolder.itemView
            val iconMargin = (itemView.height - rwSwipeIcon!!.intrinsicHeight) / 2

            Log.d("onChildDraw", "dX:$dX - Half width:${-(itemView.width / 2)}")

            // If the item is swiped to the left
            if (dX > 0) {
                if (dX < itemView.width / 2) {
                    if (enteredMoreSwipe) {
                        enteredMoreSwipe = false
                    }
                    Log.d("onChildDraw", "left less")
                    rwSwipeBackground!!.alpha = ((dX.roundToInt()) * 255) / (itemView.width / 2)
                } else {
                    if (!enteredMoreSwipe) {
                        recyclerView.doHaptic()
                        enteredMoreSwipe = true
                    }
                    Log.d("onChildDraw", "left more")
                    rwSwipeBackground!!.alpha = 255
                }
                rwSwipeBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    dX.roundToInt(),
                    itemView.bottom
                )
                rwSwipeIcon.setBounds(
                    dX.roundToInt() - iconMargin - rwSwipeIcon.intrinsicWidth,
                    itemView.top + iconMargin,
                    dX.roundToInt() - iconMargin,
                    itemView.bottom - iconMargin
                )
            } else {
                if (dX > -itemView.width / 2) {
                    if (enteredMoreSwipe) {
                        enteredMoreSwipe = false
                    }
                    Log.d("onChildDraw", "right less")
                    rwSwipeBackground!!.alpha = ((dX.roundToInt()) * 255) / (-itemView.width / 2)
                } else {
                    if (!enteredMoreSwipe) {
                        recyclerView.doHaptic()
                        enteredMoreSwipe = true
                    }
                    Log.d("onChildDraw", "right more")
                    rwSwipeBackground!!.alpha = 255
                }
                rwSwipeBackground.setBounds(
                    itemView.right + dX.roundToInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                rwSwipeIcon.setBounds(
                    itemView.right + dX.roundToInt() + iconMargin,
                    itemView.top + iconMargin,
                    itemView.right + dX.roundToInt() + iconMargin + rwSwipeIcon.intrinsicHeight,
                    itemView.bottom - iconMargin
                )
            }

            // Finally draw the background
            c.save()
            rwSwipeBackground.draw(c)

            if (dX > 0) {
                c.clipRect(itemView.left, itemView.top, dX.roundToInt(), itemView.bottom)
            } else {
                c.clipRect(
                    itemView.right + dX.roundToInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
            }
            rwSwipeIcon.draw(c)
            c.restore()
        }

        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }


}