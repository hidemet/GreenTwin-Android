package com.ndumas.appdt.presentation.automation.create.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.databinding.ItemSelectionRadioBinding

data class SunOffsetUiModel(
    val offsetMinutes: Long,
    val label: UiText,
)

class SunOffsetAdapter(
    private var selectedOffset: Long?,
) : RecyclerView.Adapter<SunOffsetAdapter.ViewHolder>() {
    private var items: List<SunOffsetUiModel> = emptyList()

    fun getSelectedOffset(): Long? = selectedOffset

    fun submitList(newItems: List<SunOffsetUiModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemSelectionRadioBinding.inflate(
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
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemSelectionRadioBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SunOffsetUiModel) {
            binding.radioButton.text = item.label.asString(binding.root.context)

            binding.radioButton.setOnCheckedChangeListener(null)
            binding.radioButton.isChecked = (item.offsetMinutes == selectedOffset)

            val clickListener = {
                val newPos = bindingAdapterPosition
                if (newPos != RecyclerView.NO_POSITION && item.offsetMinutes != selectedOffset) {
                    val oldOffset = selectedOffset
                    val oldPos = items.indexOfFirst { it.offsetMinutes == oldOffset }

                    selectedOffset = item.offsetMinutes

                    if (oldPos != -1) notifyItemChanged(oldPos)
                    notifyItemChanged(newPos)
                }
            }

            binding.root.setOnClickListener { clickListener() }
            binding.radioButton.setOnClickListener { clickListener() }
        }
    }
}
