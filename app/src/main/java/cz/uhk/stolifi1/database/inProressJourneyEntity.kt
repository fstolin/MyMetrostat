package cz.uhk.stolifi1.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "in-progress-table")
data class inProressJourneyEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "start-station-id")
    val startStationId: Int,
    @ColumnInfo(name = "start-time")
    val startTime: Double
)