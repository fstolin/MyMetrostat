package cz.uhk.stolifi1

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import cz.uhk.stolifi1.database.JourneyDAO
import cz.uhk.stolifi1.database.MetroStationApp
import cz.uhk.stolifi1.database.MetroStationDAO
import cz.uhk.stolifi1.database.MetroStationEntity
import cz.uhk.stolifi1.databinding.ActivityMainBinding
import cz.uhk.stolifi1.journey.JourneyActivity
import cz.uhk.stolifi1.miscActivities.StatsActivity
import cz.uhk.stolifi1.stations.Stations
import cz.uhk.stolifi1.stations.Stop
import cz.uhk.stolifi1.utils.APIInterface
import cz.uhk.stolifi1.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.text.StringBuilder

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var metroStationList: MutableSet<Stop> = mutableSetOf()
    private var stationData: Stations? = null
    private lateinit var metroStationDAO: MetroStationDAO
    private lateinit var journeyDAO: JourneyDAO
    private lateinit var snackView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        // View for Snackbars etc
        snackView = findViewById(R.id.mainPageLinearLayout)
        Utils.mainView = snackView

        // Start button listener
        binding?.startButton?.setOnClickListener { startButton() }
        // Stats button listener
        binding?.statsButton?.setOnClickListener{ statsButton() }
        // Main image easter egg / station data
        binding?.myMainImage?.setOnClickListener{ createStationsData() }
        // Metro Station DAO
        metroStationDAO =  (application as MetroStationApp).db.metroStationDao()
        journeyDAO = (application as MetroStationApp).db.journeysDao()

        // Only run on the first start
        if (Utils.firstStart) {
            // JSON async request
            getJSONDataAsync()
            Utils.firstStart = false
        }
    }

    private fun getJSONDataAsync() = runBlocking{
        val job = launch {
            getJSONData()
        }
        job.join()
        createStationsData()
        //Log.i(TAG, "My metro stations: $metroStationList")
    }

    // Creates station data from the JSON to be saved in the database
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
        val intent = Intent(this, StatsActivity::class.java)
        startActivity(intent)
    }

    // Suspended function to use the DAO
    private suspend fun getCurrentStations(): List<MetroStationEntity> {
        return metroStationDAO.fetchAllMetroStations().first()
    }

    private fun getDBtest() = runBlocking {
        var existingMSEs = listOf<MetroStationEntity>()
        // Get existing Stations
        existingMSEs = getCurrentStations()
        Log.i(TAG, "DATABASE: ${existingMSEs.toString()}")
    }

    // Run blocking due to coRoutine usage, fills database with stop data
    private fun fillDBWithStops(stops: MutableSet<Stop>) = runBlocking {
        var existingMSEs = listOf<MetroStationEntity>()
        // Get existing Stations
        existingMSEs = getCurrentStations()

        Log.i(TAG, "Database check: $existingMSEs")
        // Add each station
        for (stop in stops) {
            var isDuplicate = false
            val name = stop.altIdosName ?: ""
            val line = getLines(stop)
            val lat = stop.lat ?: 0.0
            val lon = stop.lon ?: 0.0

            // Check whether stop is a duplicate
            for (eMse in existingMSEs) {
                if (eMse.name == name) {
                    isDuplicate = true
                    break
                }
            }

            // If it isn't already entered
            if (!isDuplicate) {
                // Launch coroutine for adding to db
                launch {
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

    // Returns the lines in the correct String format
    private fun getLines(stop: Stop): String {
        // Hardcoded lines for transfer stations due to their low amount and a lack of time
        if (stop.altIdosName == "Muzeum") return "CA"
        if (stop.altIdosName == "Můstek") return "AB"
        if (stop.altIdosName == "Florenc") return "CB"
        if (stop.altIdosName == "Zličín") return "B"

        // Other stations
        var sb = StringBuilder()
        if (stop.lines == null) return ""
        for (line in stop.lines) {
            sb.append(line.name)
        }
        return sb.toString()
    }

   suspend private fun getJSONData() {
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