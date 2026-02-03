package com.ndumas.appdt.presentation.consumption.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.ItemConsumptionDeviceBinding
import com.ndumas.appdt.presentation.consumption.model.ConsumptionBreakdownUiModel

class ConsumptionBreakdownAdapter : ListAdapter<ConsumptionBreakdownUiModel, ConsumptionBreakdownAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemConsumptionDeviceBinding.inflate(
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
        private val binding: ItemConsumptionDeviceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConsumptionBreakdownUiModel) {
            val context = binding.root.context

            binding.tvName.text = item.name
            binding.tvValue.text = item.valueText.asString(context)
            binding.progressBar.progress = item.progress

            val uiStyle = item.type.getUiStyle()

            val resolvedColor = ContextCompat.getColor(context, uiStyle.activeColorRes)

            binding.ivIcon.setImageResource(uiStyle.iconRes)
            binding.ivIcon.setColorFilter(resolvedColor)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ConsumptionBreakdownUiModel>() {
        override fun areItemsTheSame(
            oldItem: ConsumptionBreakdownUiModel,
            newItem: ConsumptionBreakdownUiModel,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ConsumptionBreakdownUiModel,
            newItem: ConsumptionBreakdownUiModel,
        ): Boolean = oldItem == newItem
    }
}
