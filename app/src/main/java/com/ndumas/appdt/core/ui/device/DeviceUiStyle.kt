package com.ndumas.appdt.core.ui.device

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class DeviceUiStyle(
    @DrawableRes val iconRes: Int,
    @ColorRes val activeColorRes: Int,
)
