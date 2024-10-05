package com.example.opsc7311_sem2_2024

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    // Declare the binding variable
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<SettingsDataClass>
    lateinit var titleList: Array<String>

    private lateinit var sAdapater: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)



        titleList = arrayOf(
            "First Setting",
            "Second Setting",
            "Third Setting",
            "Fourth Setting",
            "Fifth Setting")

        binding.rvGeneralSettings.layoutManager = LinearLayoutManager(context)
        binding.rvGeneralSettings.adapter = sAdapater

        dataList = arrayListOf<SettingsDataClass>()
        getData()

        return binding.root
    }

    private fun getData(){
        for(i in titleList.indices){
            val dataClass = SettingsDataClass(titleList[i])
            dataList.add(dataClass)
        }
        recyclerView.adapter = SettingsAdapter(dataList)


    }




}