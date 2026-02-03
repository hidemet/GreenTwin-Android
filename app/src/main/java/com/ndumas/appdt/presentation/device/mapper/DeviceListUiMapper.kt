package com.ndumas.appdt.presentation.device.mapper

import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.presentation.home.model.DashboardItem
import javax.inject.Inject

enum class DeviceGrouping {
    ROOM,
    GROUP,
    UNASSIGNED,
}

class DeviceListUiMapper
    @Inject
    constructor() {
        fun mapToDeviceList(
            devices: List<Device>,
            grouping: DeviceGrouping = DeviceGrouping.ROOM,
        ): List<DashboardItem> {
            val groupedMap: Map<String, List<Device>> =
                when (grouping) {
                    DeviceGrouping.ROOM -> {
                        devices.groupBy {
                            if (it.room.isNullOrBlank()) "Non assegnati" else it.room
                        }
                    }

                    DeviceGrouping.GROUP -> {
                        val tempMap = mutableMapOf<String, MutableList<Device>>()
                        var hasAnyGroup = false

                        devices.forEach { device ->
                            if (device.groups.isNotEmpty()) {
                                hasAnyGroup = true
                                device.groups.forEach { group ->
                                    tempMap.getOrPut(group.name) { mutableListOf() }.add(device)
                                }
                            }
                        }

                        if (!hasAnyGroup) emptyMap() else tempMap
                    }

                    DeviceGrouping.UNASSIGNED -> {
                        val unassigned = devices.filter { it.room.isNullOrBlank() }

                        if (unassigned.isNotEmpty()) {
                            mapOf("Non assegnati" to unassigned)
                        } else {
                            emptyMap()
                        }
                    }
                }

            if (groupedMap.isEmpty()) {
                return listOf(createEmptyState(grouping))
            }

            val items = mutableListOf<DashboardItem>()

            groupedMap.toSortedMap().forEach { (headerTitle, deviceList) ->

                items.add(DashboardItem.SectionHeader("header_$headerTitle", headerTitle))

                items.addAll(
                    deviceList.sortedBy { it.name }.map { DashboardItem.DeviceWidget(it) },
                )
            }

            return items
        }

        private fun createEmptyState(grouping: DeviceGrouping): DashboardItem.EmptyState =
            when (grouping) {
                DeviceGrouping.ROOM -> {
                    DashboardItem.EmptyState(
                        title = UiText.StringResource(R.string.empty_rooms_title),
                        description = UiText.StringResource(R.string.empty_rooms_desc),
                        iconRes = R.drawable.ic_home,
                    )
                }

                DeviceGrouping.GROUP -> {
                    DashboardItem.EmptyState(
                        title = UiText.StringResource(R.string.empty_groups_title),
                        description = UiText.StringResource(R.string.empty_groups_desc),
                        iconRes = R.drawable.ic_group_work,
                    )
                }

                DeviceGrouping.UNASSIGNED -> {
                    DashboardItem.EmptyState(
                        title = UiText.StringResource(R.string.empty_unassigned_title),
                        description = UiText.StringResource(R.string.empty_unassigned_desc),
                        iconRes = R.drawable.ic_check,
                    )
                }
            }
    }
