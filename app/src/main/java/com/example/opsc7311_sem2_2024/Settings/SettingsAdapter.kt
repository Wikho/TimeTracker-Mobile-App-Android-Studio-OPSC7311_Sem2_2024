package com.example.opsc7311_sem2_2024.Settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.R

class SettingsAdapter(
    private val settingsList: List<SettingsDataClass>,
    private val listener: SettingsListener
) : RecyclerView.Adapter<SettingsAdapter.ViewHolderClass>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.settings_item_layout, parent, false)
        return ViewHolderClass(view)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = settingsList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int = settingsList.size

    inner class ViewHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvSettingTitle: TextView = itemView.findViewById(R.id.tvSettingTitle)
        private val switchSetting: Switch = itemView.findViewById(R.id.switchSetting)
        private val spinnerSetting: Spinner = itemView.findViewById(R.id.spinnerSetting)
        private val tvAppVersion: TextView = itemView.findViewById(R.id.tvAppVersion)

        fun bind(setting: SettingsDataClass) {
            tvSettingTitle.text = setting.settingsTitle

            when (setting.type) {
                SettingType.SWITCH -> {
                    switchSetting.visibility = View.VISIBLE
                    spinnerSetting.visibility = View.GONE
                    tvAppVersion.visibility = View.GONE

                    switchSetting.isChecked = setting.isEnabled
                    switchSetting.setOnCheckedChangeListener { _, isChecked ->
                        setting.isEnabled = isChecked
                        listener.onSettingsChanged(setting)
                    }
                }
                SettingType.DROPDOWN -> {
                    switchSetting.visibility = View.GONE
                    spinnerSetting.visibility = View.VISIBLE
                    tvAppVersion.visibility = View.GONE

                    val options = when (setting.settingsTitle) {
                        "Note Default Priority" -> listOf("Low", "Medium", "High")
                        "Break Reminder" -> listOf("Off", "10", "15", "25", "30", "45", "60")
                        else -> emptyList()
                    }

                    val adapter = ArrayAdapter(
                        itemView.context,
                        android.R.layout.simple_spinner_item,
                        options
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerSetting.adapter = adapter
                    spinnerSetting.setSelection(options.indexOf(setting.selectedOption))
                    spinnerSetting.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>, view: View?, position: Int, id: Long
                        ) {
                            setting.selectedOption = options[position]
                            listener.onSettingsChanged(setting)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
                SettingType.TEXT -> {
                    switchSetting.visibility = View.GONE
                    spinnerSetting.visibility = View.GONE
                    tvAppVersion.visibility = View.VISIBLE

                    tvAppVersion.text = setting.selectedOption
                }
            }
        }
    }

    interface SettingsListener {
        fun onSettingsChanged(setting: SettingsDataClass)
    }
}
