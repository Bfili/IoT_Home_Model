package com.example.iothomemodel.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.iothomemodel.MainActivity
import com.example.iothomemodel.R
import com.example.iothomemodel.databinding.FragmentDoorControlBinding
import java.util.*

class DoorControlFragment : Fragment() {

    private var sharedPreferences : SharedPreferences? = null

    private var isDoorOpenFlag = false
    private val handler: Handler = Handler()
    private val sampleTime: Int = 100
    private var requestTimerTask: TimerTask? = null
    private var requestTimer: Timer? = null
    private var requestTimerFirstRequestAfterStop = false

    private lateinit var queue : RequestQueue

    private lateinit var binding : FragmentDoorControlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Kontrola drzwi"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_door_control, container, false)

        sharedPreferences = activity?.getSharedPreferences("stored_preferences", Context.MODE_PRIVATE)

        queue = Volley.newRequestQueue(activity)

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
    private fun getDataFromURL(squareIndicator: TextView, textIndicator: TextView) {
        val IP = sharedPreferences?.getString("IP_ADRESS", "")
        val port = sharedPreferences?.getString("NET_PORT", "")
        val url = "http://$IP:$port/isDoorOpen"
        //val url = "http://192.168.1.7:80/isDoorOpen"
        if(queue == null){
             queue = Volley.newRequestQueue(activity)
        }

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                extractDataFromResponse(response, squareIndicator, textIndicator)
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
        squareIndicator: TextView,
        textIndicator: TextView
    ){
        val isDoorOpen = response.toInt()
        //Toast.makeText(activity, isDoorOpen.toString(), Toast.LENGTH_SHORT).show()
        isDoorOpenFlag = if(isDoorOpen == 0){
            false
        } else isDoorOpen == 1

        if(isDoorOpenFlag){
            squareIndicator.background = ColorDrawable(Color.GREEN)
            textIndicator.text = "Drzwi są otwarte"
        }
        else if(!isDoorOpenFlag){
            squareIndicator.background = ColorDrawable(Color.RED)
            textIndicator.text = "Drzwi są zamknięte"
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
        val squareIndicator = binding.squareLayout
        val textIndicator = binding.textView
        requestTimerTask = object : TimerTask() {
            override fun run() {
                handler.post { getDataFromURL(squareIndicator, textIndicator) }
            }
        }
    }
}