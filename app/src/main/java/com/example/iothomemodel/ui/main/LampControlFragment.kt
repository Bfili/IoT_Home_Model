package com.example.iothomemodel.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.iothomemodel.MainActivity
import com.example.iothomemodel.R
import com.example.iothomemodel.databinding.FragmentLampControlBinding
import java.util.*

class LampControlFragment : Fragment() {

    private var sharedPreferences : SharedPreferences? = null

    private var lampState = ""
    private var stateNumberAssign = 2
    private val handler: Handler = Handler()
    private val sampleTime: Int = 100
    private var requestTimerTask: TimerTask? = null
    private var requestTimer: Timer? = null
    private var requestTimerFirstRequestAfterStop = false

    private lateinit var queue : RequestQueue

    private lateinit var binding : FragmentLampControlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Oświetlenie korytarza"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_lamp_control, container, false)

        sharedPreferences = activity?.getSharedPreferences("stored_preferences", Context.MODE_PRIVATE)

        queue = Volley.newRequestQueue(activity)

        val lightBulbImg = binding.lightBulbPng
        val textIndicator = binding.corridorLampControlLampStateText

        getDataFromURL(lightBulbImg, textIndicator)

        lightBulbImg.setOnClickListener{
            when (lampState) {
                "on" -> {
                    stateNumberAssign = 0
                }
                "off" -> {
                    stateNumberAssign = 1
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        startRequestTimer()
    }

    override fun onPause() {
        super.onPause()
        stopRequestTimerTask()
    }

    @SuppressLint("SetTextI18n")
    private fun getDataFromURL(lightBulbImg: ImageView, textIndicator: TextView) {
        val IP = sharedPreferences?.getString("IP_ADRESS", "")
        val port = sharedPreferences?.getString("NET_PORT", "")
        val url = "http://$IP:$port/lampBlink?state=$stateNumberAssign"
        //val url = "http://192.168.1.7:80/lampBlink?state=$stateNumberAssign"
        if(queue == null){
            queue = Volley.newRequestQueue(activity)
        }

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response -> //lampState = response
                extractDataFromResponse(response, lightBulbImg, textIndicator)
                //Toast.makeText(activity, response.toString() , Toast.LENGTH_SHORT).show()
            },
            {
                //Toast.makeText(activity, error.toString() , Toast.LENGTH_SHORT).show()
            })

        queue.add(stringRequest)
    }

    @SuppressLint("SetTextI18n")
    private fun extractDataFromResponse(
        response: String,
        lightBulbImg: ImageView,
        textIndicator: TextView
    ){
        lampState = response
        when (lampState) {
            "on" -> {
                lightBulbImg.setImageResource(R.drawable.bulb_on)
                textIndicator.text = "Lampa jest zapalona"
            }
            "off" -> {
                lightBulbImg.setImageResource(R.drawable.bulb_off)
                textIndicator.text = "Lampa jest zgaszona"
            }
            "corr" -> {
                lightBulbImg.setImageResource(R.drawable.bulb_on)
                textIndicator.text = "Ktoś jest w korytarzu!"
                stateNumberAssign = 2
            }
        }
    }

    private fun startRequestTimer() {
        if (requestTimer == null) {
            // set a new Timer
            requestTimer = Timer()

            // initialize the TimerTask's job
            initializeRequestTimerTask()
            requestTimer!!.schedule(requestTimerTask, 0, sampleTime.toLong())

            // clear error message
            //Toast.makeText(activity, "Starting Timer", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRequestTimerTask() {
        // stop the timer, if it's not already null
        if (requestTimer != null) {
            requestTimer!!.cancel()
            requestTimer = null
            requestTimerFirstRequestAfterStop = true
            //Toast.makeText(activity, "Stopping Timer", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeRequestTimerTask() {
        val lightBulbImg = binding.lightBulbPng
        val textIndicator = binding.corridorLampControlLampStateText
        requestTimerTask = object : TimerTask() {
            override fun run() {
                handler.post { getDataFromURL(lightBulbImg, textIndicator) }
            }
        }
    }
}