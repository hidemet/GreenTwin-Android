package com.ndumas.appdt.presentation.home.mapper

import com.ndumas.appdt.domain.consumption.model.EnergySummary
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.presentation.home.model.DashboardItem
import javax.inject.Inject
import kotlin.math.roundToInt

class DashboardUiMapper
    @Inject
    constructor() {
        /**
         * Costruisce la lista completa per la Dashboard unendo Dispositivi, Consumi e Preferenze.
         */
        fun mapToDashboardItems(
            devices: List<Device>,
            savedOrder: List<String>,
            energySummary: EnergySummary?,
        ): List<DashboardItem> {
            val items = mutableListOf<DashboardItem>()

            items.add(DashboardItem.SectionHeader("header_info", "Informazioni"))

            val todayVal = energySummary?.todayConsumptionKwh ?: 0.0
            val yesterdayVal = energySummary?.yesterdayConsumptionKwh ?: 0.0

            val trendVal = energySummary?.trendPercentage?.roundToInt() ?: 0

            items.add(
                DashboardItem.EnergyWidget(
                    currentPowerKw = todayVal,
                    yesterdayPowerKw = yesterdayVal,
                    trend = trendVal,
                    lastUpdate = System.currentTimeMillis(),
                ),
            )

            if (devices.isNotEmpty()) {
                items.add(DashboardItem.SectionHeader("header_devices", "Dispositivi preferiti"))

                val deviceWidgets = devices.map { DashboardItem.DeviceWidget(it) }

                val sortedDevices = sortItemsById(deviceWidgets, savedOrder)
                items.addAll(sortedDevices)
            }

            items.add(DashboardItem.SectionHeader("header_automations", "Automazioni preferite"))

            val automationWidgets =
                listOf(
                    DashboardItem.AutomationWidget(
                        id = "auto_1",
                        name = "Serata TV",
                        description = "Luci soffuse",
                        isActive = true,
                    ),
                    DashboardItem.AutomationWidget(
                        id = "auto_2",
                        name = "Luce cinema",
                        description = "Tutto spento",
                        isActive = true,
                    ),
                    DashboardItem.AutomationWidget(
                        id = "auto_3",
                        name = "Uscita casa",
                        description = "Allarme ON",
                        isActive = false,
                    ),
                )

            val sortedAutomations = sortItemsById(automationWidgets, savedOrder)
            items.addAll(sortedAutomations)

            return items
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
