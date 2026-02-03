package com.ndumas.appdt.presentation.automation.create.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.ndumas.appdt.R

class ColorGridAdapter(
    private val onColorSelected: (Int) -> Unit,
    private val onCustomPickerClick: () -> Unit,
) : RecyclerView.Adapter<ColorGridAdapter.ColorViewHolder>() {
    private val colors =
        listOf(
            0xFFFF1744.toInt(),
            0xFFFF9100.toInt(),
            0xFFFFEA00.toInt(),
            0xFF00E676.toInt(),
            0xFF00B0FF.toInt(),
            0xFF2979FF.toInt(),
            0xFF3D5AFE.toInt(),
            0xFF651FFF.toInt(),
            0xFFF50057.toInt(),
            0xFFFF4081.toInt(),
            0xFF1DE9B6.toInt(),
            0xFFC6FF00.toInt(),
            0xFFF0F4FF.toInt(),
            0xFFF5F5F5.toInt(),
            0xFFFFF8E1.toInt(),
            0xFFFFF3E0.toInt(),
            0xFFFFE0B2.toInt(),
        )

    private var selectedPosition: Int = -1

    private var customColor: Int? = null

    override fun getItemCount(): Int = colors.size + 1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_color_selector, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ColorViewHolder,
        position: Int,
    ) {
        val isCustomButton = position == colors.size

        if (isCustomButton) {
            // --- MATITA ---
            // Se c'è un colore custom, usalo come sfondo; altrimenti grigio chiaro
            val bgColor = customColor ?: Color.parseColor("#F3F4F6")
            holder.card.setCardBackgroundColor(bgColor)
            holder.icon.visibility = View.VISIBLE

            // Tint dell'icona: contrasto appropriato in base al colore di sfondo
            val iconTint = if (customColor != null && isColorDark(bgColor)) Color.WHITE else Color.parseColor("#49454F")
            holder.icon.setColorFilter(iconTint)

            // Bordo di selezione se colore custom è selezionato
            if (customColor != null) {
                holder.card.strokeWidth = 8
                holder.card.strokeColor = if (isColorDark(bgColor)) Color.WHITE else Color.BLACK
                holder.card.scaleX = 1.1f
                holder.card.scaleY = 1.1f
            } else {
                holder.card.strokeWidth = 0
                holder.card.scaleX = 1.0f
                holder.card.scaleY = 1.0f
            }

            holder.itemView.setOnClickListener { onCustomPickerClick() }
        } else {
            // --- COLORE ---
            val color = colors[position]
            holder.card.setCardBackgroundColor(color)
            holder.icon.visibility = View.GONE

            if (position == selectedPosition) {
                holder.card.strokeWidth = 8

                holder.card.strokeColor = if (isColorDark(color)) Color.WHITE else Color.BLACK
                holder.card.scaleX = 1.1f
                holder.card.scaleY = 1.1f
            } else {
                holder.card.strokeWidth = 0
                holder.card.scaleX = 1.0f
                holder.card.scaleY = 1.0f
            }

            holder.itemView.setOnClickListener {
                val oldPos = selectedPosition
                val hadCustomColor = customColor != null
                selectedPosition = holder.bindingAdapterPosition
                customColor = null
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPosition)
                // Aggiorna anche il bottone matita per rimuovere il colore custom
                if (hadCustomColor) {
                    notifyItemChanged(colors.size)
                }
                onColorSelected(color)
            }
        }
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    fun setSelectedColor(color: Int) {
        val index = colors.indexOf(color)
        val customButtonPosition = colors.size

        if (index != -1) {
            val oldPos = selectedPosition
            val oldCustomColor = customColor
            selectedPosition = index
            customColor = null
            notifyItemChanged(oldPos)
            notifyItemChanged(selectedPosition)

            if (oldCustomColor != null) {
                notifyItemChanged(customButtonPosition)
            }
        } else {
            val oldPos = selectedPosition
            selectedPosition = -1
            customColor = color
            notifyItemChanged(oldPos)
            notifyItemChanged(customButtonPosition)
        }
    }

    class ColorViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.card_color)
        val icon: ImageView = view.findViewById(R.id.iv_icon)
    }
}
