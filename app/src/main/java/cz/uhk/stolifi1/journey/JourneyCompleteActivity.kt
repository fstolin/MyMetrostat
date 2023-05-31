package cz.uhk.stolifi1.journey

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import cz.uhk.stolifi1.MainActivity
import cz.uhk.stolifi1.R
import cz.uhk.stolifi1.database.JourneyDAO
import cz.uhk.stolifi1.database.JourneysEntity
import cz.uhk.stolifi1.database.MetroStationApp
import cz.uhk.stolifi1.database.MetroStationDAO
import cz.uhk.stolifi1.databinding.ActivityJourneyBinding
import cz.uhk.stolifi1.databinding.ActivityJourneyCompleteBinding
import cz.uhk.stolifi1.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.round
import kotlin.math.roundToInt

// Station A, Station B, duration, distance, CO2 saved

class JourneyCompleteActivity : AppCompatActivity() {

    private var binding: ActivityJourneyCompleteBinding? = null
    // Database
    private lateinit var metroStationDAO: MetroStationDAO
    private lateinit var journeyDAO: JourneyDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey_complete)

        // View-binding
        binding = ActivityJourneyCompleteBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Db - DAOs
        metroStationDAO = (application as MetroStationApp).db.metroStationDao()
        journeyDAO = (application as MetroStationApp).db.journeysDao()

        // Buttons
        binding?.completeCoolButton?.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Fill in data
        fillJourneyCompleteData()
    }

    private fun fillJourneyCompleteData() = runBlocking{
        // Get data from database
        var jEntity: JourneysEntity? = null
        val dbJob = launch {
            jEntity = journeyDAO.fetchJourneyById(Utils.journeyId.toInt()).first()
        }
        dbJob.join()
        // Check whether it was succesfull
        if (jEntity == null) {
            Log.i(TAG, "Unable to load journey details from database - JourneyCompleteActivity.fillJourneyCompleteData()")
        } else {
            // Image dep station
            binding?.completeStartStationImage?.stationName?.text = jEntity!!.departStationName
            binding?.completeStartStationImage?.stationDistance?.visibility = View.INVISIBLE
            // Image arr station
            binding?.completeEndStationImage?.stationName?.text = jEntity!!.arriveStationName
            binding?.completeEndStationImage?.stationDistance?.visibility = View.INVISIBLE
            // Drawables
            assignDrawables(jEntity!!)
            // Distance
            var distance = jEntity!!.distance
            var sb = StringBuilder()
            if (distance > 1000) {
                // Rounding
                distance /= 10.0
                Log.i(TAG, distance.toString())
                distance = (distance.toInt()) / 100.0
                Log.i(TAG, distance.toString())
                sb.append(distance)
                sb.append(" km")
            } else {
                sb.append(distance.toInt())
                sb.append(" m")
            }
            binding?.completeDistanceText?.text = sb.toString()
            // CO2
            binding?.completeCO2Text?.text = "${((jEntity!!.co2saved * 100.0).toInt() / 100.0).toString()} kg"
        }
    }

    private fun assignDrawables(journeysEntity: JourneysEntity){
        var arrLineString = journeysEntity.arriveStationLine
        var depLineString = journeysEntity.departStationLine
        // Lines
        var arrLineList = Utils.getLineDrawablesTransfer(arrLineString)
        var depLineList = Utils.getLineDrawablesTransfer(depLineString)

        Log.i(TAG, "$arrLineList, $depLineList")

        // Arrival station
        binding?.completeStartStationImage?.lineIcon?.setImageResource(arrLineList[0])
        if (arrLineList.size > 1) {
            binding?.completeStartStationImage?.lineIcon2?.setImageResource(arrLineList[1])
            binding?.completeStartStationImage?.lineIcon2?.visibility = View.VISIBLE
        } else {
            binding?.completeStartStationImage?.lineIcon2?.visibility = View.INVISIBLE
        }

        // Departure station
        binding?.completeEndStationImage?.lineIcon?.setImageResource(depLineList[0])
        if (depLineList.size > 1) {
            binding?.completeEndStationImage?.lineIcon2?.setImageResource(depLineList[1])
            binding?.completeEndStationImage?.lineIcon2?.visibility = View.VISIBLE
        } else {
            binding?.completeEndStationImage?.lineIcon2?.visibility = View.INVISIBLE
        }

    }


}