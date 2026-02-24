package com.ndumas.appdt.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ndumas.appdt.R
import com.ndumas.appdt.databinding.ItemRoomGroupSelectionBinding
import com.ndumas.appdt.presentation.home.model.SelectableRoomGroupItem

class RoomGroupSelectionAdapter(
    private val onItemClick: (String) -> Unit,
) : ListAdapter<SelectableRoomGroupItem, RoomGroupSelectionAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding =
            ItemRoomGroupSelectionBinding.inflate(
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
        private val binding: ItemRoomGroupSelectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectableRoomGroupItem) {
            val info = item.roomGroupInfo

            binding.tvName.text = info.name

            // Conteggio dispositivi con pluralizzazione
            binding.tvDeviceCount.text =
                when (info.deviceCount) {
                    0 -> "Nessun dispositivo"
                    1 -> "1 dispositivo"
                    else -> "${info.deviceCount} dispositivi"
                }

            // Icona diversa per stanza vs gruppo
            val iconRes = if (info.isRoom) R.drawable.ic_door_open else R.drawable.ic_group_work
            binding.ivIcon.setImageResource(iconRes)

            // Stato checkbox
            binding.cbSelected.isChecked = item.isSelected

            // Click listener
            binding.root.setOnClickListener {
                onItemClick(info.id)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SelectableRoomGroupItem>() {
        override fun areItemsTheSame(
            oldItem: SelectableRoomGroupItem,
            newItem: SelectableRoomGroupItem,
        ): Boolean = oldItem.roomGroupInfo.id == newItem.roomGroupInfo.id

        override fun areContentsTheSame(
            oldItem: SelectableRoomGroupItem,
            newItem: SelectableRoomGroupItem,
        ): Boolean = oldItem == newItem
    }
}
