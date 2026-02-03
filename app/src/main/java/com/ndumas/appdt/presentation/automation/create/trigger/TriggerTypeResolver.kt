package com.ndumas.appdt.presentation.automation.create.trigger

import com.ndumas.appdt.domain.device.model.SensorAttribute
import javax.inject.Inject

class TriggerTypeResolver
    @Inject
    constructor() {
        fun resolve(attribute: SensorAttribute): TriggerUiType {
            if (!attribute.unit.isNullOrBlank()) {
                return TriggerUiType.NUMERIC
            }

            return if (attribute.value.toDoubleOrNull() != null) {
                TriggerUiType.NUMERIC
            } else {
                TriggerUiType.STATE
            }
        }
    }
