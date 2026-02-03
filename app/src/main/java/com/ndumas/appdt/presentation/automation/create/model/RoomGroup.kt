package com.ndumas.appdt.presentation.automation.create.model

import com.ndumas.appdt.domain.device.model.Device

data class RoomGroup(
    val roomName: String,
    val devices: List<Device>,
)
