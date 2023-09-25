package com.example.iothomemodel.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.iothomemodel.MainActivity
import com.example.iothomemodel.R
import com.example.iothomemodel.databinding.FragmentTemperatureControlBinding
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class TemperatureControlFragment : Fragment() {

    private var sharedPreferences : SharedPreferences? = null

    private val handler: Handler = Handler()
    private val sampleTime: Int = 100
    private var requestTimerTask: TimerTask? = null
    private var requestTimer: Timer? = null
    private var requestTimerFirstRequestAfterStop = false

    private lateinit var dataGraph: GraphView
    private lateinit var series: LineGraphSeries<DataPoint>
    private var xAxisValue = 0

    private lateinit var queue : RequestQueue

    private lateinit var binding : FragmentTemperatureControlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Kontrola temperatury"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentTemperatureControlBinding>(inflater,
        R.layout.fragment_temperature_control, container, false)

        sharedPreferences = activity?.getSharedPreferences("stored_preferences", Context.MODE_PRIVATE)

        queue = Volley.newRequestQueue(activity)

        dataGraph = binding.datagraph

        dataGraph.viewport.isXAxisBoundsManual = true
        dataGraph.viewport.setMinX(0.0)
        dataGraph.viewport.setMaxX(200.0)

        dataGraph.gridLabelRenderer.textSize = 20F
        dataGraph.gridLabelRenderer.horizontalAxisTitle = "Czas [s]"
        dataGraph.gridLabelRenderer.verticalAxisTitle = "Temperatura [°C]"
        dataGraph.gridLabelRenderer.verticalAxisTitleColor = Color.RED
        dataGraph.gridLabelRenderer.verticalAxisTitleTextSize = 30F
        dataGraph.gridLabelRenderer.horizontalAxisTitleTextSize = 30F

        series = LineGraphSeries(arrayOf<DataPoint>())
        series.color = Color.RED

        dataGraph.addSeries(series)

        val buttonGraphEnable = binding.graphEnable
        buttonGraphEnable.setOnClickListener {
            startRequestTimer()
        }


        val editTextTemperature = binding.editTemperatureText
        editTextTemperature.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val newSetpoint = p0.toString()
                getDataFromURL("changeSetpoint?temperature=$newSetpoint")
            }
        })

        val buttonTemperatureIncrement = binding.incrementTemperatureButton
        buttonTemperatureIncrement.setOnClickListener {
            var newSetpoint = editTextTemperature.text.toString().toDouble()
            newSetpoint += 0.5
            editTextTemperature.text = Editable.Factory.getInstance().newEditable(newSetpoint.toString())
            getDataFromURL("changeSetpoint?temperature=$newSetpoint")
        }

        val buttonTemperatureDecrement = binding.decrementTemperatureButton
        buttonTemperatureDecrement.setOnClickListener {
            var newSetpoint = editTextTemperature.text.toString().toDouble()
            newSetpoint -= 0.5
            editTextTemperature.text = Editable.Factory.getInstance().newEditable(newSetpoint.toString())
            getDataFromURL("changeSetpoint?temperature=$newSetpoint")
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        stopRequestTimerTask()
    }


    @SuppressLint("SetTextI18n")
    private fun getDataFromURL(httpRequestType : String) {
        val IP = sharedPreferences?.getString("IP_ADRESS", "")
        val port = sharedPreferences?.getString("NET_PORT", "")
        val url = "http://$IP:$port/$httpRequestType"
        //val url = "http://192.168.1.7:80/isDoorOpen"
        if(queue == null){
            queue = Volley.newRequestQueue(activity)
        }

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                extractDataFromResponse(response, httpRequestType)
                //Toast.makeText(activity, response.toString() , Toast.LENGTH_SHORT).show()
            },
            {
                //Toast.makeText(activity, error.toString() , Toast.LENGTH_SHORT).show()
            })

        queue.add(stringRequest)
    }

    private fun roundToTwoDecimalPlaces(value: Double): Double {
        val decimal = BigDecimal(value)
        return decimal.setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    @SuppressLint("SetTextI18n")
    private fun extractDataFromResponse(
        response: String,
        httpRequestType: String,
    ){
        if(httpRequestType == "getTemperature"){
            var currentTemperature = response.toDouble()
            currentTemperature = roundToTwoDecimalPlaces(currentTemperature)
            series.appendData(DataPoint(xAxisValue.toDouble(), currentTemperature), true, 201)
            xAxisValue++
        }
    }

    private fun startRequestTimer() {
        if (requestTimer == null) {
            // set a new Timer
            requestTimer = Timer()

            // initialize the TimerTask's job
            initializeRequestTimerTask()
            requestTimer!!.schedule(requestTimerTask, 0, sampleTime.toLong())
        }
    }

    private fun stopRequestTimerTask() {
        // stop the timer, if it's not already null
        if (requestTimer != null) {
            requestTimer!!.cancel()
            requestTimer = null
            requestTimerFirstRequestAfterStop = true
        }
    }

    private fun initializeRequestTimerTask() {
        requestTimerTask = object : TimerTask() {
            override fun run() {
                handler.post { getDataFromURL("getTemperature") }
            }
        }
    }
}