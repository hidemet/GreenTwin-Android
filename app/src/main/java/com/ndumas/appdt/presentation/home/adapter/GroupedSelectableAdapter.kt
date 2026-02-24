package com.ndumas.appdt.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.ItemRoomGroupBinding
import com.ndumas.appdt.presentation.home.model.SelectionUiItem.SelectionGroup

class GroupedSelectableAdapter(
    private val onItemClick: (String) -> Unit,
) : ListAdapter<SelectionGroup, GroupedSelectableAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemRoomGroupBinding.inflate(
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
        private val binding: ItemRoomGroupBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(group: SelectionGroup) {
            binding.tvRoomName.text = group.roomName
            binding.layoutDevicesContainer.removeAllViews()

            val inflater = LayoutInflater.from(binding.root.context)
            val context = binding.root.context

            group.items.forEach { item ->
                val deviceView = inflater.inflate(R.layout.item_device_selection, binding.layoutDevicesContainer, false)

                val tvName = deviceView.findViewById<TextView>(R.id.tv_name)
                val tvInfo = deviceView.findViewById<TextView>(R.id.tv_info)
                val ivIcon = deviceView.findViewById<ImageView>(R.id.iv_icon)
                val checkbox = deviceView.findViewById<CheckBox>(R.id.checkbox)

                val device = item.device
                tvName.text = device.name
                tvInfo.text =
                    device.type.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                val style = device.type.getUiStyle()
                ivIcon.setImageResource(style.iconRes)
                val iconColor = ContextCompat.getColor(context, style.activeColorRes)
                ivIcon.setColorFilter(iconColor)

                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = item.isSelected

                val clickListener = {
                    onItemClick(device.id)
                    checkbox.isChecked = !checkbox.isChecked
                }

                deviceView.setOnClickListener { clickListener() }
                checkbox.setOnClickListener { clickListener() }

                binding.layoutDevicesContainer.addView(deviceView)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SelectionGroup>() {
        override fun areItemsTheSame(
            oldItem: SelectionGroup,
            newItem: SelectionGroup,
        ): Boolean = oldItem.roomName == newItem.roomName

        override fun areContentsTheSame(
            oldItem: SelectionGroup,
            newItem: SelectionGroup,
        ): Boolean = oldItem == newItem
    }
}
