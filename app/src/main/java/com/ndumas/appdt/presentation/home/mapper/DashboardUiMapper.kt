package com.ndumas.appdt.presentation.home.mapper

import com.ndumas.appdt.R
import com.ndumas.appdt.core.ui.UiText
import com.ndumas.appdt.domain.automation.model.Automation
import com.ndumas.appdt.domain.consumption.model.EnergySummary
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.presentation.consumption.model.PredictionState
import com.ndumas.appdt.presentation.home.model.DashboardItem
import com.ndumas.appdt.presentation.home.model.DashboardSectionType
import com.ndumas.appdt.presentation.home.model.RoomGroupInfo
import javax.inject.Inject

class DashboardUiMapper
    @Inject
    constructor() {
        companion object {
            const val ROOM_PREFIX = "room_"
            const val GROUP_PREFIX = "group_"

            // Tutte le sezioni disponibili
            val ALL_SECTIONS =
                setOf(
                    DashboardSectionType.INFO,
                    DashboardSectionType.DEVICES,
                    DashboardSectionType.ROOMS_GROUPS,
                    DashboardSectionType.AUTOMATIONS,
                )
        }

        fun mapToDashboardItems(
            devices: List<Device>,
            automations: List<Automation>,
            savedOrder: List<String>,
            energySummary: EnergySummary?,
            hiddenSections: Set<DashboardSectionType> = emptySet(),
            isEditMode: Boolean = false,
        ): List<DashboardItem> {
            val items = mutableListOf<DashboardItem>()

            // Helper per decidere se mostrare una sezione o il suo contenuto
            fun shouldShowSection(type: DashboardSectionType): Boolean = isEditMode || !hiddenSections.contains(type)

            fun isSectionVisible(type: DashboardSectionType): Boolean = !hiddenSections.contains(type)

            // Se tutte le sezioni sono nascoste e NON siamo in edit mode, mostra EmptyState
            if (!isEditMode && hiddenSections.containsAll(ALL_SECTIONS)) {
                items.add(
                    DashboardItem.EmptyState(
                        id = "empty_all_hidden",
                        title = UiText.StringResource(R.string.dashboard_all_hidden_title),
                        description = UiText.StringResource(R.string.dashboard_all_hidden_description),
                        iconRes = R.drawable.ic_visibility_off,
                    ),
                )
                return items
            }

            // --- SEZIONE INFO ---

            if (shouldShowSection(DashboardSectionType.INFO)) {
                items.add(
                    DashboardItem.SectionHeader(
                        "header_info",
                        "Informazioni",
                        DashboardSectionType.INFO,
                        isVisible = isSectionVisible(DashboardSectionType.INFO),
                    ),
                )

                val todayVal = energySummary?.todayConsumptionKwh ?: 0.0
                val trendVal = energySummary?.trendPercentage ?: 0.0
                val trendState = energySummary?.trendState ?: PredictionState.NEUTRAL

                items.add(
                    DashboardItem.EnergyWidget(
                        currentConsumptionKwh = todayVal,
                        trendPercentage = trendVal,
                        trendState = trendState,
                    ),
                )
            }

            // Estrai stanze e gruppi
            val roomsAndGroups = extractRoomsAndGroups(devices)

            // --- SEZIONE DISPOSITIVI ---
            if (shouldShowSection(DashboardSectionType.DEVICES)) {
                items.add(
                    DashboardItem.SectionHeader(
                        "header_devices",
                        "Dispositivi preferiti",
                        DashboardSectionType.DEVICES,
                        isVisible = isSectionVisible(DashboardSectionType.DEVICES),
                    ),
                )

                val savedDevices = devices.filter { it.id in savedOrder }

                if (savedDevices.isEmpty()) {
                    items.add(
                        DashboardItem.AddPlaceholder(
                            id = "placeholder_device",
                            text = UiText.DynamicString("Aggiungi dispositivo"),
                            sectionType = DashboardSectionType.DEVICES,
                        ),
                    )
                } else {
                    val sortedDevices = sortItemsById(savedDevices.map { DashboardItem.DeviceWidget(it) }, savedOrder)
                    items.addAll(sortedDevices)
                }
            }

            // --- SEZIONE STANZE/GRUPPI ---
            if (shouldShowSection(DashboardSectionType.ROOMS_GROUPS)) {
                items.add(
                    DashboardItem.SectionHeader(
                        "header_rooms_groups",
                        "Stanze/Gruppi preferiti",
                        DashboardSectionType.ROOMS_GROUPS,
                        isVisible = isSectionVisible(DashboardSectionType.ROOMS_GROUPS),
                    ),
                )

                val savedRoomsGroups = roomsAndGroups.filter { it.id in savedOrder }

                if (savedRoomsGroups.isEmpty()) {
                    items.add(
                        DashboardItem.AddPlaceholder(
                            id = "placeholder_room",
                            text = UiText.DynamicString("Aggiungi stanza o gruppo"),
                            sectionType = DashboardSectionType.ROOMS_GROUPS,
                        ),
                    )
                } else {
                    val widgets =
                        savedRoomsGroups.map { info ->
                            DashboardItem.RoomGroupWidget(
                                id = info.id,
                                name = info.name,
                                deviceCount = info.deviceCount,
                                isRoom = info.isRoom,
                            )
                        }
                    val sorted = sortItemsById(widgets, savedOrder)
                    items.addAll(sorted)
                }
            }

            // --- SEZIONE AUTOMAZIONI ---
            if (shouldShowSection(DashboardSectionType.AUTOMATIONS)) {
                items.add(
                    DashboardItem.SectionHeader(
                        "header_automations",
                        "Automazioni preferite",
                        DashboardSectionType.AUTOMATIONS,
                        isVisible = isSectionVisible(DashboardSectionType.AUTOMATIONS),
                    ),
                )

                val savedAutomations = automations.filter { it.id in savedOrder }

                if (savedAutomations.isEmpty()) {
                    items.add(
                        DashboardItem.AddPlaceholder(
                            id = "placeholder_automation",
                            text = UiText.DynamicString("Aggiungi automazione"),
                            sectionType = DashboardSectionType.AUTOMATIONS,
                        ),
                    )
                } else {
                    val widgets =
                        savedAutomations.map { automation ->
                            DashboardItem.AutomationWidget(
                                id = automation.id,
                                name = automation.name,
                                description = automation.description.ifBlank { generateAutomationSummary(automation) },
                                isActive = automation.isActive,
                            )
                        }
                    val sorted = sortItemsById(widgets, savedOrder)
                    items.addAll(sorted)
                }
            }

            return items
        }

        fun extractRoomsAndGroups(devices: List<Device>): List<RoomGroupInfo> {
            val result = mutableListOf<RoomGroupInfo>()

            // Estrai stanze (raggruppando per room name)
            devices
                .filter { !it.room.isNullOrBlank() }
                .groupBy { it.room!! }
                .forEach { (roomName, deviceList) ->
                    result.add(
                        RoomGroupInfo(
                            id = "$ROOM_PREFIX$roomName",
                            name = roomName,
                            deviceCount = deviceList.size,
                            isRoom = true,
                        ),
                    )
                }

            // Estrai gruppi
            devices
                .flatMap { device -> device.groups.map { group -> group to device } }
                .groupBy { it.first.name }
                .forEach { (groupName, pairs) ->
                    val uniqueDevices = pairs.map { it.second }.distinctBy { it.id }
                    result.add(
                        RoomGroupInfo(
                            id = "$GROUP_PREFIX$groupName",
                            name = groupName,
                            deviceCount = uniqueDevices.size,
                            isRoom = false,
                        ),
                    )
                }

            return result
        }

        private fun generateAutomationSummary(automation: Automation): String {
            val actionCount = automation.actions.size
            return when {
                actionCount == 0 -> "Nessuna azione"
                actionCount == 1 -> "1 azione"
                else -> "$actionCount azioni"
            }
        }

        private fun <T : DashboardItem> sortItemsById(
            items: List<T>,
            savedOrder: List<String>,
        ): List<T> {
            if (savedOrder.isEmpty()) return items

            return items.sortedBy { item ->
                val index = savedOrder.indexOf(item.id)

                if (index == -1) Int.MAX_VALUE else index
            }
        }
    }
