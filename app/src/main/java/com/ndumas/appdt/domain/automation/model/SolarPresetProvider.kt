package com.ndumas.appdt.domain.automation.model

import javax.inject.Inject

/**
 * Fornisce la lista degli offset (in minuti) supportati dal sistema per i trigger solari.
 * Contiene la Business Logic su quali intervalli sono validi.
 */
class SolarPresetProvider
    @Inject
    constructor() {
        fun getPresets(): List<Long> =
            listOf(
                -240L,
                -180L,
                -120L,
                -60L,
                -45L,
                -30L,
                -15L,
                0L,
                15L,
                30L,
                45L,
                60L,
                120L,
                180L,
                240L,
            )
    }
