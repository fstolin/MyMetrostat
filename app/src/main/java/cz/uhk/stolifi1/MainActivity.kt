package cz.uhk.stolifi1

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cz.uhk.stolifi1.databinding.ActivityMainBinding
import cz.uhk.stolifi1.journey.JourneyActivity
import cz.uhk.stolifi1.stations.Stations
import cz.uhk.stolifi1.stations.Stop
import cz.uhk.stolifi1.utils.APIInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private lateinit var stops: MutableSet<Stop>
    private var stationData: Stations? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Start button listener
        binding?.startButton?.setOnClickListener { startButton() }
        // Stats button listener
        binding?.statsButton?.setOnClickListener{ statsButton() }

        getJSONData()

        // Hide buttons (code to remember)
        //binding?.statsButton?.visibility = View.INVISIBLE
    }

    private fun startButton() {
        val intent = Intent(this, JourneyActivity::class.java)
        startActivity(intent)
    }

    private fun statsButton() {
        Toast.makeText(this@MainActivity, "Your statistics", Toast.LENGTH_SHORT).show()
    }

    private fun getJSONData() {
        // Retrofit builder
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://data.pid.cz/")
            .build()

        // Connect to our api interface
        val apiInterface = retrofitBuilder.create(APIInterface::class.java)
        val endpointURL = "stops/json/stops.json"

        // Retrofit data
        val retrofitData = apiInterface.getData(endpointURL)

        retrofitData.enqueue(object : Callback<Stations?> {
            override fun onResponse(call: Call<Stations?>, response: Response<Stations?>) {
                binding?.failTextView?.visibility = View.INVISIBLE
                stationData = response.body()
            }
            override fun onFailure(call: Call<Stations?>, t: Throwable) {
                binding?.failTextView?.visibility = View.VISIBLE
            }
        })
    }

    // Unassign view binding
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


}