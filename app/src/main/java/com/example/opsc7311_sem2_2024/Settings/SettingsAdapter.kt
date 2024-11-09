package com.example.opsc7311_sem2_2024.Settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.SettingsOptionsLayoutBinding

class SettingsAdapter(
    private val settingsList: List<SettingsDataClass>,
    private val listener: SettingsListener
) : RecyclerView.Adapter<SettingsAdapter.ViewHolderClass>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val binding = SettingsOptionsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = settingsList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int = settingsList.size

    inner class ViewHolderClass(private val binding: SettingsOptionsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(setting: SettingsDataClass) {
            binding.tvSettingsTitle.text = setting.settingsTitle
            binding.switchToggleSetting.isChecked = setting.isEnabled

            // Set up listener for the switch
            binding.switchToggleSetting.setOnCheckedChangeListener { _, isChecked ->
                setting.isEnabled = isChecked
                listener.onSettingsChanged(setting)
            }
        }
    }

    interface SettingsListener {
        fun onSettingsChanged(setting: SettingsDataClass)
    }
}
