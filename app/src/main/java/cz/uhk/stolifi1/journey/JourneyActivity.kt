package cz.uhk.stolifi1.journey

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
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

class JourneyActivity : AppCompatActivity() {

    private var binding: ActivityJourneyBinding? = null
    // Location variables
    private var userLat: Double = 0.0
    private var userLon: Double = 0.0

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
        Log.i(TAG, "###### $stationlist")

        // Adapter
        adapter = StationAdapter(stationlist)
        Log.i(TAG, " ##### adamter list: ${adapter.stationList.toString()} number: ${adapter.itemCount}")
        recyclerView.adapter = adapter
        Log.i(TAG, "###### $adapter")
    }

    private fun finishJourney() {
        TODO("Not yet implemented")
    }

    // gpsNowButton
    private fun gpsNowButton(){
        // Permissions
        PermissionUtils.handlePermissions(this@JourneyActivity)
        // Location
        requestUserLocationData()
    }

    // Location callback
    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            userLat = mLastLocation.latitude
            userLon = mLastLocation.longitude
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

    private fun addStationData() {
        stationlist.add(ListStation("Vysočanská", 2, 435.0, "B"))
        stationlist.add(ListStation("Vltavská", 3, 1785.0, "C"))
        stationlist.add(ListStation("Muzeum", 4, 85785.0, "AC"))
    }

}