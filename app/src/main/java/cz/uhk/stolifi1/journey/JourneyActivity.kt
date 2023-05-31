package cz.uhk.stolifi1.journey

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import cz.uhk.stolifi1.R
import cz.uhk.stolifi1.database.JourneyDAO
import cz.uhk.stolifi1.database.JourneysEntity
import cz.uhk.stolifi1.database.MetroStationApp
import cz.uhk.stolifi1.database.MetroStationDAO
import cz.uhk.stolifi1.database.MetroStationEntity
import cz.uhk.stolifi1.databinding.ActivityJourneyBinding
import cz.uhk.stolifi1.utils.ListStation
import cz.uhk.stolifi1.utils.PermissionUtils
import cz.uhk.stolifi1.utils.StationAdapter
import cz.uhk.stolifi1.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class JourneyActivity : AppCompatActivity(), StationAdapter.OnItemClickListener {

    private var binding: ActivityJourneyBinding? = null
    // Location variables
    private var userLat: Double = 0.0
    private var userLon: Double = 0.0
    // Workflow variables
    private var selectFrom = true
    private var selectTo = false
    private var alreadySelectedFrom = false
    private var alreadySelectedTo = false
    private var fromStationId = 0
    private var toStationId = 0

    // Location fused client
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    // Snackbar View
    private lateinit var snackView: View
    // Database
    private lateinit var metroStationDAO: MetroStationDAO
    private lateinit var journeyDAO: JourneyDAO
    // Station lists
    private lateinit var metroStations: List<MetroStationEntity>
    private lateinit var stationlist: ArrayList<ListStation>
    // Recycle view + Searchviews
    private lateinit var recyclerView: RecyclerView
    private lateinit var startStationSearchView: SearchView
    private lateinit var endStationSearchView: SearchView
    // Adapter
    private lateinit var adapter: StationAdapter
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey)

        // View-binding
        binding = ActivityJourneyBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Snackbar view
        snackView = findViewById(R.id.journeyActivityMainView)
        context = this

        // Recycler + search Views
        recyclerView = findViewById(R.id.stationRecycleView)
        startStationSearchView = findViewById(R.id.startSearchView)
        endStationSearchView = findViewById(R.id.endSearchView)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Handle permissions
        PermissionUtils.handlePermissions(this@JourneyActivity)
        // Location
        requestUserLocationData()

        // Db - DAOs
        metroStationDAO = (application as MetroStationApp).db.metroStationDao()
        journeyDAO = (application as MetroStationApp).db.journeysDao()

        // Hide unnecessary UI
        binding?.toStationLinearLayout?.visibility = View.GONE
        binding?.startStationImage?.root?.visibility = View.GONE
        binding?.endStationImage?.root?.visibility = View.GONE
        binding?.buttonLayout?.visibility = View.GONE

        // Buttons
        binding?.finishButton?.setOnClickListener {
            finishJourney()
        }

        // On create get database for the selectables
        getCurrentStations()

        // Add stationList data
        stationlist = arrayListOf()
        addStationData()

        // Adapter
        adapter = StationAdapter(stationlist, this)
        recyclerView.adapter = adapter

        // #### Start Station SEARCH VIEW ####
        // Clicking on the start station list
        startStationSearchView.setOnClickListener {
            startFromSearchAgain()
            requestUserLocationData()
        }

        // Checking for the location when entering the text field
        startStationSearchView.setOnQueryTextFocusChangeListener{_, _ ->
            if (!alreadySelectedFrom) hideToshowRecycle()
            selectFrom = true
            selectTo = false
            requestUserLocationData()
        }

        // Start station text filtering the list
        startStationSearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                startFromSearchAgain()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                startFromSearchAgain()
                filterList(newText)
                return true
            }

        })

        // #### End Station SEARCH VIEW ####
        // Clicking on the start station list
       endStationSearchView.setOnClickListener {
            toSearchStartAgain()
        }

        // Checking for the location when entering the text field
        endStationSearchView.setOnQueryTextFocusChangeListener{_, _ ->
            if (!alreadySelectedTo) binding?.stationRecycleView?.visibility = View.VISIBLE
            startStationSearchView.visibility = View.GONE
            selectTo = true
            selectFrom = false
        }

        // End station text filtering the list
        endStationSearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                toSearchStartAgain()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                toSearchStartAgain()
                filterList(newText)
                return true
            }

        })

        // Back button
        binding?.backButton?.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        // Try again button
        binding?.againButton?.setOnClickListener{
            val intent = Intent(this, JourneyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun calculateCO2(stationDistance: Double): Double {
        // CO2 per 1 km - average car [kg]
        var carCO2 = 2.845 / 10
        // Train CO2 per 1 km [kg]
        var trainCO2 = 0.37 / 10

        return ((carCO2 - trainCO2) * (stationDistance / 1000))
    }

    // Finished journey -> save all the data to the database
    private fun finishJourney() = runBlocking {
        var fromStation: MetroStationEntity? = null
        var endStation: MetroStationEntity? = null

        // Get the stations from db
        val fetchJob = launch {
            fromStation = metroStationDAO.fetchMetroStationById(fromStationId).first()
            endStation = metroStationDAO.fetchMetroStationById(toStationId).first()
        }
        fetchJob.join()

        // Check if successful
        if (fromStation == null || endStation == null) {
            Log.i(TAG, "##### accesing stations failed. $fromStationId to $toStationId")
        } else {

            // Update stations arrivals & departure counts in database
            fromStation!!.departureCount += 1
            endStation!!.arrivalCount += 1
            val updateJob = launch {
                metroStationDAO.update(fromStation!!)
                metroStationDAO.update(endStation!!)
            }
            updateJob.join()

            // Create new Journey object & populate it
            val stationDistance = calculateDistanceBetweenStations(fromStation!!, endStation!!)
            val journeyCO2 = calculateCO2(stationDistance)
            var journeyId: Long = -1

            val journey = JourneysEntity(   departStationName = fromStation!!.name, arriveStationName = endStation!!.name,
                                            distance = stationDistance, duration = 0, co2saved = journeyCO2,
                                            arriveStationLine = fromStation!!.line, departStationLine = endStation!!.line)
            val myJourneyJob = launch {
                journeyId = journeyDAO.insert(journey)
            }
            myJourneyJob.join()
            Utils.journeyId = journeyId

            // Show results screen
            val intent = Intent(context, JourneyCompleteActivity::class.java)
            startActivity(intent)
        }
    }


    // Location callback
    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            userLat = mLastLocation.latitude
            userLon = mLastLocation.longitude
            // Update distance data for stations
            updateDistanceData()
        }
    }

    // Request user location - we already have the permissions handled
    @SuppressLint("MissingPermission")
    private fun requestUserLocationData(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Builder of location request
        val locationRequestBuilder = LocationRequest.Builder(android.location.LocationRequest.QUALITY_HIGH_ACCURACY,1000)
        locationRequestBuilder.setMaxUpdates(10)
        // Location request
        val locationRequest: LocationRequest = locationRequestBuilder.build()

        // Needs permissions -> will already be checked after clicking on start journey
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
    }

    // Unassign view binding
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun getCurrentStations() = runBlocking {
        val job = launch {
            metroStations = metroStationDAO.fetchAllMetroStations().first()
        }
        job.join()
    }

    // Adds stations to recyclerView list with required data
    private fun addStationData() {
        for (station in metroStations) {
            var tempStation = ListStation(station.name, station.id, 0.0, station.line, station.lon, station.lat)
            tempStation.distance = calculateDistance(station)
            stationlist.add(tempStation)
        }
        stationlist = ArrayList(stationlist.sortedBy { it.distance })
    }

    // Calculates distance between the user and the station
    private fun calculateDistance(station: MetroStationEntity): Double {
        val stationLoc: Location = Location("station location")
        stationLoc.latitude = station.lat
        stationLoc.longitude = station.lon

        val userLoc: Location = Location("user location")
        userLoc.latitude = userLat
        userLoc.longitude = userLon

        return stationLoc.distanceTo(userLoc).toDouble()
    }

    private fun calculateDistanceBetweenStations(station: MetroStationEntity, endStation: MetroStationEntity): Double {
        val stationLoc: Location = Location("station location")
        stationLoc.latitude = station.lat
        stationLoc.longitude = station.lon

        val endStationLoc: Location = Location("end station location")
        endStationLoc.latitude = endStation.lat
        endStationLoc.longitude = endStation.lon

        return stationLoc.distanceTo(endStationLoc).toDouble()
    }

    // Calculates distance between the user and the station
    private fun calculateDistance(station: ListStation): Double {
        var stationLoc: Location = Location("station location")
        stationLoc.latitude = station.lat
        stationLoc.longitude = station.lon

        var userLoc: Location = Location("user location")
        userLoc.latitude = userLat
        userLoc.longitude = userLon

        return stationLoc.distanceTo(userLoc).toDouble()
    }

    // Updates distance data for all stations
    private fun updateDistanceData(){
        for (station in stationlist) {
            station.distance = calculateDistance(station)
        }
        stationlist = ArrayList(stationlist.sortedBy { it.distance })
        adapter.updateStationListDistanceOnly(stationlist)
    }

    // filters a list based on the new string. Sort by distance
    private fun filterList(newText: String?) {
        if (newText != null) {
            var filteredList = ArrayList<ListStation>()
            for (station in stationlist) {
                if (station.name.lowercase(Locale.ROOT).contains(newText.lowercase())){
                    filteredList.add(station)
                }
            }

            // Empty result

                filteredList = ArrayList(filteredList.sortedBy { it.distance })
                adapter.updateStationList(filteredList)

        }
    }

    // on stationList item click -> select it
    override fun onItemClick(position: Int) {
        // Selecting the first station
        if (selectFrom) {
            addItemStartStation(position)
        } else {
            addItemEndStation(position)
        }
    }

    private fun addItemStartStation(position: Int) {
        // Selecting clicked station, assigning its id
        val clickedItem: ListStation = adapter.stationList[position]
        fromStationId = clickedItem.dbId

        // Showing & editing UI
        binding?.startStationImage?.root?.visibility = View.VISIBLE
        binding?.startStationImage?.stationName?.text = clickedItem.name
        binding?.startStationImage?.stationDistance?.visibility = View.INVISIBLE

        // Lines
        var lineList = Utils.getLineDrawablesTransfer(clickedItem.line)
        Log.i(TAG, "####$lineList")
        binding?.startStationImage?.lineIcon?.setImageResource(lineList[0])
        if (lineList.size > 1) {
            binding?.startStationImage?.lineIcon2?.setImageResource(lineList[1])
            binding?.startStationImage?.lineIcon2?.visibility = View.VISIBLE
        } else {
            binding?.startStationImage?.lineIcon2?.visibility = View.INVISIBLE
        }

        // Hiding recyclerView & searchview
        //binding?.stationRecycleView?.visibility = View.INVISIBLE
        selectFrom = false
        alreadySelectedFrom = true

        // Showing the new UI
        binding?.toStationLinearLayout?.visibility = View.VISIBLE
        binding?.startSearchView?.visibility = View.GONE
        hideKeyboard(snackView)
        adapter.stationList = stationlist
    }

    private fun addItemEndStation(position: Int) {
        // Selecting clicked station, assigning its id
        val clickedItem: ListStation = adapter.stationList[position]
        toStationId = clickedItem.dbId

        // Showing & editing UI
        binding?.endStationImage?.root?.visibility = View.VISIBLE
        binding?.endStationImage?.stationName?.text = clickedItem.name
        binding?.endStationImage?.stationDistance?.visibility = View.INVISIBLE

        // Lines
        var lineList = Utils.getLineDrawablesTransfer(clickedItem.line)
        Log.i(TAG, "####$lineList")
        binding?.endStationImage?.lineIcon?.setImageResource(lineList[0])
        if (lineList.size > 1) {
            binding?.endStationImage?.lineIcon2?.setImageResource(lineList[1])
            binding?.endStationImage?.lineIcon2?.visibility = View.VISIBLE
        } else {
            binding?.endStationImage?.lineIcon2?.visibility = View.INVISIBLE
        }

        // Hiding recyclerView & searchview
        binding?.stationRecycleView?.visibility = View.GONE
        selectTo = false
        alreadySelectedTo = true

        // Showing the new UI
        binding?.buttonLayout?.visibility = View.VISIBLE
        binding?.endSearchView?.visibility = View.GONE
        hideKeyboard(snackView)
    }

    private fun hideToshowRecycle(){
        binding?.startStationImage?.root?.visibility = View.GONE
        binding?.toStationLinearLayout?.visibility = View.GONE
        binding?.stationRecycleView?.visibility = View.VISIBLE
    }

    // Start again the search from the start button
    private fun startFromSearchAgain() {
        hideToshowRecycle()
        selectFrom = true
        selectTo = false
    }

    // Start again the search to the final station
    private fun toSearchStartAgain(){
        selectFrom = false
        selectTo = true
        startStationSearchView.visibility = View.GONE
        binding?.stationRecycleView?.visibility = View.VISIBLE
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}