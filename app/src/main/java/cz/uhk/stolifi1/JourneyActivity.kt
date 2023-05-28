package cz.uhk.stolifi1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import cz.uhk.stolifi1.databinding.ActivityJourneyBinding
import cz.uhk.stolifi1.databinding.ActivityMainBinding

class JourneyActivity : AppCompatActivity() {

    private var binding: ActivityJourneyBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journey)
        // View-binding
        binding = ActivityJourneyBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        // Buttons
        binding?.gpsNow?.setOnClickListener{ gpsNowButton() }
    }

    private fun gpsNowButton(){
        Toast.makeText(this@JourneyActivity, "Yo yo, journey activity", Toast.LENGTH_SHORT).show()
    }

    // Unassign view binding
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}