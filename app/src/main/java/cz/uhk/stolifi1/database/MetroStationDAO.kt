package cz.uhk.stolifi1.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// metro station DAO
@Dao
interface MetroStationDAO {

    // Suspend - can't be done on the main thread
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(metroStationEntity: MetroStationEntity)

    @Update
    suspend fun update(metroStationEntity: MetroStationEntity)

    @Delete
    suspend fun delete(metroStationEntity: MetroStationEntity)

    @Query("DELETE FROM `metro-stations` WHERE id > 0")
    fun deleteAll()

    @Query("SELECT * FROM `metro-stations`")
    fun fetchAllMetroStations(): Flow<List<MetroStationEntity>>

    @Query("SELECT * FROM `metro-stations` WHERE id=:id")
    fun fetchMetroStationById(id: Int): Flow<MetroStationEntity>

    suspend fun insertNonDuplicate(metroStationEntity: MetroStationEntity, existingMetroStations: List<MetroStationEntity>) {
        for (mse in existingMetroStations) {
            if (metroStationEntity.name == mse.name) return;
        }
        insert(metroStationEntity)
    }

}