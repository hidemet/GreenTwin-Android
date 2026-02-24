package com.ndumas.appdt.presentation.home.mapper

import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.presentation.home.model.SelectionUiItem.SelectableDeviceItem
import com.ndumas.appdt.presentation.home.model.SelectionUiItem.SelectionGroup
import javax.inject.Inject

class SelectionUiMapper
    @Inject
    constructor() {
        companion object {
            private const val UNASSIGNED_LABEL = "Non assegnati"
        }

        fun mapToSelectionGroups(
            devices: List<Device>,
            selectedIds: Set<String> = emptySet(),
        ): List<SelectionGroup> {
            if (devices.isEmpty()) return emptyList()

            val grouped = devices.groupBy { it.room ?: UNASSIGNED_LABEL }

            // Ordiniamo le stanze (Non assegnati alla fine)
            val sortedRooms =
                grouped.keys.sortedWith { r1, r2 ->
                    when {
                        r1 == UNASSIGNED_LABEL -> 1
                        r2 == UNASSIGNED_LABEL -> -1
                        else -> r1.compareTo(r2)
                    }
                }

            return sortedRooms.map { roomName ->
                val deviceList = grouped[roomName] ?: emptyList()
                val sortedDevices = deviceList.sortedBy { it.name }

                val items =
                    sortedDevices.map { device ->
                        SelectableDeviceItem(
                            device = device,
                            isSelected = selectedIds.contains(device.id),
                        )
                    }

                SelectionGroup(roomName, items)
            }
        }
    }
