package com.ndumas.appdt.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.ItemAutomationSelectionBinding
import com.ndumas.appdt.presentation.home.model.SelectableAutomationItem

class AutomationSelectionAdapter(
    private val onItemClick: (String) -> Unit,
) : ListAdapter<SelectableAutomationItem, AutomationSelectionAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemAutomationSelectionBinding.inflate(
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
        private val binding: ItemAutomationSelectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectableAutomationItem) {
            val context = binding.root.context
            val automation = item.automation

            // Nome
            binding.tvAutomationName.text = automation.name

            // Descrizione
            binding.tvAutomationDescription.text =
                automation.description.ifBlank {
                    "Nessuna descrizione"
                }

            // Icona: verde se attiva, grigia se inattiva
            val iconColor =
                if (automation.isActive) {
                    ContextCompat.getColor(context, R.color.tw_lime_500)
                } else {
                    ContextCompat.getColor(context, R.color.tw_gray_400)
                }
            binding.ivAutomationIcon.setColorFilter(iconColor)

            // Checkbox
            binding.checkboxAutomation.setOnCheckedChangeListener(null)
            binding.checkboxAutomation.isChecked = item.isSelected

            // Click listeners
            val clickAction = {
                onItemClick(automation.id)
            }

            binding.root.setOnClickListener { clickAction() }
            binding.checkboxAutomation.setOnClickListener { clickAction() }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SelectableAutomationItem>() {
        override fun areItemsTheSame(
            oldItem: SelectableAutomationItem,
            newItem: SelectableAutomationItem,
        ): Boolean = oldItem.automation.id == newItem.automation.id

        override fun areContentsTheSame(
            oldItem: SelectableAutomationItem,
            newItem: SelectableAutomationItem,
        ): Boolean = oldItem == newItem
    }
}
