package com.example.opsc7311_sem2_2024

import android.view.LayoutInflater
import android.widget.TextView
import  android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView

class SettingsAdapter (private val dataList: ArrayList<SettingsDataClass>): RecyclerView.Adapter<SettingsAdapter.ViewHolderClass>()     {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.settings_options_layout, parent, false)
        return ViewHolderClass(itemView)

    }

    override fun getItemCount(): Int {

        return dataList.size

    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.tvSettingsTitle.setText(currentItem.settingsTitle)


    }


    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView){

        val tvSettingsTitle: TextView = itemView.findViewById(R.id.tv_SettingsTitle)


    }

}