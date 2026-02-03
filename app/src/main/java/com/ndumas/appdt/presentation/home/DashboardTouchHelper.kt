package com.ndumas.appdt.presentation.home

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class DashboardTouchHelper(
    private val adapter: DashboardAdapter,
    private val onOrderChanged: (List<String>) -> Unit,
) : ItemTouchHelper.Callback() {
    var isEditMode: Boolean = false

    companion object {
        private const val ALPHA_DRAGGING = 0.7f
        private const val ALPHA_FULL = 1.0f
        private const val NO_DRAG = 0
    }

    override fun isLongPressDragEnabled(): Boolean = false

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        // Se non siamo in edit mode, nessun drag consentito
        if (!isEditMode) {
            return makeMovementFlags(NO_DRAG, 0)
        }

        val position = viewHolder.bindingAdapterPosition

        val viewType =
            if (position != RecyclerView.NO_POSITION) {
                adapter.getItemViewType(position)
            } else {
                -1
            }

        val dragFlags =
            if (position != RecyclerView.NO_POSITION &&
                viewType != DashboardAdapter.TYPE_ENERGY &&
                viewType != DashboardAdapter.TYPE_HEADER
            ) {
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            } else {
                NO_DRAG
            }

        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        val fromPos = viewHolder.bindingAdapterPosition
        val toPos = target.bindingAdapterPosition

        if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION) return false

        //  Type Matching Check
        val dragType = viewHolder.itemViewType
        val targetType = target.itemViewType

        if (dragType != targetType) {
            return false
        }

        val list = ArrayList(adapter.currentList)
        if (fromPos < list.size && toPos < list.size) {
            Collections.swap(list, fromPos, toPos)
            adapter.submitList(list)
            return true
        }

        return false
    }

    override fun onSwiped(
        viewHolder: RecyclerView.ViewHolder,
        direction: Int,
    ) {
        // Non supportato
    }

    override fun onSelectedChanged(
        viewHolder: RecyclerView.ViewHolder?,
        actionState: Int,
    ) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.alpha = ALPHA_DRAGGING
        }
    }

    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = ALPHA_FULL

        val newOrderIds = adapter.currentList.map { it.id }
        onOrderChanged(newOrderIds)
    }
}
