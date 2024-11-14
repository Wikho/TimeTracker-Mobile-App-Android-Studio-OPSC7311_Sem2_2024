import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.Settings.SettingType
import com.example.opsc7311_sem2_2024.Settings.SettingsDataClass

object SettingsSingleton {
    private val settingsMap = mutableMapOf<String, Any>()
    private val firebaseManager = FirebaseManager()

    fun saveSetting(setting: SettingsDataClass) {
        when (setting.type) {
            SettingType.SWITCH -> settingsMap[setting.settingsTitle] = setting.isEnabled
            SettingType.DROPDOWN -> settingsMap[setting.settingsTitle] = setting.selectedOption
            SettingType.TEXT -> settingsMap[setting.settingsTitle] = setting.selectedOption
        }
        // Save to Firebase
        firebaseManager.saveUserSettings(settingsMap) { success, message ->
            if (!success) {
                // Handle error if needed
            }
        }
    }

    fun getSettingValue(title: String): Any? {
        return settingsMap[title]
    }

    fun loadSettings(onComplete: () -> Unit) {
        firebaseManager.fetchUserSettings { settings ->
            settingsMap.clear()
            settingsMap.putAll(settings)
            onComplete()
        }
    }
}