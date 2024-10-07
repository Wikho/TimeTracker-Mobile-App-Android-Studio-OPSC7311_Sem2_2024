package com.example.opsc7311_sem2_2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.opsc7311_sem2_2024.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(), SettingsAdapter.SettingsListener {

    // Declare the binding variable
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataListSF: ArrayList<SettingsDataClass>
    lateinit var titleList: Array<String>

    private lateinit var sAdapater: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)





    }

    private fun setUpRecycle(){

        //sAdapater = SettingsDataClass()

    }


    override fun onSettingsListener(setting: SettingsDataClass){
        titleList = arrayOf(
            "First Setting",
            "Second Setting",
            "Third Setting",
            "Fourth Setting",
            "Fifth Setting"
        )

        binding.rvGeneralSettings.layoutManager = LinearLayoutManager(context)
        binding.rvGeneralSettings.adapter = sAdapater
        binding.rvGeneralSettings.setHasFixedSize(true)

        dataListSF = arrayListOf()
        getData()

    }



    private fun getData(){
        for(i in titleList.indices){
            val dataClass = SettingsDataClass(titleList[i])
            dataListSF.add(dataClass)
        }
        recyclerView.adapter = SettingsAdapter(listener = this)


    }




}