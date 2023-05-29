package cz.uhk.stolifi1.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

class Utils {

    companion object {

        // shows a dismissable Snackbar
        fun showDSnack(str: String, view: View){
            val snack = Snackbar.make(view,"Your stats", Snackbar.LENGTH_SHORT)
            snack.setAction("Dismiss") { snack.dismiss() }
            snack.show()
        }

    }
}