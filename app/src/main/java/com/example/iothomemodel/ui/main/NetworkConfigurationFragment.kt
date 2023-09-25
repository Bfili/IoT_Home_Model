package com.example.iothomemodel.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.iothomemodel.MainActivity
import com.example.iothomemodel.R
import com.example.iothomemodel.databinding.FragmentNetworkConfigurationBinding

class NetworkConfigurationFragment : Fragment() {

    private var sharedPreferences : SharedPreferences? = null
    private var editor : SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Ustawienia sieci"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentNetworkConfigurationBinding>(inflater,
        R.layout.fragment_network_configuration, container, false)

        sharedPreferences = activity?.getSharedPreferences("stored_preferences", Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit()

        val buttonConfirm = binding.buttonConfirm
        val editIP = binding.editIP
        val editPort = binding.editPort

        buttonConfirm.setOnClickListener {
            if(editIP.text.isNotEmpty()){
                //Common.DEFAULT_IP_ADDRESS = editIP.text.toString()
                editor?.putString("IP_ADRESS", editIP.text.toString())
                editor?.apply()
            }
            else{
                Toast.makeText(activity, "Nie podano adresu IP", Toast.LENGTH_LONG).show()
            }

            if(editPort.text.isNotEmpty()){
                //Common.DEFAULT_PORT = editPort.text.toString()
                editor?.putString("NET_PORT", editPort.text.toString())
                editor?.apply()
            }
            else{
                Toast.makeText(activity, "Nie podano portu", Toast.LENGTH_LONG).show()
            }
            if(editIP.text.isNotEmpty() and editPort.text.isNotEmpty())
            {
                view?.findNavController()?.navigate(R.id.action_networkConfigurationFragment_to_mainMenuFragment)
            }
        }

        return binding.root
    }
}