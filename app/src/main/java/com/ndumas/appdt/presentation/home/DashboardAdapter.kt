package com.ndumas.appdt.presentation.home

import android.content.Context
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.ItemWidgetAutomationBinding
import com.ndumas.appdt.databinding.ItemWidgetDeviceBinding
import com.ndumas.appdt.databinding.ItemWidgetEmptyStateBinding
import com.ndumas.appdt.databinding.ItemWidgetEnergyInfoBinding
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.presentation.home.model.DashboardItem
import java.util.Locale

class DashboardAdapter(
    private val onDeviceToggle: (DashboardItem.DeviceWidget) -> Unit,
    private val onDeviceDetails: (DashboardItem.DeviceWidget) -> Unit,
    private val onAutomationToggle: (DashboardItem.AutomationWidget) -> Unit,
    private val onAutomationDetails: (DashboardItem.AutomationWidget) -> Unit,
    private val onDeleteClick: (DashboardItem) -> Unit,
    private val dragStartListener: (RecyclerView.ViewHolder) -> Unit,
) : ListAdapter<DashboardItem, RecyclerView.ViewHolder>(DashboardDiffCallback()) {
    private var isEditMode: Boolean = false

    fun setEditMode(enabled: Boolean) {
        this.isEditMode = enabled

        notifyItemRangeChanged(0, itemCount, "EDIT_MODE_CHANGED")
    }

    companion object {
        const val TYPE_ENERGY = 0
        const val TYPE_DEVICE = 1
        const val TYPE_AUTOMATION = 2
        const val TYPE_HEADER = 3

        const val TYPE_EMPTY_STATE = 4
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is DashboardItem.EnergyWidget -> TYPE_ENERGY
            is DashboardItem.DeviceWidget -> TYPE_DEVICE
            is DashboardItem.AutomationWidget -> TYPE_AUTOMATION
            is DashboardItem.SectionHeader -> TYPE_HEADER
            is DashboardItem.EmptyState -> TYPE_EMPTY_STATE
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ENERGY -> {
                val binding = ItemWidgetEnergyInfoBinding.inflate(inflater, parent, false)
                EnergyViewHolder(binding)
            }

            TYPE_DEVICE -> {
                val binding = ItemWidgetDeviceBinding.inflate(inflater, parent, false)
                DeviceViewHolder(binding)
            }

            TYPE_AUTOMATION -> {
                val binding = ItemWidgetAutomationBinding.inflate(inflater, parent, false)
                AutomationViewHolder(binding)
            }

            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }

            TYPE_EMPTY_STATE -> {
                val binding = ItemWidgetEmptyStateBinding.inflate(inflater, parent, false)
                EmptyStateViewHolder(binding)
            }

            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (holder) {
            is EnergyViewHolder -> holder.bind(getItem(position) as DashboardItem.EnergyWidget)
            is DeviceViewHolder -> holder.bind(getItem(position) as DashboardItem.DeviceWidget, isEditMode)
            is AutomationViewHolder -> holder.bind(getItem(position) as DashboardItem.AutomationWidget, isEditMode)
            is HeaderViewHolder -> holder.bind(getItem(position) as DashboardItem.SectionHeader)
            is EmptyStateViewHolder -> holder.bind(getItem(position) as DashboardItem.EmptyState)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNotEmpty() && payloads[0] == "EDIT_MODE_CHANGED") {
            onBindViewHolder(holder, position)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class EmptyStateViewHolder(
        private val binding: ItemWidgetEmptyStateBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardItem.EmptyState) {
            val context = binding.root.context
            binding.tvTitle.text = item.title.asString(context)
            binding.tvDescription.text = item.description.asString(context)
            binding.ivIcon.setImageResource(item.iconRes)
        }
    }

    inner class EnergyViewHolder(
        private val binding: ItemWidgetEnergyInfoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardItem.EnergyWidget) {
            binding.tvPowerValue.text = String.format(Locale.getDefault(), "%.1f kW", item.currentPowerKw)

            val sign = if (item.trend > 0) "+" else ""
            binding.tvComparison.text =
                "$sign${item.trend}% rispetto a ieri (${String.format(Locale.getDefault(), "%.1f", item.yesterdayPowerKw)} kW)"

            val trendColorRes =
                if (item.trend <= 0) {
                    R.color.badge_success_text
                } else {
                    R.color.badge_error_text
                }
            binding.tvComparison.setTextColor(ContextCompat.getColor(binding.root.context, trendColorRes))

            val timeAgo =
                DateUtils.getRelativeTimeSpanString(
                    item.lastUpdate,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                )
            binding.tvUpdated.text = "Aggiornato: $timeAgo"
            binding.root.isClickable = false
        }
    }

    inner class DeviceViewHolder(
        private val binding: ItemWidgetDeviceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: DashboardItem.DeviceWidget,
            isEditMode: Boolean,
        ) {
            val device = item.device
            val context = binding.root.context

            binding.tvDeviceName.text = device.name
            binding.tvDeviceInfo.text =
                when {
                    !device.isOnline -> "Offline"
                    device.type == DeviceType.SENSOR && device.isOn -> "Rilevato"
                    device.type == DeviceType.SENSOR && !device.isOn -> "Nessuno"
                    device.isOn && device.currentPower > 0 -> "${device.currentPower} W"
                    device.isOn -> "On"
                    else -> "Off"
                }

            val uiStyle = device.type.getUiStyle()
            binding.ivIcon.setImageResource(uiStyle.iconRes)

            val isActiveState = device.isOn && device.isOnline
            binding.cardContainer.isActivated = isActiveState

            val iconColor =
                if (isActiveState) {
                    ContextCompat.getColor(context, uiStyle.activeColorRes)
                } else {
                    ContextCompat.getColor(context, R.color.device_icon_off)
                }
            binding.ivIcon.setColorFilter(iconColor)

            binding.ivIcon.setOnClickListener(null)
            binding.ivIcon.isClickable = false

            if (isEditMode) {
                binding.cardContainer.strokeWidth = 4

                binding.cardContainer.foreground = null

                binding.root.isClickable = true
                binding.root.isFocusable = true
                binding.root.isLongClickable = true

                binding.root.setOnClickListener { /* No-op in edit mode */ }

                binding.root.setOnLongClickListener { view ->
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    dragStartListener(this@DeviceViewHolder)
                    true
                }

                binding.ivRemoveAction.visibility = View.VISIBLE
                binding.ivDragHandle.visibility = View.GONE

                binding.ivRemoveAction.setOnClickListener { onDeleteClick(item) }

                binding.root.scaleX = 0.95f
                binding.root.scaleY = 0.95f
            } else {
                binding.ivRemoveAction.visibility = View.GONE
                binding.ivDragHandle.visibility = View.GONE

                val outValue = android.util.TypedValue()
                context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    outValue,
                    true,
                )
                binding.cardContainer.foreground = ContextCompat.getDrawable(context, outValue.resourceId)

                binding.root.scaleX = 1.0f
                binding.root.scaleY = 1.0f
                binding.root.isHapticFeedbackEnabled = true
                binding.root.isClickable = true
                binding.root.isFocusable = true
                binding.root.isLongClickable = true

                binding.root.setOnClickListener {
                    onDeviceToggle(item)
                }

                binding.root.setOnLongClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onDeviceDetails(item)
                    true
                }
            }
        }
    }

    inner class AutomationViewHolder(
        private val binding: ItemWidgetAutomationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: DashboardItem.AutomationWidget,
            isEditMode: Boolean,
        ) {
            val context = binding.root.context
            binding.tvAutomationName.text = item.name

            binding.tvAutomationDesc.text = item.description

            binding.switchActive.isClickable = false
            binding.switchActive.isChecked = item.isActive

            if (isEditMode) {
                binding.cardContainer.strokeWidth = 4
                binding.cardContainer.strokeColor = resolveColorAttribute(context, R.attr.colorPrimary)

                binding.cardContainer.foreground = null

                binding.ivRemoveAction.visibility = View.VISIBLE
                binding.ivDragHandle.visibility = View.GONE
                binding.switchActive.visibility = View.GONE

                binding.ivRemoveAction.setOnClickListener { onDeleteClick(item) }

                binding.root.isClickable = true
                binding.root.isFocusable = true
                binding.root.isLongClickable = true

                binding.root.setOnClickListener { /* No-op in edit mode */ }

                binding.root.setOnLongClickListener { view ->
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    dragStartListener(this@AutomationViewHolder)
                    true
                }

                binding.root.scaleX = 0.95f
                binding.root.scaleY = 0.95f
            } else {
                binding.ivRemoveAction.visibility = View.GONE
                binding.ivDragHandle.visibility = View.GONE
                binding.switchActive.visibility = View.VISIBLE

                val outValue = android.util.TypedValue()
                context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    outValue,
                    true,
                )
                binding.cardContainer.foreground = ContextCompat.getDrawable(context, outValue.resourceId)

                binding.root.isClickable = true
                binding.root.isFocusable = true
                binding.root.isLongClickable = true
                binding.root.scaleX = 1.0f
                binding.root.scaleY = 1.0f

                binding.root.setOnClickListener {
                    onAutomationToggle(item)
                }

                binding.root.setOnLongClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onAutomationDetails(item)
                    true
                }

                binding.switchActive.setOnCheckedChangeListener(null)
                binding.switchActive.isChecked = item.isActive
                binding.switchActive.setOnClickListener { onAutomationToggle(item) }
            }
        }
    }

    class HeaderViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val textView: android.widget.TextView = itemView.findViewById(R.id.tv_header_title)

        fun bind(item: DashboardItem.SectionHeader) {
            textView.text = item.title
        }
    }

    private fun resolveColorAttribute(
        context: Context,
        attr: Int,
    ): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    class DashboardDiffCallback : DiffUtil.ItemCallback<DashboardItem>() {
        override fun areItemsTheSame(
            oldItem: DashboardItem,
            newItem: DashboardItem,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DashboardItem,
            newItem: DashboardItem,
        ): Boolean = oldItem == newItem
    }
}
