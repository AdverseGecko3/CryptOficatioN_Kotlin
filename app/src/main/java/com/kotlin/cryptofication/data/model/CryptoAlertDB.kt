package com.kotlin.cryptofication.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CryptoAlert::class],
    version = 8
)
abstract class CryptoAlertDB : RoomDatabase() {

    abstract fun cryptoAlertDAO(): CryptoAlertDAO

    companion object {
        @Volatile
        private var INSTANCE: CryptoAlertDB? = null

        fun getDatabase(context: Context): CryptoAlertDB {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CryptoAlertDB::class.java,
                    "cryptofication_database"
                )
                    .addMigrations(migration_7_8)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        private val migration_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE alert_crypto ADD COLUMN quantity REAL NOT NULL DEFAULT 0")
            }
        }
    }
}