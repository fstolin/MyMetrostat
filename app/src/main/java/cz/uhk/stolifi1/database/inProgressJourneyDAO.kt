package cz.uhk.stolifi1.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface inProgressJourneyDAO {
    // Suspend - can't be done on the main thread
    @Insert
    suspend fun insert(journeysEntity: JourneysEntity)

    @Update
    suspend fun update(journeysEntity: JourneysEntity)

    @Delete
    suspend fun delete(journeysEntity: JourneysEntity)

    @Query("SELECT * FROM `in-progress-table`")
    fun fetchAllJourneys(): Flow<List<inProressJourneyEntity>>
}