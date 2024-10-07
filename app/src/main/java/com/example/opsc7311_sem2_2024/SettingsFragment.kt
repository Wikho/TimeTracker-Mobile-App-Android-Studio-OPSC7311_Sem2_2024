package com.example.opsc7311_sem2_2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.opsc7311_sem2_2024.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(), SettingsAdapter.SettingsListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sAdapter: SettingsAdapter
    private val settingsList = mutableListOf<SettingsDataClass>()

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
        super.onViewCreated(view, savedInstanceState)

        // Initialize settings list
        settingsList.apply {
            add(SettingsDataClass("Enable Notifications", true))
            add(SettingsDataClass("Dark Mode", false))
            // Add more settings as needed
        }

        // Set up RecyclerView
        sAdapter = SettingsAdapter(settingsList, this)
        binding.rvGeneralSettings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sAdapter
        }
    }

    override fun onSettingsChanged(setting: SettingsDataClass) {

        // Handle settings change
        Toast.makeText(
            requireContext(),
            "${setting.settingsTitle} is now ${if (setting.isEnabled) "enabled" else "disabled"}",
            Toast.LENGTH_SHORT
        ).show()


    }
}
