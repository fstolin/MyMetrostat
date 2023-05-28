package cz.uhk.stolifi1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    }

    private fun startButton() {
        Toast.makeText(this@MainActivity, "Start button pressed", Toast.LENGTH_SHORT).show()
    }

    private fun statsButton() {
        Toast.makeText(this@MainActivity, "Your statistics", Toast.LENGTH_SHORT).show()
    }
}