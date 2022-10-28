package com.adversegecko3.cryptofication.adapter

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.adversegecko3.cryptofication.R
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mAppContext
import com.adversegecko3.cryptofication.ui.view.CryptOficatioNApp.Companion.mResources
import com.adversegecko3.cryptofication.utilities.doHaptic
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
        if (viewHolder.itemViewType == 1) return
        var rwSwipeBackground: ColorDrawable? = null
        var rwSwipeIcon: Drawable? = null
        when (fragment) {
            "market" -> {
                rwSwipeBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ColorDrawable(mAppContext.getColor(R.color.yellow_favorite))
                } else {
                    @Suppress("DEPRECATION")
                    ColorDrawable(mResources.getColor(R.color.yellow_favorite))
                }
                rwSwipeIcon = ContextCompat.getDrawable(mAppContext, R.drawable.ic_star_outlined)!!
            }
            "alerts" -> {
                rwSwipeBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ColorDrawable(mAppContext.getColor(R.color.red_delete))
                } else {
                    @Suppress("DEPRECATION")
                    ColorDrawable(mResources.getColor(R.color.red_delete))
                }
                rwSwipeIcon = ContextCompat.getDrawable(mAppContext, R.drawable.ic_delete)!!
            }
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get the item swiped and the margin size of the icon
            val itemView = viewHolder.itemView
            val iconMargin = (itemView.height - rwSwipeIcon!!.intrinsicHeight) / 2

            // If the item is swiped to the left
            if (dX > 0) {
                if (dX < itemView.width / 2) {
                    if (enteredMoreSwipe) {
                        // -0% to -50%
                        enteredMoreSwipe = false
                    }
                    rwSwipeBackground!!.alpha = ((dX.roundToInt()) * 255) / (itemView.width / 2)
                } else {
                    if (!enteredMoreSwipe) {
                        // -50% to -100%
                        recyclerView.doHaptic()
                        enteredMoreSwipe = true
                    }
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
                        // +0% to +50%
                        enteredMoreSwipe = false
                    }
                    rwSwipeBackground!!.alpha = ((dX.roundToInt()) * 255) / (-itemView.width / 2)
                } else {
                    if (!enteredMoreSwipe) {
                        // +50% to +100%
                        recyclerView.doHaptic()
                        enteredMoreSwipe = true
                    }
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