package com.ndumas.appdt.data.automation.mapper

import com.ndumas.appdt.data.automation.remote.dto.*
import com.ndumas.appdt.domain.automation.model.*
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class AutomationMapper
    @Inject
    constructor() {
        //  DOMAIN -> DTO (Scrittura)
        fun mapToRequest(draft: AutomationDraft): AutomationRequestDto {
            val triggers = draft.trigger?.let { listOf(mapTrigger(it)) } ?: emptyList()
            val actions = draft.actions.map { mapAction(it) }

            val conditions = mutableListOf<Map<String, Any>>()

            // Logica Giorni (Time o Solar) -> Va in 'condition'
            val trigger = draft.trigger
            if (trigger is AutomationTrigger.Time && trigger.days.isNotEmpty()) {
                conditions.add(mapDaysToCondition(trigger.days))
            } else if (trigger is AutomationTrigger.Solar && trigger.days.isNotEmpty()) {
                conditions.add(mapDaysToCondition(trigger.days))
            }

            // Generazione ID
            // System.currentTimeMillis() restituisce 13 cifre (millisecondi epoch).
            val generatedId = System.currentTimeMillis().toString()

            return AutomationRequestDto(
                automation =
                    AutomationDto(
                        id = generatedId,
                        alias = draft.name,
                        description = draft.description,
                        mode = "single",
                        trigger = triggers,
                        condition = conditions,
                        action = actions,
                    ),
            )
        }

        private fun mapTrigger(trigger: AutomationTrigger): TriggerDto =
            when (trigger) {
                is AutomationTrigger.Time -> {
                    TriggerDto(
                        platform = "time",
                        at = trigger.time.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    )
                }

                is AutomationTrigger.Solar -> {
                    TriggerDto(
                        platform = "sun",
                        event = if (trigger.event == SolarEvent.SUNRISE) "sunrise" else "sunset",
                        offset = trigger.offsetMinutes * 60,
                    )
                }

                is AutomationTrigger.DeviceState -> {
                    TriggerDto(
                        platform = "device",
                        deviceId = trigger.deviceId,
                        domain = "sensor",
                        type = trigger.attribute,
                    )
                }
            }

        private fun mapAction(action: AutomationAction): ActionDto =
            when (action) {
                is AutomationAction.DeviceAction -> {
                    ActionDto(
                        service = "${action.domain}.${action.service}",
                        target = TargetDto(deviceId = listOf(action.deviceId)),
                        data = action.parameters.ifEmpty { null },
                    )
                }
            }

        private fun mapDaysToCondition(days: List<DayOfWeek>): Map<String, Any> {
            val haDays = days.map { it.name.take(3).lowercase(Locale.US) }
            return mapOf("condition" to "time", "weekday" to haDays)
        }

        // --- DTO -> DOMAIN (Lettura) ---

        fun mapDtoToDomain(dto: AutomationDto): Automation {
            val safeId = dto.id ?: "unknown_${dto.hashCode()}"
            val safeName = dto.name ?: dto.alias ?: "Senza nome"

            val weekdays = extractWeekdaysFromConditions(dto.condition)

            return Automation(
                id = safeId,
                name = safeName,
                description = dto.description,
                isActive = dto.mode != "off",
                // 2. PASSAGGIO GIORNI AL TRIGGER
                triggers = dto.trigger.mapNotNull { mapTriggerDto(it, weekdays) },
                actions = dto.action.mapNotNull { mapActionDto(it) },
            )
        }

        private fun extractWeekdaysFromConditions(conditions: List<Map<String, Any>>): List<DayOfWeek> {
            val timeCondition = conditions.find { it["condition"] == "time" } ?: return emptyList()

            @Suppress("UNCHECKED_CAST")
            val daysList = timeCondition["weekday"] as? List<String> ?: return emptyList()

            return daysList.mapNotNull { dayStr ->
                when (dayStr.lowercase()) {
                    "mon" -> DayOfWeek.MONDAY
                    "tue" -> DayOfWeek.TUESDAY
                    "wed" -> DayOfWeek.WEDNESDAY
                    "thu" -> DayOfWeek.THURSDAY
                    "fri" -> DayOfWeek.FRIDAY
                    "sat" -> DayOfWeek.SATURDAY
                    "sun" -> DayOfWeek.SUNDAY
                    else -> null
                }
            }
        }

        private fun mapTriggerDto(
            dto: TriggerDto,
            days: List<DayOfWeek>,
        ): AutomationTrigger? =
            try {
                val type = dto.platform ?: dto.triggerType ?: ""

                when (type) {
                    "time" -> {
                        dto.at?.let {
                            val timeStr = if (it.length == 5) "$it:00" else it
                            val time = java.time.LocalTime.parse(timeStr)
                            AutomationTrigger.Time(time, days)
                        }
                    }

                    "sun" -> {
                        val event = if (dto.event == "sunrise") SolarEvent.SUNRISE else SolarEvent.SUNSET
                        val offsetMins = (dto.offset ?: 0L) / 60
                        AutomationTrigger.Solar(event, offsetMins, days)
                    }

                    else -> {
                        if (dto.platform == "device") {
                            AutomationTrigger.DeviceState(
                                deviceId = dto.deviceId ?: "",
                                deviceName = "",
                                attribute = dto.type ?: "",
                                operator = "",
                                value = "",
                            )
                        } else {
                            null
                        }
                    }
                }
            } catch (e: Exception) {
                null
            }

        private fun mapActionDto(dto: ActionDto): AutomationAction? {
            return try {
                // Risoluzione Service/Domain
                val domain: String
                val service: String

                if (dto.domain != null) {
                    domain = dto.domain
                    service = dto.service
                } else if (dto.service.contains(".")) {
                    // Fallback formato standard HA "light.turn_on"
                    val parts = dto.service.split(".")
                    domain = parts[0]
                    service = parts[1]
                } else {
                    domain = "generic"
                    service = dto.service
                }

                val deviceId =
                    dto.flatDeviceId
                        ?: dto.target?.deviceId?.firstOrNull()
                        ?: ""

                if (deviceId.isBlank()) return null // Se non c'è device, saltiamo

                AutomationAction.DeviceAction(
                    deviceId = deviceId,
                    deviceName = deviceId,
                    domain = domain,
                    service = service,
                    parameters = dto.data ?: emptyMap(),
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun mapSimulationDtoToDomain(dto: SimulationResponseDto): SimulationResult =
            SimulationResult(
                automation = mapDtoToDomain(dto.automation),
                conflicts = dto.conflicts.map { mapConflictDto(it) },
                suggestions = dto.suggestions.map { mapSuggestionDto(it) },
            )

        private fun mapConflictDto(dto: ConflictDto): AutomationConflict =
            AutomationConflict(
                type = dto.type,
                description = dto.description,
                threshold = dto.threshold,
            )

        private fun mapSuggestionDto(dto: SuggestionDto): AutomationSuggestion =
            AutomationSuggestion(
                type = dto.type,
                newTime = dto.newActivationTime,
                savings = dto.savedMoney,
            )
    }
