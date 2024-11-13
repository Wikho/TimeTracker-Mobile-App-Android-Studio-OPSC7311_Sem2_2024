package com.example.opsc7311_sem2_2024.Settings

enum class SettingType {
    SWITCH,
    DROPDOWN,
    TEXT
}

data class SettingsDataClass(
    val settingsTitle: String,
    var selectedOption: String = "",
    var isEnabled: Boolean = false,
    val type: SettingType
)

