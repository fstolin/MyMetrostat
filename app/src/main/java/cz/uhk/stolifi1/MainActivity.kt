package cz.uhk.stolifi1

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cz.uhk.stolifi1.database.JourneyDAO
import cz.uhk.stolifi1.database.MetroStationApp
import cz.uhk.stolifi1.database.MetroStationDAO
import cz.uhk.stolifi1.database.MetroStationEntity
import cz.uhk.stolifi1.databinding.ActivityMainBinding
import cz.uhk.stolifi1.journey.JourneyActivity
import cz.uhk.stolifi1.stations.Stations
import cz.uhk.stolifi1.stations.Stop
import cz.uhk.stolifi1.utils.APIInterface
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log
import kotlin.text.StringBuilder

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var metroStationList: MutableSet<Stop> = mutableSetOf()
    private var stationData: Stations? = null
    private lateinit var metroStationDAO: MetroStationDAO
    private lateinit var journeyDAO: JourneyDAO

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
        // Main image easter egg / station data
        binding?.myMainImage?.setOnClickListener{ createStationsData() }
        // Metro Station DAO
        metroStationDAO =  (application as MetroStationApp).db.metroStationDao()
        journeyDAO = (application as MetroStationApp).db.journeysDao()

        // TODO only download JSON & update databse when neccesary - once a week - or when requested maybe when the file was changed
        // JSON request
        // TODO async
        getJSONData()


        // Hide buttons (code to remember)
        //binding?.statsButton?.visibility = View.INVISIBLE
    }

    private fun createStationsData() {
        // Prevent null issues
        if (stationData == null) {
            binding?.failTextView?.visibility = View.VISIBLE
            return
        }

        // List to check added stations
        val filledList = mutableListOf<String>()
        // Fill in stops - only use Metro stops
        for (stopGroup in stationData!!.stopGroups) {
            // Metro is only in Prague
            if (stopGroup.districtCode != "AB") continue

            val stops : List<Stop>? = stopGroup.stops ?: continue

            for (stop in stops!!) {
                // Check whether the stop is for metro & whether it already is in the added stations
                if (stop.isMetro == true && !filledList.contains(stop.altIdosName ?: "null name")) {
                    metroStationList.add(stop)
                    filledList.add(stop.altIdosName ?: "null name")
                }
            }
        }
        Toast.makeText(this@MainActivity, "station data created", Toast.LENGTH_SHORT).show()
        printStations()
        fillDBWithStops(metroStationList)
    }

    private fun printStations(){
        val br = StringBuilder()
        for (station in metroStationList) {
            br.append(station.altIdosName)
            br.append(", ")
        }
        Log.i(TAG, br.toString())
    }

    private fun startButton() {
        val intent = Intent(this, JourneyActivity::class.java)
        startActivity(intent)
    }

    private fun statsButton() {
        Toast.makeText(this@MainActivity, "Your statistics", Toast.LENGTH_SHORT).show()
    }

    private fun fillDBWithStops(stops: MutableSet<Stop>) {
        var existingMSEs = listOf<MetroStationEntity>()
        // Get existing Stations
        lifecycleScope.launch {
            metroStationDAO.fetchAllMetroStations().collect() {
                existingMSEs = it
            }
        }
        Log.i(TAG, existingMSEs.toString())
        // Add each station
        for (stop in stops) {
            var isDuplicate = false
            val name = stop.altIdosName ?: ""
            val line = ""
            val lat = stop.lat ?: 0.0
            val lon = stop.lon ?: 0.0

            // Check whether stop is a duplicate
            for (eMse in existingMSEs) {
                if (eMse.name == name) {
                    Log.i(TAG, name)
                    isDuplicate = true
                    break
                }
            }
            // If it isn't already entered
            if (!isDuplicate) {
                lifecycleScope.launch {
                    metroStationDAO.insert(
                        MetroStationEntity(
                            name = name,
                            line = line,
                            lat = lat,
                            lon = lon
                        )
                    )
                }
            }
        }
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
                Toast.makeText(this@MainActivity, "successfully downloaded station data", Toast.LENGTH_SHORT).show()
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