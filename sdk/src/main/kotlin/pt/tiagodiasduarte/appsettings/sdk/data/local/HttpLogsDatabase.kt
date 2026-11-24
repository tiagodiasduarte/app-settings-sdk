package pt.tiagodiasduarte.appsettings.sdk.data.local

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

/** Placeholder entity so Room can generate the database; replaced by the real schema. */
@Entity(tableName = "http_logs")
internal data class HttpLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
)

@Database(entities = [HttpLogEntity::class], version = 1, exportSchema = true)
internal abstract class HttpLogsDatabase : RoomDatabase()
