package com.adversegecko3.cryptofication.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CryptoAlert::class],
    version = 9
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
                    .addMigrations(
                        migration_7_8,
                        migration_8_9
                    )
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        private val migration_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alert_crypto ADD COLUMN quantity REAL NOT NULL DEFAULT 0")
            }
        }

        private val migration_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alert_crypto ADD COLUMN symbol TEXT NOT NULL DEFAULT 'crypto'")
                db.execSQL("ALTER TABLE alert_crypto ADD COLUMN current_price REAL NOT NULL DEFAULT 0")
            }
        }
    }
}