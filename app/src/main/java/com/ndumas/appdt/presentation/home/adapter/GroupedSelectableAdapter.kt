package com.ndumas.appdt.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.ItemDeviceSelectionBinding
import com.ndumas.appdt.presentation.home.model.SelectionItem

class GroupedSelectableAdapter(
    private val onItemClick: (String) -> Unit,
) : ListAdapter<SelectionItem, RecyclerView.ViewHolder>(DiffCallback()) {
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_DEVICE = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is SelectionItem.Header -> TYPE_HEADER
            is SelectionItem.SelectableDevice -> TYPE_DEVICE
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view as TextView)
            }

            TYPE_DEVICE -> {
                val binding = ItemDeviceSelectionBinding.inflate(inflater, parent, false)
                DeviceViewHolder(binding)
            }

            else -> {
                throw IllegalArgumentException("Invalid ViewType")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item = getItem(position)) {
            is SelectionItem.Header -> (holder as HeaderViewHolder).bind(item)
            is SelectionItem.SelectableDevice -> (holder as DeviceViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(
        private val textView: TextView,
    ) : RecyclerView.ViewHolder(textView) {
        fun bind(item: SelectionItem.Header) {
            textView.text = item.name
        }
    }

    inner class DeviceViewHolder(
        private val binding: ItemDeviceSelectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectionItem.SelectableDevice) {
            val device = item.device

            binding.tvName.text = device.name
            binding.tvInfo.text = device.type.name

            val uiStyle = device.type.getUiStyle()
            binding.ivIcon.setImageResource(uiStyle.iconRes)

            binding.checkbox.setOnCheckedChangeListener(null)
            binding.checkbox.isChecked = item.isSelected

            binding.root.setOnClickListener { onItemClick(device.id) }

            binding.checkbox.setOnClickListener { onItemClick(device.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SelectionItem>() {
        override fun areItemsTheSame(
            oldItem: SelectionItem,
            newItem: SelectionItem,
        ): Boolean =
            when {
                oldItem is SelectionItem.Header && newItem is SelectionItem.Header -> {
                    oldItem.name == newItem.name
                }

                oldItem is SelectionItem.SelectableDevice && newItem is SelectionItem.SelectableDevice -> {
                    oldItem.device.id == newItem.device.id
                }

                else -> {
                    false
                }
            }

        override fun areContentsTheSame(
            oldItem: SelectionItem,
            newItem: SelectionItem,
        ): Boolean = oldItem == newItem
    }
}
