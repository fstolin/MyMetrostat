package cz.uhk.stolifi1.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// List our entities
@Database(entities = [MetroStationEntity::class, JourneysEntity::class, inProressJourneyEntity::class], version = 4)
abstract class MetroStationDatabase : RoomDatabase() {

    // Entity DAOs
    abstract fun metroStationDao(): MetroStationDAO
    abstract fun journeysDao(): JourneyDAO
    abstract fun inProgressDao(): inProgressJourneyDAO

    companion object {

        // Avoid repeatedly initializing the database
        @Volatile
        private var INSTANCE: MetroStationDatabase? = null

        // Singleton pattern - single instance of database
        fun getInstance(context: Context): MetroStationDatabase{
            // 1 thread at a time
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    // If database changed - we will delete it
                    instance = Room.databaseBuilder(context.applicationContext, MetroStationDatabase::class.java, "MetroStationDatabase").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return  instance
            }
        }

    }
}