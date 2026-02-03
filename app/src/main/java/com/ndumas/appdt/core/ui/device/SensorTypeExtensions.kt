package com.ndumas.appdt.core.ui.device

import androidx.annotation.DrawableRes
import com.ndumas.appdt.R
import com.ndumas.appdt.domain.device.model.SensorIconType

@DrawableRes
fun SensorIconType.getIconRes(): Int =
    when (this) {
        SensorIconType.TEMPERATURE -> R.drawable.ic_thermostat
        SensorIconType.HUMIDITY -> R.drawable.ic_humidity_percentage
        SensorIconType.BATTERY -> R.drawable.ic_battery
        SensorIconType.POWER -> R.drawable.ic_bolt
        SensorIconType.VOLTAGE -> R.drawable.ic_bolt
        SensorIconType.CURRENT -> R.drawable.ic_bolt
        SensorIconType.GENERIC -> R.drawable.ic_sensor
    }
