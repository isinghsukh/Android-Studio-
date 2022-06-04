package com.example.shakeplay

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.ToneGenerator.TONE_SUP_RINGTONE
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.util.*
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {


    // Declaring sensorManager
    // and acceleration constants
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    //Tone generator:
    private val myTone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private var stopPressed = false

    //ViewModel:
    private lateinit var viewModel: CountViewModel

    //textview:
    val valueText: TextView by lazy { findViewById(R.id.ValueText) }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Getting the Sensor Manager instance
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        //ViewModel:
        viewModel = ViewModelProvider(this).get(CountViewModel::class.java)


        //Button Logic:
        val stopButton: Button = findViewById(R.id.StopButton)
        stopButton.setOnClickListener{
            viewModel.limitReached = false
            viewModel.shakeCount = 0
            valueText.text = "" + viewModel.shakeCount
            myTone.stopTone()
        }
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            // Fetching x,y,z values
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration

            // Getting current accelerations
            // with the help of fetched x,y,z values
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            // Display a Toast message if
            // acceleration value is over 12

            if (acceleration > 12) {
                //Toast.makeText(applicationContext, "Shake event detected: $acceleration", Toast.LENGTH_SHORT).show()
                viewModel.shakeCount++
                valueText.text = "" + viewModel.shakeCount
            }

            //check whether shakeCount is equal to 10:
            if (viewModel.shakeCount >= 10) {
//                val msg = "Reached Limit!!"
//                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                playTone()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
        valueText.text = "" + viewModel.shakeCount
//        if(viewModel.shakeCount == 0) {
//            myTone.stopTone()
//            myTone.release()
//        }
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    private fun playTone() {
        myTone.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE)
    }

    companion object {
        private const val TAG = "Shake&Play"
    }
}
