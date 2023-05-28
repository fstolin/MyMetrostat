package cz.uhk.stolifi1

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cz.uhk.stolifi1.database.MetroStationApp
import cz.uhk.stolifi1.database.MetroStationDAO
import cz.uhk.stolifi1.database.MetroStationEntity
import cz.uhk.stolifi1.databinding.ActivityMainBinding
import cz.uhk.stolifi1.journey.JourneyActivity
import cz.uhk.stolifi1.stations.Stations
import cz.uhk.stolifi1.stations.Stop
import cz.uhk.stolifi1.utils.APIInterface
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
        var filledList = mutableListOf<String>()
        // Fill in stops - only use Metro stops
        for (stopGroup in stationData!!.stopGroups) {
            // Metro is only in Prague
            if (stopGroup.districtCode != "AB") continue

            var stops : List<Stop>? = stopGroup.stops ?: continue

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
        var br = StringBuilder()
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

    private fun fillDBWithStops(stops: MutableSet<Stop>){
        for (stop in stops) {
            var name = stop.altIdosName ?: ""
            var line = ""
            var lat = stop.lat ?: 0.0
            var lon = stop.lon ?: 0.0
            lifecycleScope.launch {
                metroStationDAO.insert(MetroStationEntity(name=name, line=line, lat=lat, lon=lon))
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