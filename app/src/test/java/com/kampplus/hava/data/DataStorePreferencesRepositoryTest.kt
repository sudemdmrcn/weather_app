package com.kampplus.hava.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.testing.InMemoryPreferencesDataStore
import com.kampplus.hava.testing.sampleCity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tercih deposunun mantığını doğrular: varsayılanlar, kalıcılık (aynı depoyu yeni repository ile
 * tekrar okuyarak), tekrar eklememe ve bozuk veri. Depo bellekte çalışır (bkz. [InMemoryPreferencesDataStore]).
 */
class DataStorePreferencesRepositoryTest {

    private fun dataStore(): DataStore<Preferences> = InMemoryPreferencesDataStore()

    private val ankara = sampleCity(id = 323786L, name = "Ankara")
    private val izmir = sampleCity(id = 311046L, name = "İzmir")

    @Test
    fun `bos tercihlerde varsayilanlar doner`() = runTest {
        val repository = DataStorePreferencesRepository(dataStore())

        assertEquals(TemperatureUnit.CELSIUS, repository.unit.first())
        assertEquals(listOf(DEFAULT_CITY), repository.savedCities.first())
        assertEquals(DEFAULT_CITY, repository.selectedCity.first())
    }

    @Test
    fun `birim secimi kalicidir`() = runTest {
        val store = dataStore()
        DataStorePreferencesRepository(store).setUnit(TemperatureUnit.FAHRENHEIT)

        // Yeni bir repository aynı dosyadan okur; "uygulama yeniden açıldı" durumu.
        assertEquals(TemperatureUnit.FAHRENHEIT, DataStorePreferencesRepository(store).unit.first())
    }

    @Test
    fun `eklenen sehirler sirayla saklanir`() = runTest {
        val store = dataStore()
        val repository = DataStorePreferencesRepository(store)

        repository.addCity(ankara)
        repository.addCity(izmir)

        assertEquals(listOf(DEFAULT_CITY, ankara, izmir), DataStorePreferencesRepository(store).savedCities.first())
    }

    @Test
    fun `ayni sehir tekrar eklenmez`() = runTest {
        val repository = DataStorePreferencesRepository(dataStore())

        repository.addCity(ankara)
        repository.addCity(ankara)

        assertEquals(2, repository.savedCities.first().size)
    }

    @Test
    fun `secilen sehir saklanir`() = runTest {
        val store = dataStore()
        val repository = DataStorePreferencesRepository(store)
        repository.addCity(ankara)

        repository.selectCity(ankara.id)

        assertEquals(ankara, DataStorePreferencesRepository(store).selectedCity.first())
    }

    @Test
    fun `kayitli olmayan sehir secilirse ilk sehre donulur`() = runTest {
        val repository = DataStorePreferencesRepository(dataStore())

        repository.selectCity(424242L)

        assertEquals(DEFAULT_CITY, repository.selectedCity.first())
    }

    @Test
    fun `bozuk sehir kaydi uygulamayi cokertmez`() = runTest {
        val store = dataStore()
        store.edit { it[stringPreferencesKey("saved_cities")] = "bu json degil" }

        assertEquals(listOf(DEFAULT_CITY), DataStorePreferencesRepository(store).savedCities.first())
    }
}
