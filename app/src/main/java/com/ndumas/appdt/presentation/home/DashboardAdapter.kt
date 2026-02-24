package com.ndumas.appdt.presentation.home

import android.content.Context
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.device.getUiStyle
import com.ndumas.appdt.databinding.ItemWidgetAutomationDashboardBinding
import com.ndumas.appdt.databinding.ItemWidgetDeviceBinding
import com.ndumas.appdt.databinding.ItemWidgetEmptyStateBinding
import com.ndumas.appdt.databinding.ItemWidgetEnergyInfoBinding
import com.ndumas.appdt.databinding.ItemWidgetRoomGroupBinding
import com.ndumas.appdt.domain.device.model.DeviceType
import com.ndumas.appdt.presentation.home.model.DashboardItem
import com.ndumas.appdt.presentation.home.model.DashboardSectionType
import java.util.Locale

class DashboardAdapter(
    private val onDeviceToggle: (DashboardItem.DeviceWidget) -> Unit,
    private val onDeviceDetails: (DashboardItem.DeviceWidget) -> Unit,
    private val onAutomationDetails: (DashboardItem.AutomationWidget) -> Unit,
    private val onRoomGroupClick: (DashboardItem.RoomGroupWidget) -> Unit,
    private val onDeleteClick: (DashboardItem) -> Unit,
    private val dragStartListener: (RecyclerView.ViewHolder) -> Unit,
    private val onEnergyWidgetClick: () -> Unit = {},
    private val onAddClick: (DashboardSectionType) -> Unit,
    private val onSectionVisibilityToggle: (DashboardSectionType) -> Unit = {},
) : ListAdapter<DashboardItem, RecyclerView.ViewHolder>(DashboardDiffCallback()) {
    private var isEditMode: Boolean = false
    private var hiddenSections: Set<DashboardSectionType> = emptySet()
    private var needsRebind: Boolean = false

    companion object {
        private const val GHOST_ALPHA = 0.38f
        private const val NORMAL_ALPHA = 1.0f
        private const val ANIMATION_DURATION = 200L
        const val TYPE_ENERGY = 0
        const val TYPE_DEVICE = 1
        const val TYPE_HEADER = 3
        const val TYPE_EMPTY_STATE = 4
        const val TYPE_AUTOMATION_DASHBOARD = 5
        const val TYPE_ROOM_GROUP = 6
        const val TYPE_ADD_PLACEHOLDER = 7
    }

    fun setEditMode(enabled: Boolean) {
        if (this.isEditMode != enabled) {
            this.isEditMode = enabled
            this.needsRebind = true
        }
    }

    fun setHiddenSections(sections: Set<DashboardSectionType>) {
        if (this.hiddenSections != sections) {
            this.hiddenSections = sections
            this.needsRebind = true
        }
    }

    /**
     * Forza il rebind di tutti gli elementi se lo stato è cambiato.
     * Chiamare dopo submitList per assicurare che l'alpha sia corretta.
     */
    fun applyPendingStateChanges() {
        if (needsRebind) {
            needsRebind = false
            notifyDataSetChanged()
        }
    }

    private fun isSectionVisible(sectionType: DashboardSectionType): Boolean = !hiddenSections.contains(sectionType)

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is DashboardItem.EnergyWidget -> TYPE_ENERGY
            is DashboardItem.DeviceWidget -> TYPE_DEVICE
            is DashboardItem.AutomationWidget -> TYPE_AUTOMATION_DASHBOARD
            is DashboardItem.SectionHeader -> TYPE_HEADER
            is DashboardItem.EmptyState -> TYPE_EMPTY_STATE
            is DashboardItem.RoomGroupWidget -> TYPE_ROOM_GROUP
            is DashboardItem.AddPlaceholder -> TYPE_ADD_PLACEHOLDER
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

            TYPE_AUTOMATION_DASHBOARD -> {
                val binding = ItemWidgetAutomationDashboardBinding.inflate(inflater, parent, false)
                AutomationDashboardViewHolder(binding)
            }

            TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }

            TYPE_ADD_PLACEHOLDER -> {
                val view = inflater.inflate(R.layout.item_widget_add_placeholder, parent, false)
                AddPlaceholderViewHolder(view)
            }

            TYPE_EMPTY_STATE -> {
                val binding = ItemWidgetEmptyStateBinding.inflate(inflater, parent, false)
                EmptyStateViewHolder(binding)
            }

            TYPE_ROOM_GROUP -> {
                val binding = ItemWidgetRoomGroupBinding.inflate(inflater, parent, false)
                RoomGroupViewHolder(binding)
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
            is EnergyViewHolder -> {
                val item = getItem(position) as DashboardItem.EnergyWidget
                val sectionVisible = isSectionVisible(item.sectionType)
                holder.bind(item, onEnergyWidgetClick, isEditMode, sectionVisible)
            }

            is DeviceViewHolder -> {
                val item = getItem(position) as DashboardItem.DeviceWidget
                val sectionVisible = isSectionVisible(item.sectionType)
                holder.bind(item, isEditMode, sectionVisible)
            }

            is AutomationDashboardViewHolder -> {
                val item = getItem(position) as DashboardItem.AutomationWidget
                val sectionVisible = isSectionVisible(item.sectionType)
                holder.bind(item, isEditMode, sectionVisible)
            }

            is HeaderViewHolder -> {
                holder.bind(
                    getItem(position) as DashboardItem.SectionHeader,
                    onAddClick,
                    onSectionVisibilityToggle,
                    isEditMode,
                )
            }

            is EmptyStateViewHolder -> {
                holder.bind(getItem(position) as DashboardItem.EmptyState)
            }

            is AddPlaceholderViewHolder -> {
                val item = getItem(position) as DashboardItem.AddPlaceholder
                val sectionVisible = isSectionVisible(item.sectionType)
                holder.bind(item, onAddClick, isEditMode, sectionVisible)
            }

            is RoomGroupViewHolder -> {
                val item = getItem(position) as DashboardItem.RoomGroupWidget
                val sectionVisible = isSectionVisible(item.sectionType)
                holder.bind(item, isEditMode, sectionVisible)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNotEmpty() && (payloads[0] == "EDIT_MODE_CHANGED" || payloads[0] == "VISIBILITY_CHANGED")) {
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

            // Imposta l'altezza per occupare lo spazio disponibile (escluse toolbar e bottom nav)
            binding.root.post {
                val parent = binding.root.parent as? androidx.recyclerview.widget.RecyclerView
                parent?.let {
                    val params = binding.root.layoutParams
                    params.height = it.height
                    binding.root.layoutParams = params
                }
            }
        }
    }

    inner class EnergyViewHolder(
        private val binding: ItemWidgetEnergyInfoBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: DashboardItem.EnergyWidget,
            onEnergyWidgetClick: () -> Unit,
            isEditMode: Boolean,
            isSectionVisible: Boolean,
        ) {
            val context = binding.root.context
            val isGhostMode = isEditMode && !isSectionVisible
            val targetAlpha = if (isGhostMode) GHOST_ALPHA else NORMAL_ALPHA

            // Cancella animazioni precedenti e applica alpha immediatamente
            binding.root.animate().cancel()
            binding.root.alpha = targetAlpha

            // Valore consumo formattato
            binding.tvPowerValue.text = String.format(Locale.getDefault(), "%.1f", item.currentConsumptionKwh)

            // Badge percentuale con colore dinamico
            val trendText = String.format(Locale.getDefault(), "%.0f%%", item.trendPercentage)
            binding.tvPercentageBadge.text = trendText

            when (item.trendState) {
                com.ndumas.appdt.presentation.consumption.model.PredictionState.POSITIVE -> {
                    // Verde - consumo inferiore alla stima (buono)
                    binding.tvPercentageBadge.setBackgroundResource(R.drawable.bg_badge_green)
                    binding.tvPercentageBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_success_text))
                    binding.tvPercentageBadge.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_trending_down,
                        0,
                        0,
                        0,
                    )
                    binding.tvPercentageBadge.compoundDrawableTintList =
                        ContextCompat.getColorStateList(context, R.color.badge_success_text)
                }

                com.ndumas.appdt.presentation.consumption.model.PredictionState.NEGATIVE -> {
                    // Rosso - consumo superiore alla stima (male)
                    binding.tvPercentageBadge.setBackgroundResource(R.drawable.bg_badge_error)
                    binding.tvPercentageBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_error_text))
                    binding.tvPercentageBadge.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_trending_up,
                        0,
                        0,
                        0,
                    )
                    binding.tvPercentageBadge.compoundDrawableTintList =
                        ContextCompat.getColorStateList(context, R.color.badge_error_text)
                }

                com.ndumas.appdt.presentation.consumption.model.PredictionState.NEUTRAL -> {
                    // Grigio - neutrale
                    binding.tvPercentageBadge.setBackgroundResource(R.drawable.bg_badge_neutral)
                    binding.tvPercentageBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_neutral_text))
                    binding.tvPercentageBadge.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }
            }

            // Testo di confronto
            binding.tvComparisonText.text = context.getString(R.string.energy_comparison_text)

            // Click listener per navigare ai consumi (disabilitato in ghost mode)
            if (isGhostMode) {
                binding.cardEnergyContainer.setOnClickListener(null)
                binding.cardEnergyContainer.isClickable = false
            } else {
                binding.cardEnergyContainer.isClickable = true
                binding.cardEnergyContainer.setOnClickListener {
                    onEnergyWidgetClick()
                }
            }
        }
    }

    inner class DeviceViewHolder(
        private val binding: ItemWidgetDeviceBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: DashboardItem.DeviceWidget,
            isEditMode: Boolean,
            isSectionVisible: Boolean,
        ) {
            val device = item.device
            val context = binding.root.context
            val isGhostMode = isEditMode && !isSectionVisible
            val targetAlpha = if (isGhostMode) GHOST_ALPHA else NORMAL_ALPHA

            // Cancella animazioni precedenti e applica alpha immediatamente
            binding.root.animate().cancel()
            binding.root.alpha = targetAlpha

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

                // In ghost mode, disabilita le interazioni di drag
                if (isGhostMode) {
                    binding.root.isClickable = false
                    binding.root.isFocusable = false
                    binding.root.isLongClickable = false
                    binding.root.setOnClickListener(null)
                    binding.root.setOnLongClickListener(null)
                    binding.ivRemoveAction.visibility = View.GONE
                } else {
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
                    binding.ivRemoveAction.setOnClickListener { onDeleteClick(item) }
                }
                binding.ivDragHandle.visibility = View.GONE

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

    inner class AutomationDashboardViewHolder(
        private val binding: ItemWidgetAutomationDashboardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: DashboardItem.AutomationWidget,
            isEditMode: Boolean,
            isSectionVisible: Boolean,
        ) {
            val context = binding.root.context
            val isGhostMode = isEditMode && !isSectionVisible
            val targetAlpha = if (isGhostMode) GHOST_ALPHA else NORMAL_ALPHA

            // Cancella animazioni precedenti e applica alpha immediatamente
            binding.root.animate().cancel()
            binding.root.alpha = targetAlpha

            binding.tvAutomationName.text = item.name
            binding.tvAutomationDesc.text = item.description

            val isActiveState = item.isActive
            binding.cardContainer.isActivated = isActiveState

            val iconColor =
                if (isActiveState) {
                    ContextCompat.getColor(context, R.color.automation_icon_active)
                } else {
                    ContextCompat.getColor(context, R.color.device_icon_off)
                }
            binding.ivIcon.setColorFilter(iconColor)

            if (isEditMode) {
                binding.cardContainer.strokeWidth = 4
                binding.cardContainer.strokeColor = resolveColorAttribute(context, R.attr.colorPrimary)
                binding.cardContainer.foreground = null

                // In ghost mode, disabilita le interazioni di drag
                if (isGhostMode) {
                    binding.root.isClickable = false
                    binding.root.setOnLongClickListener(null)
                    binding.ivRemoveAction.visibility = View.GONE
                } else {
                    binding.root.isClickable = true
                    binding.root.setOnLongClickListener { view ->
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        dragStartListener(this@AutomationDashboardViewHolder)
                        true
                    }
                    binding.ivRemoveAction.visibility = View.VISIBLE
                    binding.ivRemoveAction.setOnClickListener { onDeleteClick(item) }
                }
                binding.ivDragHandle.visibility = View.GONE
                binding.root.scaleX = 0.95f
                binding.root.scaleY = 0.95f
            } else {
                binding.cardContainer.strokeWidth = 0
                binding.ivRemoveAction.visibility = View.GONE
                binding.ivDragHandle.visibility = View.GONE
                val outValue = android.util.TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                binding.cardContainer.foreground = ContextCompat.getDrawable(context, outValue.resourceId)
                binding.root.scaleX = 1.0f
                binding.root.scaleY = 1.0f
                binding.root.setOnClickListener { onAutomationDetails(item) }
                binding.root.setOnLongClickListener(null)
            }
        }
    }

    inner class RoomGroupViewHolder(
        private val binding: ItemWidgetRoomGroupBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: DashboardItem.RoomGroupWidget,
            isEditMode: Boolean,
            isSectionVisible: Boolean,
        ) {
            val context = binding.root.context
            val isGhostMode = isEditMode && !isSectionVisible
            val targetAlpha = if (isGhostMode) GHOST_ALPHA else NORMAL_ALPHA

            // Cancella animazioni precedenti e applica alpha immediatamente
            binding.root.animate().cancel()
            binding.root.alpha = targetAlpha

            binding.tvName.text = item.name

            // Imposta il conteggio dei dispositivi con pluralizzazione
            binding.tvDeviceCount.text =
                when (item.deviceCount) {
                    0 -> "Nessun dispositivo"
                    1 -> "1 dispositivo"
                    else -> "${item.deviceCount} dispositivi"
                }

            // Icona diversa per stanza vs gruppo
            val iconRes = if (item.isRoom) R.drawable.ic_door_open else R.drawable.ic_group_work
            binding.ivIcon.setImageResource(iconRes)
            binding.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.device_icon_off))

            if (isEditMode) {
                binding.cardContainer.strokeWidth = 4

                binding.cardContainer.foreground = null

                // In ghost mode, disabilita le interazioni di drag
                if (isGhostMode) {
                    binding.root.isClickable = false
                    binding.root.isFocusable = false
                    binding.root.isLongClickable = false
                    binding.root.setOnClickListener(null)
                    binding.root.setOnLongClickListener(null)
                    binding.ivRemoveAction.visibility = View.GONE
                } else {
                    binding.root.isClickable = true
                    binding.root.isFocusable = true
                    binding.root.isLongClickable = true

                    binding.root.setOnClickListener { /* No-op in edit mode */ }

                    binding.root.setOnLongClickListener { view ->
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        dragStartListener(this@RoomGroupViewHolder)
                        true
                    }

                    binding.ivRemoveAction.visibility = View.VISIBLE
                    binding.ivRemoveAction.setOnClickListener { onDeleteClick(item) }
                }
                binding.ivDragHandle.visibility = View.GONE

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
                binding.root.isLongClickable = false

                binding.root.setOnClickListener {
                    onRoomGroupClick(item)
                }

                binding.root.setOnLongClickListener(null)
            }
        }
    }

    class HeaderViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val container: View = itemView.findViewById(R.id.section_header_container)
        private val textView: TextView = itemView.findViewById(R.id.tv_header_title)
        private val visibilityButton: com.google.android.material.button.MaterialButton =
            itemView.findViewById(R.id.btn_visibility_toggle)
        private val addButton: View = itemView.findViewById(R.id.btn_add_section_item)

        fun bind(
            item: DashboardItem.SectionHeader,
            onAddClick: (DashboardSectionType) -> Unit,
            onVisibilityToggle: (DashboardSectionType) -> Unit,
            isEditMode: Boolean,
        ) {
            textView.text = item.title

            // Nascondi pulsante + per la sezione info energetiche (non ha widget aggiuntivi)
            if (item.id == "header_info") {
                addButton.visibility = View.GONE
            } else {
                // Pulsante + sempre visibile (per aggiungere widget)
                addButton.visibility = View.VISIBLE
                addButton.setOnClickListener { onAddClick(item.sectionType) }
            }

            // Pulsante occhio visibile solo in edit mode (per tutte le sezioni)
            if (isEditMode) {
                visibilityButton.visibility = View.VISIBLE
                // Cambia icona in base a isVisible
                val iconRes = if (item.isVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                visibilityButton.setIconResource(iconRes)
                visibilityButton.setOnClickListener { onVisibilityToggle(item.sectionType) }

                // Cancella animazioni precedenti e applica alpha per sezioni nascoste (ghost effect)
                container.animate().cancel()
                container.alpha = if (item.isVisible) NORMAL_ALPHA else 0.5f
            } else {
                visibilityButton.visibility = View.GONE
                // Resetta alpha quando non in edit mode
                container.animate().cancel()
                container.alpha = NORMAL_ALPHA
                container.alpha = 1.0f
            }
        }
    }

    inner class AddPlaceholderViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        private val cardContainer: View = itemView.findViewById(R.id.card_container)
        private val textView: TextView = itemView.findViewById(R.id.tv_placeholder_label)

        fun bind(
            item: DashboardItem.AddPlaceholder,
            onAddClick: (DashboardSectionType) -> Unit,
            isEditMode: Boolean,
            isSectionVisible: Boolean,
        ) {
            val isGhostMode = isEditMode && !isSectionVisible
            val targetAlpha = if (isGhostMode) GHOST_ALPHA else NORMAL_ALPHA

            // Cancella animazioni precedenti e applica alpha immediatamente
            itemView.animate().cancel()
            itemView.alpha = targetAlpha

            textView.text = item.text.asString(itemView.context)

            // In ghost mode, disabilita le interazioni
            if (isGhostMode) {
                cardContainer.setOnClickListener(null)
                cardContainer.isClickable = false
            } else {
                cardContainer.isClickable = true
                cardContainer.setOnClickListener { onAddClick(item.sectionType) }
            }
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
