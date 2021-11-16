package com.kotlin.cryptofication.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CryptoAlert::class],
    version = 7
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
                    "cryptofication_database")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}