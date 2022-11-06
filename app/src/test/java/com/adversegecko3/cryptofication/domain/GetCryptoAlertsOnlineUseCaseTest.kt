package com.adversegecko3.cryptofication.domain

import com.adversegecko3.cryptofication.data.model.Crypto
import com.adversegecko3.cryptofication.data.model.CryptoSparkline
import com.adversegecko3.cryptofication.data.repos.CryptoAlertRepository
import com.adversegecko3.cryptofication.data.repos.CryptoProvider
import com.adversegecko3.cryptofication.data.repos.CryptoRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class GetCryptoAlertsOnlineUseCaseTest {

    @RelaxedMockK
    private lateinit var repository: CryptoRepository

    @RelaxedMockK
    private lateinit var cryptoProvider: CryptoProvider

    @RelaxedMockK
    private lateinit var mRoom: CryptoAlertRepository

    lateinit var getCryptoAlertsOnlineUseCase: GetCryptoAlertsOnlineUseCase

    @Before
    fun onBefore() {
        MockKAnnotations.init(this)
        getCryptoAlertsOnlineUseCase =
            GetCryptoAlertsOnlineUseCase(repository, cryptoProvider, mRoom)
    }

    @Test
    fun `When the API returns an empty list`() = runBlocking {
        //Given
        coEvery { repository.getAllCryptoAlerts() } returns emptyList()

        //When
        val response = getCryptoAlertsOnlineUseCase()

        //Then
        assert(response == emptyList<Any>())
    }

    @Test
    fun `When the API returns a list with BTC`() = runBlocking {
        //Given
        val list = listOf(
            Crypto(
                "dogecoin", "dogecoin", "DOGE", "", 0.13,
                10, 0.13, 0.13, 0.000255,
                5.0, CryptoSparkline(listOf(0.13))
            ),
            Crypto(
                "shiba-inu", "shiba-inu", "SHIB", "", 0.000012,
                10, 0.000012, 0.000012, 0.0000001,
                5.0, CryptoSparkline(listOf(0.000012))
            ),
            Crypto(
                "bitcoin", "bitcoin", "BTC", "", 20500.0,
                1, 20500.0, 20500.0, 100.0,
                5.0, CryptoSparkline(listOf(20500.0))
            ),
            Crypto(
                "troy", "troy", "TROY", "", 0.0035,
                1, 0.0035, 0.0035, 0.00005,
                5.0, CryptoSparkline(listOf(0.0035))
            )
        )
        coEvery { repository.getAllCryptoAlerts() } returns list
        val spy = Mockito.spy(GetCryptoAlertsOnlineUseCase::class.java)

        //When
        val response = getCryptoAlertsOnlineUseCase()

        //Then
        Mockito.doReturn(true).`when`(spy).checkIfAlertsHasBitcoin()
        assert(response != emptyList<Any>())
    }

    @Test
    fun `When the API returns a list without BTC`() = runBlocking {
        //Given
        val list = listOf(
            Crypto(
                "dogecoin", "dogecoin", "DOGE", "", 0.13,
                10, 0.13, 0.13, 0.000255,
                5.0, CryptoSparkline(listOf(0.13))
            ),
            Crypto(
                "shiba-inu", "shiba-inu", "SHIB", "", 0.000012,
                10, 0.000012, 0.000012, 0.0000001,
                5.0, CryptoSparkline(listOf(0.000012))
            ),
            Crypto(
                "troy", "troy", "TROY", "", 0.0035,
                1, 0.0035, 0.0035, 0.00005,
                5.0, CryptoSparkline(listOf(0.0035))
            )
        )
        coEvery { repository.getAllCryptoAlerts() } returns list
        val spy = Mockito.spy(GetCryptoAlertsOnlineUseCase::class.java)

        //When
        val response = getCryptoAlertsOnlineUseCase()

        //Then
        Mockito.doReturn(false).`when`(spy).checkIfAlertsHasBitcoin()
        assert(response != emptyList<Any>())
    }
}