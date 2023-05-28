package cz.uhk.stolifi1.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// metro station DAO
@Dao
interface MetroStationDAO {

    // Suspend - can't be done on the main thread
    @Insert
    suspend fun insert(metroStationEntity: MetroStationEntity)

    @Update
    suspend fun update(metroStationEntity: MetroStationEntity)

    @Delete
    suspend fun delete(metroStationEntity: MetroStationEntity)

    @Query("SELECT * FROM `metro-stations`")
    fun fetchAllMetroStations(): Flow<List<MetroStationEntity>>

    @Query("SELECT * FROM `metro-stations` WHERE id=:id")
    fun fetchMetroStationById(id: Int): Flow<MetroStationEntity>

}