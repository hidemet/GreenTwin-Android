package com.ndumas.appdt.presentation.device.mapper

import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.presentation.home.model.DashboardItem
import com.ndumas.appdt.presentation.home.model.DashboardSectionType
import javax.inject.Inject

enum class DeviceGrouping {
    ROOM,
    GROUP,
    UNASSIGNED,
}

class DeviceListUiMapper
    @Inject
    constructor() {
        /**
         * Mappa i dispositivi in una lista di DashboardItem con supporto accordion.
         *
         * @param devices Lista dei dispositivi da mappare
         * @param grouping Tipo di raggruppamento (Room, Group, Unassigned)
         * @param expandedSections Set di header ID espansi. Se null, tutte le sezioni sono espanse.
         *                         Se targetSection è specificato, solo quella sezione è espansa.
         * @param targetSection Nome della sezione da espandere (usato per navigazione dalla Home)
         */
        fun mapToDeviceList(
            devices: List<Device>,
            grouping: DeviceGrouping = DeviceGrouping.ROOM,
            expandedSections: Set<String>? = null,
            targetSection: String? = null,
        ): List<DashboardItem> {
            val groupedMap: Map<String, List<Device>> =
                when (grouping) {
                    DeviceGrouping.ROOM -> {
                        // Escludi dispositivi senza stanza (saranno mostrati in tab "Non assegnati")
                        devices
                            .filter { !it.room.isNullOrBlank() }
                            .groupBy { it.room!! }
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

            // Calcola quali sezioni devono essere espanse
            val effectiveExpandedSections: Set<String> =
                when {
                    // Se c'è un target specifico dalla navigazione, solo quella sezione è espansa
                    targetSection != null -> {
                        setOf("header_$targetSection")
                    }

                    // Se expandedSections è null, tutte sono espanse
                    expandedSections == null -> {
                        groupedMap.keys.map { "header_$it" }.toSet()
                    }

                    // Altrimenti usa le sezioni espanse fornite
                    else -> {
                        expandedSections
                    }
                }

            groupedMap.toSortedMap().forEach { (headerTitle, deviceList) ->
                val headerId = "header_$headerTitle"
                val isExpanded = effectiveExpandedSections.contains(headerId)

                items.add(
                    DashboardItem.SectionHeader(
                        id = headerId,
                        title = headerTitle,
                        sectionType = DashboardSectionType.DEVICES,
                        isExpanded = isExpanded,
                    ),
                )

                // Aggiungi i dispositivi solo se la sezione è espansa
                if (isExpanded) {
                    items.addAll(
                        deviceList.sortedBy { it.name }.map { DashboardItem.DeviceWidget(it) },
                    )
                }
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
