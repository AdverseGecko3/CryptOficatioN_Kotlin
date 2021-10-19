package com.kotlin.cryptofication.adapter

import android.graphics.Canvas
import android.os.Build
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.cryptofication.R
import com.kotlin.cryptofication.ui.view.CryptOficatioNApp

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlin.math.roundToInt

class SimpleItemTouchHelperCallback(
    adapter: ItemTouchHelperAdapter,
    srlMarketReload: SwipeRefreshLayout
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    private val mAdapter = adapter
    private val mSrlMarketReload = srlMarketReload

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapter.onItemSwiped(direction, viewHolder)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        val swiping = actionState == ItemTouchHelper.ACTION_STATE_SWIPE
        mSrlMarketReload.isEnabled = !swiping
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
        lateinit var rwSwipeBackground: ColorDrawable
        val rwSwipeIcon =
            AppCompatResources.getDrawable(CryptOficatioNApp.appContext, R.drawable.ic_star)!!

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get the item swiped and the margin size of the icon
            val itemView = viewHolder.itemView
            val iconMargin = (itemView.height - rwSwipeIcon.intrinsicHeight) / 2

            Log.d("onChildDraw", "$dX ${-(itemView.width / 2)}")

            // If the item is swiped to the left
            if (dX > 0) {
                rwSwipeBackground = if (dX < itemView.width / 2) {
                    Log.d("onChildDraw", "left less")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ColorDrawable(CryptOficatioNApp.appContext.getColor(R.color.gray))
                    } else {
                        ColorDrawable(CryptOficatioNApp.appContext.resources.getColor(R.color.gray))
                    }
                } else {
                    Log.d("onChildDraw", "left more")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ColorDrawable(
                            CryptOficatioNApp.appContext.getColor(R.color.yellow_favorite)
                        )
                    } else {
                        ColorDrawable(CryptOficatioNApp.appContext.resources.getColor(R.color.yellow_favorite))
                    }
                }
                rwSwipeBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    dX.roundToInt(),
                    itemView.bottom
                )
                rwSwipeIcon.setBounds(
                    itemView.left + iconMargin,
                    itemView.top + iconMargin,
                    itemView.left + iconMargin + rwSwipeIcon.intrinsicHeight,
                    itemView.bottom - iconMargin
                )
            } else {
                rwSwipeBackground = if (dX > -itemView.width / 2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.d("onChildDraw", "right less")
                        ColorDrawable(CryptOficatioNApp.appContext.getColor(R.color.gray))
                    } else {
                        ColorDrawable(CryptOficatioNApp.appContext.resources.getColor(R.color.gray))
                    }
                } else {
                    Log.d("onChildDraw", "right more")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ColorDrawable(
                            CryptOficatioNApp.appContext.getColor(R.color.yellow_favorite)
                        )
                    } else {
                        ColorDrawable(CryptOficatioNApp.appContext.resources.getColor(R.color.yellow_favorite))
                    }
                }
                rwSwipeBackground.setBounds(
                    itemView.right + dX.roundToInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                rwSwipeIcon.setBounds(
                    itemView.right - iconMargin - rwSwipeIcon.intrinsicHeight,
                    itemView.top + iconMargin,
                    itemView.right - iconMargin,
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