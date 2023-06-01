package cz.uhk.stolifi1.miscActivities

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import cz.uhk.stolifi1.R
import cz.uhk.stolifi1.database.JourneyDAO
import cz.uhk.stolifi1.database.JourneysEntity
import cz.uhk.stolifi1.database.MetroStationApp
import cz.uhk.stolifi1.database.MetroStationDAO
import cz.uhk.stolifi1.databinding.ActivityStatsBinding
import cz.uhk.stolifi1.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class StatsActivity : AppCompatActivity() {

    private var binding: ActivityStatsBinding? = null
    // Database
    private lateinit var metroStationDAO: MetroStationDAO
    private lateinit var journeyDAO: JourneyDAO
    // Snackbar
    private lateinit var snackView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // View-binding
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        // Snackbar
        snackView = findViewById(R.id.statsMainView)

        // Db - DAOs
        metroStationDAO = (application as MetroStationApp).db.metroStationDao()
        journeyDAO = (application as MetroStationApp).db.journeysDao()

        // Buttons
        binding?.understoodStatsButton?.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        // Fill the statistics
        binding?.statsMainView?.visibility = View.VISIBLE
        fillStatistics()
    }


    private fun fillStatistics() = runBlocking {
        var journeyList: List<JourneysEntity> = listOf()
        // get the complete journey list from DB
        val dbJob = launch {
            journeyList = journeyDAO.fetchAllJourneys().first()
        }
        dbJob.join()

        // Initialize stats variables - lists are prepopulated to prevent if only 2 top stations are present
        var totalDistance: Double = 0.0
        var totalCO2saved: Double = 0.0
        var topStations: ArrayList<PopularStation> = arrayListOf(
            PopularStation("Želivského", 0),
            PopularStation("Zličín", 0),
            PopularStation("Černý most", 0)
        )
        var topLines: ArrayList<PopularLine> = arrayListOf(PopularLine('A',0))

        // Check if it was successful
        if(journeyList.isEmpty()) {
            handleEmptyStatistics()
        } else {
            // Loop through journeys
            for (journey in journeyList) {
                // Calculate total distance
                totalDistance += journey.distance
                // Calculate total CO2 saved
                totalCO2saved += journey.co2saved
                // Calculate top 3 stations
                // Arrivals
                var arrivalStation: PopularStation? = getFromlist(topStations, journey.arriveStationName)
                if (arrivalStation != null) {
                    arrivalStation.count += 1
                } else {
                    topStations.add(PopularStation(journey.arriveStationName, 1))
                }
                // Departures
                var departureStation: PopularStation? = getFromlist(topStations, journey.departStationName)
                if (departureStation != null) {
                    departureStation.count += 1
                } else {
                    topStations.add(PopularStation(journey.departStationName, 1))
                }
                // Calculate top line
                // Arrivals
                for (char in journey.arriveStationLine) {
                    var line: PopularLine? = getFromLinelist(topLines, char)
                    if (line != null) {
                        line.count += 1
                    } else {
                        topLines.add(PopularLine(char, 1))
                    }
                }
                // Departures
                for (char in journey.departStationLine) {
                    var line: PopularLine? = getFromLinelist(topLines, char)
                    if (line != null) {
                        line.count += 1
                    } else {
                        topLines.add(PopularLine(char, 1))
                    }
                }
            }

            // Stats finished in application -> now show in UI
            // Total journeys
            binding?.completeJourneysCountText?.text = "${journeyList.count()}"
            // Distance + CO
            totalDistance /= 1000
            binding?.completeDistanceText?.text = "${totalDistance.toInt().toString()} km"
            binding?.completeCO2Text?.text = "${totalCO2saved.toInt().toString()} kg"
            // Stations
            topStations = ArrayList (topStations.sortedBy { it.count })
            topStations.reverse()
            Log.i(TAG, topStations.toString())
            binding?.top1Name?.text = topStations[0].name
            binding?.top1Visits?.text= topStations[0].count.toString()
            binding?.top2Name?.text = topStations[1].name
            binding?.top2Visits?.text= topStations[1].count.toString()
            binding?.top3Name?.text = topStations[2].name
            binding?.top3Visits?.text= topStations[2].count.toString()
            // Line
            topLines = ArrayList(topLines.sortedBy { it.count })
            topLines.reverse()
            var topLine = topLines[0]
            binding?.topLineImageView?.setImageResource(Utils.getLineDrawable(topLine.name))
        }
    }

    private fun handleEmptyStatistics() {
        binding?.statsMainView?.visibility = View.INVISIBLE
        Utils.showDSnack("You do not have any journeys yet!", Utils.mainView ?: snackView)
        onBackPressedDispatcher.onBackPressed()
    }

    private fun getFromlist(list: ArrayList<PopularStation>, str: String): PopularStation? {
        for (unit in list) {
            if (unit.name == str) return unit
        }
        return null
    }

    private fun getFromLinelist(list: ArrayList<PopularLine>, char: Char): PopularLine? {
        for (unit in list) {
            if (unit.name == char) return unit
        }
        return null
    }

    data class PopularStation(var name: String, var count: Int)
    data class PopularLine(var name: Char, var count: Int)
}