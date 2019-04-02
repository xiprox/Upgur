package dev.ihsan.upgur.ui.widget

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.annotation.DimenRes

class ItemOffsetDecoration() : RecyclerView.ItemDecoration() {
    private var itemOffset: Int = 0

    constructor(itemOffset: Int) : this() {
        this.itemOffset = itemOffset
    }

    constructor(context: Context?, @DimenRes itemOffsetId: Int) : this(
        context?.resources?.getDimensionPixelSize(itemOffsetId) ?: 0
    )

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(itemOffset, itemOffset, itemOffset, itemOffset)
    }
}