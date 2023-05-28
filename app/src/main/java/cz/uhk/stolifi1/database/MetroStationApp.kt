package cz.uhk.stolifi1.database

import android.app.Application

// Every application class has to be declared in Android Manifest!
class MetroStationApp:Application() {
    val db by lazy{
        MetroStationDatabase.getInstance(this)
    }
}