package cz.uhk.stolifi1

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import cz.uhk.stolifi1.databinding.ActivityJourneyBinding

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

        // Handle permissions
        handlePermissions(this@JourneyActivity)

        // View-binding
        binding = ActivityJourneyBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // Buttons
        binding?.gpsNow?.setOnClickListener{ gpsNowButton() }
    }

    // gpsNowButton
    private fun gpsNowButton(){
        // Permissions
        handlePermissions(this@JourneyActivity)
        // Location
        requestUserLocationData()
    }

    // Location callback
    private val mLocationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation!!
            userLat = mLastLocation.latitude
            userLon = mLastLocation.longitude
            binding?.testText?.text = "Lat: ${userLat}; Lon: ${userLon}"
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

    // Handles permissions using DEXTER library
    private fun handlePermissions(context: Context){
        Dexter.withContext(context).withPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(permReport: MultiplePermissionsReport?) {
                // If parameters are accepted
                if(permReport!!.areAllPermissionsGranted()) {
                    Toast.makeText(context, "Permissions ok", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                mutableList: MutableList<PermissionRequest>?,
                permToken: PermissionToken?
            ) {
                // Show rationale
                showRationaleDialog()
                // Return to main screen
                //onBackPressedDispatcher.onBackPressed()
            }

        }).onSameThread().check()
    }

    // Shows rationale for user, who didn't provide permissions
    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you declined location permissions. Location is needed to access metro stations. Visit application settings to edit the permissions.")
            .setPositiveButton("GO TO SETTINGS"){_, _ ->
                // Show application settings
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }

            }
            .setNegativeButton("CANCEL") {dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Unassign view binding
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}