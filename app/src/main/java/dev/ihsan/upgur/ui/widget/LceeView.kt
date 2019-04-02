package dev.ihsan.upgur.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ViewFlipper
import dev.ihsan.upgur.data.remote.Resource

/**
 * A Loading, Content, Error, Empty State View to ease switching between states.
 */
class LceeView(context: Context, attrs: AttributeSet) : ViewFlipper(context, attrs) {

    fun showLoading() {
        setDisplayedChildSafe(INDEX_LOADING)
    }

    fun showContent() {
        setDisplayedChildSafe(INDEX_CONTENT)
    }

    fun showError() {
        setDisplayedChildSafe(INDEX_ERROR)
    }

    fun showEmptyState() {
        setDisplayedChildSafe(INDEX_EMPTY_STATE)
    }

    fun showBasedOnResource(resource: Resource<*>, usingSwipeRefresh: Boolean = false) {
        when (resource.status) {
            Resource.Status.LOADING -> {
                if (usingSwipeRefresh) showContent() else showLoading()
            }
            Resource.Status.SUCCESS -> {
                showContent()
            }
            Resource.Status.ERROR -> {
                // If data is not null, it means that its loaded from db.
                if (resource.data == null) showError() else showContent()
            }
            Resource.Status.EMPTY -> {
                showEmptyState()
            }
        }
    }

    private fun setDisplayedChildSafe(whichChild: Int) {
        if (displayedChild != whichChild) displayedChild = whichChild
    }

    companion object {
        private const val INDEX_LOADING = 0
        private const val INDEX_CONTENT = 1
        private const val INDEX_ERROR = 2
        private const val INDEX_EMPTY_STATE = 3
    }
}