package com.example.opsc7311_sem2_2024.Settings

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.FirebaseManager
import com.example.opsc7311_sem2_2024.LogSignIn.LoginActivity
import com.example.opsc7311_sem2_2024.R
import com.example.opsc7311_sem2_2024.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment(), SettingsAdapter.SettingsListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sAdapter: SettingsAdapter
    private val settingsList = mutableListOf<SettingsDataClass>()

    private val firebaseManager = FirebaseManager()
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Fetch and set the user's name
        firebaseManager.getUserName { userName ->
            binding.tvUsername.text = userName ?: getString(R.string.user_name)
        }

        // Fetch and set the user's email
        firebaseManager.getUserEmail { userEmail ->
            binding.tvEmail.text = userEmail ?: getString(R.string.user_email)
        }


        // Initialize settings list
        SettingsSingleton.loadSettings {
            // Initialize settings list with loaded values
            settingsList.apply {
                clear()
                add(SettingsDataClass(
                    settingsTitle = "Note Default Priority",
                    selectedOption = SettingsSingleton.getSettingValue("Note Default Priority") as? String ?: "Medium",
                    type = SettingType.DROPDOWN
                ))
                add(SettingsDataClass(
                    settingsTitle = "Break Reminder",
                    selectedOption = SettingsSingleton.getSettingValue("Break Reminder") as? String ?: "25",
                    type = SettingType.DROPDOWN
                ))
                add(SettingsDataClass(
                    settingsTitle = "Sound",
                    isEnabled = SettingsSingleton.getSettingValue("Sound") as? Boolean ?: true,
                    type = SettingType.SWITCH
                ))
                add(SettingsDataClass(
                    settingsTitle = "Notification",
                    isEnabled = SettingsSingleton.getSettingValue("Notification") as? Boolean ?: true,
                    type = SettingType.SWITCH
                ))
                add(SettingsDataClass(
                    settingsTitle = "App Version",
                    selectedOption = getAppVersion(),
                    type = SettingType.TEXT
                ))
            }

            // Notify adapter of data change
            sAdapter.notifyDataSetChanged()
        }

        // Set up RecyclerView
        sAdapter = SettingsAdapter(settingsList, this)
        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sAdapter
        }



    }

    override fun onSettingsChanged(setting: SettingsDataClass) {
        // Handle settings change
        SettingsSingleton.saveSetting(setting)
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName ?: "N/A"
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"
        }
    }
}
