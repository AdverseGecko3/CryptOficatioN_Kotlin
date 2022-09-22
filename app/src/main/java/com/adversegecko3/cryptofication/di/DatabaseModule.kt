package com.adversegecko3.cryptofication.di

import android.content.Context
import com.adversegecko3.cryptofication.data.model.CryptoAlertDAO
import com.adversegecko3.cryptofication.data.model.CryptoAlertDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun providesDatabase(@ApplicationContext appContext: Context): CryptoAlertDB =
        CryptoAlertDB.getDatabase(appContext)

    @Singleton
    @Provides
    fun providesDAO(database: CryptoAlertDB): CryptoAlertDAO =
        database.cryptoAlertDAO()
}