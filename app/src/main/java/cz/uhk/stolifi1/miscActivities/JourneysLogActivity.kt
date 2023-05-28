package cz.uhk.stolifi1.miscActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cz.uhk.stolifi1.R

// TODO optional list / log of already completed journeys with their details from journeyCompleteActivity
// Use listView?

class JourneysLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journeys_log)
    }
}