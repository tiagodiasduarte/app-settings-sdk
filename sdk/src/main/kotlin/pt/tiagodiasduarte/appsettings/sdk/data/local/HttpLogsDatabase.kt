package pt.tiagodiasduarte.appsettings.sdk.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The SDK's HTTP logs database. Logs expire within a week at most, so schema changes drop the
 * tables instead of migrating them.
 */
@Database(entities = [HttpLogEntity::class, HttpLogBodiesEntity::class], version = 1, exportSchema = true)
@TypeConverters(HeadersConverter::class)
internal abstract class HttpLogsDatabase : RoomDatabase() {

    abstract fun httpLogsDao(): HttpLogsDao

    companion object {
        const val NAME = "appsettings_http_logs.db"

        fun create(context: Context): HttpLogsDatabase =
            Room.databaseBuilder(context.applicationContext, HttpLogsDatabase::class.java, NAME)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
