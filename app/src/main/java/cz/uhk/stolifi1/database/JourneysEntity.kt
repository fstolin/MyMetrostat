package cz.uhk.stolifi1.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journeys-table")
data class JourneysEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("depart-station-name")
    val departStationName: String,
    @ColumnInfo("depart-station-line")
    val departStationLine: String,
    @ColumnInfo("arrive-station-name")
    val arriveStationName: String,
    @ColumnInfo("arrive-station-line")
    val arriveStationLine: String,
    val distance: Double,
    val duration: Int,
    val co2saved: Double,

)