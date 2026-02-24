package com.ndumas.appdt.presentation.device.adapter

import android.animation.ObjectAnimator
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.ItemWidgetDeviceBinding
import com.ndumas.appdt.databinding.ItemWidgetEmptyStateBinding
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.presentation.home.model.DashboardItem

/**
 * Adapter dedicato per la schermata DeviceFragment con supporto accordion.
 * Separato da DashboardAdapter per rispettare Single Responsibility Principle.
 */
class DeviceListAdapter(
    private val onDeviceToggle: (DashboardItem.DeviceWidget) -> Unit,
    private val onDeviceDetails: (DashboardItem.DeviceWidget) -> Unit,
    private val onSectionToggle: (String) -> Unit,
) : ListAdapter<DashboardItem, RecyclerView.ViewHolder>(DeviceListDiffCallback()) {
    companion object {
        private const val ANIMATION_DURATION = 200L
        const val TYPE_DEVICE = 1
        const val TYPE_HEADER = 3
        const val TYPE_EMPTY_STATE = 4
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is DashboardItem.DeviceWidget -> TYPE_DEVICE
            is DashboardItem.SectionHeader -> TYPE_HEADER
            is DashboardItem.EmptyState -> TYPE_EMPTY_STATE
            else -> throw IllegalArgumentException("Invalid item type: ${getItem(position)}")
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DEVICE -> {
                val binding = ItemWidgetDeviceBinding.inflate(inflater, parent, false)
                DeviceViewHolder(binding)
            }

            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_section_header_expandable, parent, false)
                ExpandableHeaderViewHolder(view)
            }

            TYPE_EMPTY_STATE -> {
                val binding = ItemWidgetEmptyStateBinding.inflate(inflater, parent, false)
                EmptyStateViewHolder(binding)
            }

            else -> {
                throw IllegalArgumentException("Invalid view type: $viewType")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (holder) {
            is DeviceViewHolder -> {
                holder.bind(getItem(position) as DashboardItem.DeviceWidget)
            }

            is ExpandableHeaderViewHolder -> {
                holder.bind(getItem(position) as DashboardItem.SectionHeader)
            }

            is EmptyStateViewHolder -> {
                holder.bind(getItem(position) as DashboardItem.EmptyState)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNotEmpty() && payloads[0] == "EXPAND_STATE_CHANGED") {
            // Solo aggiorna l'icona chevron senza rebind completo
            if (holder is ExpandableHeaderViewHolder) {
                val item = getItem(position) as DashboardItem.SectionHeader
                holder.updateExpandIcon(item.isExpanded, animate = true)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class DeviceViewHolder(
        private val binding: ItemWidgetDeviceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardItem.DeviceWidget) {
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

            // Nascondi elementi edit mode (non usati in DeviceFragment)
            binding.ivRemoveAction.visibility = View.GONE
            binding.ivDragHandle.visibility = View.GONE
            binding.ivIcon.setOnClickListener(null)
            binding.ivIcon.isClickable = false

            // Ripristina foreground per ripple effect
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

    inner class ExpandableHeaderViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val container: View = itemView.findViewById(R.id.section_header_container)
        private val textView: TextView = itemView.findViewById(R.id.tv_header_title)
        private val expandIcon: ImageView = itemView.findViewById(R.id.iv_expand_icon)

        fun bind(item: DashboardItem.SectionHeader) {
            textView.text = item.title

            // Imposta rotazione iniziale senza animazione
            updateExpandIcon(item.isExpanded, animate = false)

            container.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onSectionToggle(item.id)
            }
        }

        fun updateExpandIcon(
            isExpanded: Boolean,
            animate: Boolean,
        ) {
            val targetRotation = if (isExpanded) 0f else -180f

            if (animate) {
                ObjectAnimator.ofFloat(expandIcon, View.ROTATION, expandIcon.rotation, targetRotation).apply {
                    duration = ANIMATION_DURATION
                    start()
                }
            } else {
                expandIcon.rotation = targetRotation
            }
        }
    }

    class EmptyStateViewHolder(
        private val binding: ItemWidgetEmptyStateBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardItem.EmptyState) {
            val context = binding.root.context
            binding.tvTitle.text = item.title.asString(context)
            binding.tvDescription.text = item.description.asString(context)
            binding.ivIcon.setImageResource(item.iconRes)

            // Imposta l'altezza per occupare lo spazio disponibile
            binding.root.post {
                val parent = binding.root.parent as? RecyclerView
                parent?.let {
                    val params = binding.root.layoutParams
                    params.height = it.height
                    binding.root.layoutParams = params
                }
            }
        }
    }

    class DeviceListDiffCallback : DiffUtil.ItemCallback<DashboardItem>() {
        override fun areItemsTheSame(
            oldItem: DashboardItem,
            newItem: DashboardItem,
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: DashboardItem,
            newItem: DashboardItem,
        ): Boolean = oldItem == newItem

        override fun getChangePayload(
            oldItem: DashboardItem,
            newItem: DashboardItem,
        ): Any? {
            // Ottimizzazione: se cambia solo isExpanded, usa payload per animazione
            if (oldItem is DashboardItem.SectionHeader && newItem is DashboardItem.SectionHeader) {
                if (oldItem.copy(isExpanded = newItem.isExpanded) == newItem) {
                    return "EXPAND_STATE_CHANGED"
                }
            }
            return null
        }
    }
}
