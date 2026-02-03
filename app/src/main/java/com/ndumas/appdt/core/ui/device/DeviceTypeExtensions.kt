package com.ndumas.appdt.core.ui.device

import com.ndumas.appdt.R
import com.ndumas.appdt.domain.device.model.DeviceType

fun DeviceType.getUiStyle(): DeviceUiStyle =
    when (this) {
        // Clima
        DeviceType.AIR_CONDITIONER -> DeviceUiStyle(R.drawable.ic_ac_unit, R.color.tw_cyan_600)

        DeviceType.THERMOSTAT -> DeviceUiStyle(R.drawable.ic_thermostat, R.color.tw_orange_300)

        DeviceType.FAN -> DeviceUiStyle(R.drawable.ic_fan, R.color.tw_blue_500)

        // Elettrodomestici Cucina
        DeviceType.REFRIGERATOR -> DeviceUiStyle(R.drawable.ic_fridge, R.color.tw_teal_600)

        DeviceType.DISHWASHER -> DeviceUiStyle(R.drawable.ic_dishwasher, R.color.tw_blue_500)

        DeviceType.INDUCTION_STOVE -> DeviceUiStyle(R.drawable.ic_stove, R.color.tw_gray_500)

        DeviceType.MICROWAVE -> DeviceUiStyle(R.drawable.ic_microwave, R.color.tw_amber_600)

        DeviceType.OVEN -> DeviceUiStyle(R.drawable.ic_oven, R.color.tw_red_400)

        // Luci e Interruttori
        DeviceType.LIGHT -> DeviceUiStyle(R.drawable.ic_lightbulb, R.color.tw_yellow_500)

        DeviceType.SWITCH -> DeviceUiStyle(R.drawable.ic_switch, R.color.tw_indigo_400)

        DeviceType.BUTTON -> DeviceUiStyle(R.drawable.ic_smart_button, R.color.tw_green_600)

        // Multimedia
        DeviceType.TV -> DeviceUiStyle(R.drawable.ic_tv, R.color.tw_violet_600)

        DeviceType.MEDIA_PLAYER -> DeviceUiStyle(R.drawable.ic_media_player, R.color.tw_purple_600)

        DeviceType.SPEAKER -> DeviceUiStyle(R.drawable.ic_speaker, R.color.tw_pink_600)

        DeviceType.DESKTOP -> DeviceUiStyle(R.drawable.ic_desktop, R.color.tw_blue_500)

        // Sicurezza e Accessi
        DeviceType.CAMERA -> DeviceUiStyle(R.drawable.ic_camera, R.color.tw_blue_400)

        DeviceType.DOOR -> DeviceUiStyle(R.drawable.ic_door, R.color.tw_red_400)

        DeviceType.DOORBELL -> DeviceUiStyle(R.drawable.ic_doorbell, R.color.tw_lime_600)

        DeviceType.LOCK -> DeviceUiStyle(R.drawable.ic_lock, R.color.tw_gray_400)

        // Casa e Sensori
        DeviceType.WASHING_MACHINE -> DeviceUiStyle(R.drawable.ic_washing_machine, R.color.tw_indigo_400)

        DeviceType.BLINDS -> DeviceUiStyle(R.drawable.ic_blinds, R.color.tw_violet_600)

        DeviceType.WINDOW -> DeviceUiStyle(R.drawable.ic_window, R.color.tw_sky_600)

        DeviceType.SENSOR -> DeviceUiStyle(R.drawable.ic_sensor, R.color.tw_emerald_600)

        // Aggregazioni
        DeviceType.ROOM -> DeviceUiStyle(R.drawable.ic_door_open, R.color.tw_orange_400)

        DeviceType.GROUP -> DeviceUiStyle(R.drawable.ic_group_work, R.color.tw_purple_600)

        // Fallback
        DeviceType.OTHER -> DeviceUiStyle(R.drawable.ic_devices, R.color.tw_gray_400)
    }
