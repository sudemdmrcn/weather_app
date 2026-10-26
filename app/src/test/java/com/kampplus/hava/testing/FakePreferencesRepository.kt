package com.kampplus.hava.testing

import com.kampplus.hava.data.DEFAULT_CITY
import com.kampplus.hava.data.PreferencesRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.TemperatureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Bellekte çalışan sahte tercihler: diske yazmaz, ViewModel testlerini hızlı ve bağımsız tutar.
 * Gerçek uygulamanın ([com.kampplus.hava.data.DataStorePreferencesRepository]) davranışını
 * ayrıca kendi testi doğrular.
 */
class FakePreferencesRepository(initialCities: List<City> = listOf(DEFAULT_CITY)) : PreferencesRepository {

    private val cities = MutableStateFlow(initialCities)
    private val selectedId = MutableStateFlow<Long?>(null)
    private val currentUnit = MutableStateFlow(TemperatureUnit.CELSIUS)

    override val unit: Flow<TemperatureUnit> = currentUnit
    override val savedCities: Flow<List<City>> = cities
    override val selectedCity: Flow<City> = combine(cities, selectedId) { list, id ->
        list.firstOrNull { it.id == id } ?: list.first()
    }

    override suspend fun setUnit(unit: TemperatureUnit) {
        currentUnit.value = unit
    }

    override suspend fun addCity(city: City) {
        if (cities.value.none { it.id == city.id }) cities.value = cities.value + city
    }

    override suspend fun selectCity(cityId: Long) {
        selectedId.value = cityId
    }
}
