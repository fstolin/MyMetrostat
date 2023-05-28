package cz.uhk.stolifi1

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import cz.uhk.stolifi1.databinding.ActivityJourneyBinding
import cz.uhk.stolifi1.databinding.ActivityMainBinding

class JourneyActivity : AppCompatActivity() {

    private var binding: ActivityJourneyBinding? = null
    // Location fused client
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    // Location variables
    private var userLat: Double = 0.0
    private var userLon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey)

        // Location
        requestUserLocationData()

        // View-binding
        binding = ActivityJourneyBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Buttons
        binding?.gpsNow?.setOnClickListener{ gpsNowButton() }
    }

    // gpsNowButton
    private fun gpsNowButton(){
        Toast.makeText(this@JourneyActivity, "Yo yo, journey activity", Toast.LENGTH_SHORT).show()
        binding?.testText?.text = "Lat: ${userLat}; Lon: ${userLon}"
    }

    // Location callback
    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            userLat = mLastLocation.latitude
            userLon = mLastLocation.longitude
        }
    }

    // Request user location
    @SuppressLint("MissingPermission")
    private fun requestUserLocationData(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Builder of location request
        var locationRequestBuilder = LocationRequest.Builder(android.location.LocationRequest.QUALITY_HIGH_ACCURACY,1000)
        locationRequestBuilder.setMaxUpdates(1)
        // Location request
        var locationRequest: LocationRequest = locationRequestBuilder.build()

        // Needs permissions -> will already be checked after clicking on start journey
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
    }

    // Unassign view binding
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}