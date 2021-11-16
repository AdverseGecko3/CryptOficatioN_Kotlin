package com.kotlin.cryptofication.data.model

import androidx.room.*

@Dao
interface CryptoAlertDAO {
    @Query("SELECT * FROM alert_crypto")
    suspend fun getAll(): List<CryptoAlert>

    @Query("SELECT * FROM alert_crypto WHERE id = :id")
    suspend fun getSingleAlert(id: String): CryptoAlert?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(cryptoAlert: CryptoAlert): Long

    @Delete
    suspend fun delete(cryptoAlert: CryptoAlert): Int
}