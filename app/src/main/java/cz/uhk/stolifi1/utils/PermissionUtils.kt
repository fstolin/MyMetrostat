package cz.uhk.stolifi1.utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionUtils {

    companion object {

        // Handles permissions using DEXTER library
        fun handlePermissions(context: Context) {
            Dexter.withContext(context).withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(permReport: MultiplePermissionsReport?) {
                    // If parameters are accepted
                    if (permReport!!.areAllPermissionsGranted()) {

                    } else {
                        showRationaleDialog(context)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    mutableList: MutableList<PermissionRequest>?,
                    permToken: PermissionToken?
                ) {
                    // Show rationale
                    showRationaleDialog(context)
                    // Return to main screen
                    //onBackPressedDispatcher.onBackPressed()
                }

            }).onSameThread().check()
        }


        // Shows rationale for user, who didn't provide permissions
        private fun showRationaleDialog(context: Context) {
            AlertDialog.Builder(context)
                .setMessage("It looks like you declined location permissions. Location is needed to access metro stations. Visit application settings to edit the permissions.")
                .setPositiveButton("GO TO SETTINGS"){_, _ ->
                    // Show application settings
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", context.packageName, null)
                        intent.data = uri
                        startActivity(context, intent, Bundle.EMPTY)
                    } catch (e: ActivityNotFoundException){
                        e.printStackTrace()
                    }

                }
                .setNegativeButton("CANCEL") {dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}