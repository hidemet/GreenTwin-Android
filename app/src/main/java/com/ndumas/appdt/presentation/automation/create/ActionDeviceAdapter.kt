package com.ndumas.appdt.presentation.automation.create.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.ItemDeviceSelectionBinding // Riutilizziamo il layout esistente
import com.ndumas.appdt.domain.device.model.Device

class ActionDeviceAdapter(
    private val onClick: (Device) -> Unit,
) : ListAdapter<Device, ActionDeviceAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemDeviceSelectionBinding.inflate(
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
        private val binding: ItemDeviceSelectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device) {
            binding.tvName.text = device.name
            binding.tvInfo.text = device.room ?: "Non assegnato"

            val style = device.type.getUiStyle()
            binding.ivIcon.setImageResource(style.iconRes)

            binding.checkbox.visibility = android.view.View.GONE

            binding.root.setOnClickListener { onClick(device) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(
            oldItem: Device,
            newItem: Device,
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Device,
            newItem: Device,
        ) = oldItem == newItem
    }
}
