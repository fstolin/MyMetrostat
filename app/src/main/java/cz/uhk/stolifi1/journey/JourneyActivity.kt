package cz.uhk.stolifi1.journey

import android.annotation.SuppressLint
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
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
    private var selectFrom = false
    private var selectTo = false
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey)

        // View-binding
        binding = ActivityJourneyBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Snackbar view
        snackView = findViewById(R.id.journeyActivityMainView)

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

        // Clicking on the start station list
        startStationSearchView.setOnClickListener {
            requestUserLocationData()
        }

        startStationSearchView.setOnQueryTextFocusChangeListener{_, _ ->
            requestUserLocationData()
        }

        // Start station text filtering the list
        startStationSearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                selectFrom = true
                return true
            }

        })
    }

    private fun finishJourney() {
        TODO("Not yet implemented")
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
        var stationLoc: Location = Location("station location")
        stationLoc.latitude = station.lat
        stationLoc.longitude = station.lon

        var userLoc: Location = Location("user location")
        userLoc.latitude = userLat
        userLoc.longitude = userLon

        return stationLoc.distanceTo(userLoc).toDouble()
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
                if (station.name.lowercase(Locale.ROOT).contains(newText)){
                    filteredList.add(station)
                }
            }

            // Empty result
            if (!filteredList.isEmpty()) {
                filteredList = ArrayList(filteredList.sortedBy { it.distance })
                adapter.updateStationList(filteredList)
            }
        }
    }

    // on stationList item click -> select it
    override fun onItemClick(position: Int) {
        val clickedItem : ListStation = adapter.stationList[position]
        Utils.showDSnack("#### Item $position name: ${clickedItem.name}", snackView)
    }

}