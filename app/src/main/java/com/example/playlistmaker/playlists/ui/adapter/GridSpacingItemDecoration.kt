package com.example.playlistmaker.playlists.ui.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val column = parent.getChildAdapterPosition(view) % spanCount

        outRect.left = spacing * column / spanCount
        outRect.right = spacing * (spanCount - 1 - column) / spanCount

        if (parent.getChildAdapterPosition(view) >= spanCount) {
            outRect.top = spacing
        }
    }
}
