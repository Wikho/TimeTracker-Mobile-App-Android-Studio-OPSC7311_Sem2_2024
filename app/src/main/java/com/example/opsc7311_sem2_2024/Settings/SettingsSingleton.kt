import com.example.opsc7311_sem2_2024.Settings.SettingType
import com.example.opsc7311_sem2_2024.Settings.SettingsDataClass

object SettingsSingleton {
    private val settingsMap = mutableMapOf<String, Any>()

    fun saveSetting(setting: SettingsDataClass) {
        when (setting.type) {
            SettingType.SWITCH -> settingsMap[setting.settingsTitle] = setting.isEnabled
            SettingType.DROPDOWN -> settingsMap[setting.settingsTitle] = setting.selectedOption
            SettingType.TEXT -> settingsMap[setting.settingsTitle] = setting.selectedOption
        }
    }

    fun getSettingValue(title: String): Any? {
        return settingsMap[title]
    }
}