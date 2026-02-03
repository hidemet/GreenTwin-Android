package com.ndumas.appdt.presentation.control.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.core.ui.device.getIconRes
import com.ndumas.appdt.databinding.ItemSensorCardBinding
import com.ndumas.appdt.domain.device.model.SensorAttribute

class SensorGridAdapter : ListAdapter<SensorAttribute, SensorGridAdapter.ViewHolder>(SensorDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemSensorCardBinding.inflate(
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

    class ViewHolder(
        private val binding: ItemSensorCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SensorAttribute) {
            binding.tvSensorLabel.text = item.label
            binding.tvSensorValue.text = item.value
            binding.tvSensorUnit.text = item.unit ?: ""
            binding.ivSensorIcon.setImageResource(item.icon.getIconRes())
        }
    }

    class SensorDiffCallback : DiffUtil.ItemCallback<SensorAttribute>() {
        override fun areItemsTheSame(
            oldItem: SensorAttribute,
            newItem: SensorAttribute,
        ): Boolean = oldItem.label == newItem.label

        override fun areContentsTheSame(
            oldItem: SensorAttribute,
            newItem: SensorAttribute,
        ): Boolean = oldItem == newItem
    }
}
