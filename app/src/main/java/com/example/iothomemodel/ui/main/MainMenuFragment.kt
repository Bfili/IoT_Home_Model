package com.example.iothomemodel.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.iothomemodel.MainActivity
import com.example.iothomemodel.R
import com.example.iothomemodel.databinding.FragmentMainMenuBinding

class MainMenuFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<FragmentMainMenuBinding>(inflater,
            R.layout.fragment_main_menu, container, false)
        binding.temperatureControlMenuButton.setOnClickListener { view : View -> view.findNavController().navigate(R.id.action_mainMenuFragment_to_temperatureControlFragment) }
        binding.corridorLampControlMenuButton.setOnClickListener { view : View -> view.findNavController().navigate(R.id.action_mainMenuFragment_to_lampControlFragment) }
        binding.doorsControlMenuButton.setOnClickListener { view : View -> view.findNavController().navigate(R.id.action_mainMenuFragment_to_doorControlFragment) }
        binding.changeIpMenuButton.setOnClickListener { view : View -> view.findNavController().navigate(R.id.action_mainMenuFragment_to_networkConfigurationFragment) }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).supportActionBar?.title = ""
    }
}