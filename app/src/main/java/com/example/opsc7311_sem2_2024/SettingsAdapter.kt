package com.example.opsc7311_sem2_2024

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer.ListListener
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.SettingsOptionsLayoutBinding
import com.example.opsc7311_sem2_2024.databinding.TaskItemLayoutBinding

class SettingsAdapter (private val listener: SettingsListener): RecyclerView.Adapter<SettingsAdapter.ViewHolderClass>()     {

    private val dataList = mutableListOf<SettingsDataClass>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = SettingsOptionsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderClass(itemView)

    }



    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.bind(currentItem)


    }
    override fun getItemCount(): Int {

        return dataList.size

    }

    inner class ViewHolderClass(private val binding: SettingsOptionsLayoutBinding): RecyclerView.ViewHolder(binding.root){

        //val tvSettingsTitle: TextView = textView.findViewById(R.id.tv_SettingsTitle)
        fun bind(setting: SettingsDataClass){

            binding.tvSettingsTitle.text = setting.settingsTitle

        }


    }

    interface SettingsListener {
        fun onSettingsListener(setting: SettingsDataClass)
    }

}