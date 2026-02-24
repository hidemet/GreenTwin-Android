package com.ndumas.appdt.presentation.automation.create.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.ItemRoomGroupBinding
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.presentation.automation.create.model.RoomGroup

class ActionGroupAdapter(
    private val onDeviceClick: (Device) -> Unit,
) : ListAdapter<RoomGroup, ActionGroupAdapter.ViewHolder>(DiffCallback()) {
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
        fun bind(group: RoomGroup) {
            binding.tvRoomName.text = group.roomName

            binding.layoutDevicesContainer.removeAllViews()

            val inflater = LayoutInflater.from(binding.root.context)
            val context = binding.root.context

            group.devices.forEach { device ->

                val deviceView = inflater.inflate(R.layout.item_device_row, binding.layoutDevicesContainer, false)

                val tvName = deviceView.findViewById<TextView>(R.id.tv_name)
                val tvInfo = deviceView.findViewById<TextView>(R.id.tv_info)
                val ivIcon = deviceView.findViewById<ImageView>(R.id.iv_icon)

                tvName.text = device.name

                tvInfo.text =
                    device.type.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }

                val style = device.type.getUiStyle()
                ivIcon.setImageResource(style.iconRes)

                val iconColor = ContextCompat.getColor(context, style.activeColorRes)
                ivIcon.setColorFilter(iconColor)

                deviceView.setOnClickListener { onDeviceClick(device) }

                binding.layoutDevicesContainer.addView(deviceView)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RoomGroup>() {
        override fun areItemsTheSame(
            oldItem: RoomGroup,
            newItem: RoomGroup,
        ): Boolean = oldItem.roomName == newItem.roomName

        override fun areContentsTheSame(
            oldItem: RoomGroup,
            newItem: RoomGroup,
        ): Boolean = oldItem == newItem
    }
}
