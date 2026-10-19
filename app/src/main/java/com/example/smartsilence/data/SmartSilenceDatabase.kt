package com.example.smartsilence.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        LocationEntity::class,
        TaskEntity::class,
        PriorityContactEntity::class,
        SettingsEntity::class,
        LocationPriorityContactEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SmartSilenceDatabase : RoomDatabase() {

    abstract fun smartSilenceDao(): SmartSilenceDao

    companion object {

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS location_priority_contacts (
                        locationId INTEGER NOT NULL,
                        contactId INTEGER NOT NULL,
                        PRIMARY KEY(locationId, contactId),
                        FOREIGN KEY(locationId)
                            REFERENCES locations(id)
                            ON DELETE CASCADE,
                        FOREIGN KEY(contactId)
                            REFERENCES priority_contacts(id)
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_location_priority_contacts_locationId
                    ON location_priority_contacts(locationId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_location_priority_contacts_contactId
                    ON location_priority_contacts(contactId)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                    ALTER TABLE settings
                    ADD COLUMN autoReplyMessage TEXT NOT NULL
                    DEFAULT 'Hi, I''m currently unavailable. I''ll get back to you soon.'
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: SmartSilenceDatabase? = null

        fun getDatabase(context: Context): SmartSilenceDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartSilenceDatabase::class.java,
                    "smart_silence_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}