package com.ndumas.appdt.presentation.consumption.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.ndumas.appdt.R
import com.ndumas.appdt.domain.consumption.model.Consumption
import com.ndumas.appdt.presentation.consumption.ConsumptionTimeFilter
import com.ndumas.appdt.presentation.consumption.formatter.ConsumptionUiFormatter

@SuppressLint("ViewConstructor")
class ConsumptionMarkerView(
    context: Context,
    private val formatter: ConsumptionUiFormatter,
) : MarkerView(context, R.layout.view_chart_marker) {
    private val tvValue: TextView = findViewById(R.id.tv_marker_value)
    private val tvDate: TextView = findViewById(R.id.tv_marker_date)
    private val arrowView: View = findViewById(R.id.cv_arrow)

    private var dataList: List<Consumption> = emptyList()
    private var currentFilter: ConsumptionTimeFilter = ConsumptionTimeFilter.WEEK

    fun updateData(
        newData: List<Consumption>,
        filter: ConsumptionTimeFilter,
    ) {
        this.dataList = newData
        this.currentFilter = filter
    }

    override fun refreshContent(
        e: Entry?,
        highlight: Highlight?,
    ) {
        if (e == null) return
        val index = e.x.toInt()
        val item = dataList.getOrNull(index)

        if (item != null) {
            tvValue.text = formatter.formatEnergy(item.energyKwh).asString(context)
            val dateText = formatter.formatSingleDate(item.date, currentFilter).asString(context)
            val prefix = if (currentFilter == ConsumptionTimeFilter.TODAY) "alle" else "il"
            tvDate.text = "$prefix $dateText"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF = MPPointF(-(width / 2).toFloat(), -height.toFloat())

    override fun draw(
        canvas: Canvas,
        posX: Float,
        posY: Float,
    ) {
        val offset = getOffsetForDrawingAtPoint(posX, posY)

        val anchorX = posX + offset.x

        val arrowCenter = posX - anchorX - (arrowView.width / 2f)

        val standardCenterX = width / 2f
        val actualCenterX = posX - anchorX
        val shift = actualCenterX - standardCenterX

        arrowView.translationX = shift

        super.draw(canvas, posX, posY)
    }
}
