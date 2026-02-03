package com.ndumas.appdt.presentation.home.mapper

import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.presentation.home.model.SelectionItem
import javax.inject.Inject

class SelectionUiMapper
    @Inject
    constructor() {
        companion object {
            private const val UNASSIGNED_LABEL = "Non assegnati"
        }

        fun mapToSelectionList(devices: List<Device>): List<SelectionItem> {
            if (devices.isEmpty()) return emptyList()

            val roomComparator =
                Comparator<String> { room1, room2 ->
                    when {
                        room1 == UNASSIGNED_LABEL -> 1
                        room2 == UNASSIGNED_LABEL -> -1
                        else -> room1.compareTo(room2)
                    }
                }

            val grouped =
                devices
                    .groupBy { it.room ?: UNASSIGNED_LABEL }
                    .toSortedMap(roomComparator)

            val result = mutableListOf<SelectionItem>()

            grouped.forEach { (roomName, deviceList) ->
                result.add(SelectionItem.Header(roomName))

                val sortedDevices = deviceList.sortedBy { it.name }

                sortedDevices.forEach { device ->

                    result.add(SelectionItem.SelectableDevice(device))
                }
            }

            return result
        }
    }
