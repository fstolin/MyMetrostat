package cz.uhk.stolifi1.journey

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cz.uhk.stolifi1.R

// TODO Potential journey complete activity which shows details of finished journey
// Station A, Station B, duration, distance, CO2 saved

class JourneyCompleteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey_complete)
    }
}