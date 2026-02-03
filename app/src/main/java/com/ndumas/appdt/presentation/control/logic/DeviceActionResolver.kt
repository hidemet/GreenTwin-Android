package com.ndumas.appdt.presentation.control.logic

import com.ndumas.appdt.domain.device.model.DeviceType
import javax.inject.Inject

/**
 * Esempio:
 * - LIGHT (ON) -> turn_off
 * - LOCK (LOCKED) -> unlock
 * - BLINDS (OPEN) -> close_cover
 */
class DeviceActionResolver
    @Inject
    constructor() {
        /**
         * Risolve il nome del servizio di toggle (accensione/spegnimento/apertura).
         *
         * @param type Il tipo di dispositivo (dominio).
         * @param isOn Lo stato attuale del dispositivo (true = acceso/aperto/sbloccato).
         * @return La stringa del servizio da chiamare (es. "turn_on", "lock").
         */
        fun resolveToggleService(
            type: DeviceType,
            isOn: Boolean,
        ): String =
            when (type) {
                DeviceType.LIGHT,
                DeviceType.SWITCH,
                DeviceType.FAN,
                DeviceType.AIR_CONDITIONER,
                DeviceType.REFRIGERATOR,
                DeviceType.DISHWASHER,
                DeviceType.WASHING_MACHINE,
                DeviceType.OVEN,
                DeviceType.MICROWAVE,
                DeviceType.INDUCTION_STOVE,
                DeviceType.DESKTOP,
                -> {
                    if (isOn) "turn_off" else "turn_on"
                }

                DeviceType.TV,
                DeviceType.MEDIA_PLAYER,
                DeviceType.SPEAKER,
                -> {
                    if (isOn) "turn_off" else "turn_on"
                }

                DeviceType.LOCK -> {
                    if (isOn) "lock" else "unlock"
                }

                DeviceType.BLINDS,
                DeviceType.WINDOW,
                DeviceType.DOOR,
                DeviceType.DOORBELL,
                -> {
                    if (isOn) "close_cover" else "open_cover"
                }

                DeviceType.BUTTON -> {
                    "press"
                }

                DeviceType.SENSOR,
                DeviceType.ROOM,
                DeviceType.GROUP,
                DeviceType.CAMERA,
                DeviceType.THERMOSTAT,
                DeviceType.OTHER,
                -> {
                    if (isOn) "turn_off" else "turn_on"
                }
            }

        /**
         * Risolve il servizio Play/Pause basandosi sullo stato attuale.
         * @param isPlaying true se sta suonando (quindi invio Pausa), false altrimenti.
         */
        fun resolvePlayPauseService(isPlaying: Boolean): String = if (isPlaying) "media_pause" else "media_play"

        /**
         * Risolve i comandi di navigazione (Next/Prev).
         */
        fun resolveMediaNavigationService(isNext: Boolean): String = if (isNext) "media_next_track" else "media_previous_track"
    }
