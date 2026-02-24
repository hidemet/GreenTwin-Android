package com.ndumas.appdt.presentation.automation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.databinding.ItemWidgetAutomationBinding
import com.ndumas.appdt.presentation.home.model.DashboardItem

/**
 * Adapter dedicato per la lista automazioni con toggle di attivazione.
 */
class AutomationListAdapter(
    private val onToggle: (DashboardItem.AutomationWidget, Boolean) -> Unit,
    private val onItemClick: (DashboardItem.AutomationWidget) -> Unit,
) : ListAdapter<DashboardItem.AutomationWidget, AutomationListAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemWidgetAutomationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemWidgetAutomationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardItem.AutomationWidget) {
            binding.tvAutomationName.text = item.name
            binding.tvAutomationDesc.text = item.description

            // Imposta lo switch senza triggerare il listener
            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.isChecked = item.isActive

            // Listener per il toggle
            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                if (item.isActive != isChecked) {
                    onToggle(item, isChecked)
                }
            }

            // Nasconde elementi non necessari in questa vista
            binding.ivRemoveAction.visibility = View.GONE
            binding.ivDragHandle.visibility = View.GONE

            // Click sulla card per aprire i dettagli
            binding.cardContainer.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DashboardItem.AutomationWidget>() {
        override fun areItemsTheSame(
            oldItem: DashboardItem.AutomationWidget,
            newItem: DashboardItem.AutomationWidget,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DashboardItem.AutomationWidget,
            newItem: DashboardItem.AutomationWidget,
        ): Boolean = oldItem == newItem
    }
}
