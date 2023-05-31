package cz.uhk.stolifi1.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity for the simplified station data from the JSON
@Entity(tableName = "metro-stations")
data class MetroStationEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val line: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    var arrivalCount: Int = 0,
    var departureCount: Int = 0

)