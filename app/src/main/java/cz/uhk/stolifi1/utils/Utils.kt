package cz.uhk.stolifi1.utils

import android.graphics.drawable.Drawable
import android.view.View
import com.google.android.material.snackbar.Snackbar
import cz.uhk.stolifi1.R

class Utils {

    companion object {

        var journeyId: Long = 0

        // shows a dismissable Snackbar
        fun showDSnack(str: String, view: View){
                val snack = Snackbar.make(view,str, Snackbar.LENGTH_SHORT)
            snack.setAction("Dismiss") { snack.dismiss() }
            snack.show()
        }

        private fun getLineDrawable(char: Char): Int {
            if (char == 'B') return R.drawable.metrob
            if (char == 'C') return R.drawable.metroc
            return R.drawable.metroa
        }

        fun getLineDrawablesTransfer(str: String): List<Int> {
            var theList = arrayListOf<Int>()
            for (c in str){
                theList.add(getLineDrawable(c))
            }
            return theList
        }

    }
}