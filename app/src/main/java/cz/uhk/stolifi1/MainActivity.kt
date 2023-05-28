package cz.uhk.stolifi1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cz.uhk.stolifi1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // bind view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // start button listener
        binding?.startButton?.setOnClickListener { startButton() }
        // stats button listener
        binding?.statsButton?.setOnClickListener{ statsButton() }

        // hide buttons (code to remember)
        //binding?.statsButton?.visibility = View.INVISIBLE
    }

    private fun startButton() {
        val intent = Intent(this, JourneyActivity::class.java)
        startActivity(intent)
    }

    private fun statsButton() {
        Toast.makeText(this@MainActivity, "Your statistics", Toast.LENGTH_SHORT).show()
    }

    // Unassign view binding
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}