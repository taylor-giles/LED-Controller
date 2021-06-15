package giles.util

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * An ItemTouchHelperCallback implementation to allow RecyclerView items to be dragged to reorder and swiped to dismiss
 *
 * @param adapter The ItemTouchHelperAdapter that will be using this callback
 * @param isLongPressDraggable Determines whether or not this callback should allow dragging initiated by long press, default true
 * @param isDraggable Determines whether or not this callback should support dragging, default true
 * @param isSwipable Determines whether or not this callback should support swiping, default true
 */
class ItemTouchHelperCallback @JvmOverloads constructor(
    val adapter: ItemTouchHelperAdapter,
    val isLongPressDraggable: Boolean = true,
    val isDraggable: Boolean = true,
    val isSwipable: Boolean = true
): ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = if(isDraggable) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
        val swipeFlags = if(isSwipable) ItemTouchHelper.START or ItemTouchHelper.END else 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        //Do not allow movement between items of different type
        if (viewHolder.itemViewType != target.itemViewType) {
            return false;
        }

        adapter.moveItem(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        return true //The movement has been completed by the adapter
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.swipeItem(viewHolder.absoluteAdapterPosition, direction)
    }

    override fun isItemViewSwipeEnabled() = isSwipable

    override fun isLongPressDragEnabled() = (isLongPressDraggable && isDraggable)
}


/**
 * Interface for RecyclerView Adapters that use ItemTouchHelperCallback.
 */
interface ItemTouchHelperAdapter {
    fun moveItem(fromPosition: Int, toPosition: Int){}
    fun swipeItem(position: Int, direction: Int){}
}

interface OnDragStartListener {
    fun onDragStart(viewHolder: RecyclerView.ViewHolder)
}