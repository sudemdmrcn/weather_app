package com.kampplus.hava.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.kampplus.hava.model.City
import com.kampplus.hava.model.TemperatureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Kullanıcı tercihlerinin tek kapısı: sıcaklık birimi, kayıtlı şehirler ve seçili şehir.
 *
 * Arayüz olarak tanımlıdır ki ViewModel testleri bellekte çalışan bir sahte ile yapılabilsin
 * ([DataStorePreferencesRepository] gerçek uygulamadır, diske yazar).
 */
interface PreferencesRepository {
    /** Gösterim birimi. Veri hep Celsius saklanır, yalnızca gösterim değişir. */
    val unit: Flow<TemperatureUnit>

    /** Kayıtlı şehirler, eklenme sırasıyla. İlk şehir "Konumum" sayılır. Hiçbir zaman boş değildir. */
    val savedCities: Flow<List<City>>

    /** Bugün ekranında gösterilen şehir. Kayıtlı listede değilse ilk kayıtlı şehir. */
    val selectedCity: Flow<City>

    suspend fun setUnit(unit: TemperatureUnit)

    /** Şehri listeye ekler. Aynı `id` zaten varsa tekrar eklenmez. */
    suspend fun addCity(city: City)

    suspend fun selectCity(cityId: Long)
}

/** Hiç şehir kaydedilmemişken kullanılan başlangıç şehri. */
val DEFAULT_CITY = City(
    id = 745044L,
    name = "İstanbul",
    latitude = 41.01384,
    longitude = 28.94966,
    countryCode = "TR",
    country = "Türkiye",
    admin1 = "İstanbul"
)

private val Context.havaDataStore: DataStore<Preferences> by preferencesDataStore(name = "hava_prefs")

/**
 * Gerçek uygulama: Jetpack DataStore (Preferences). Şehir listesi tek bir JSON metni olarak saklanır;
 * bozuk veya eski biçimli kayıt uygulamayı çökertmez, başlangıç listesine dönülür.
 */
class DataStorePreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson = Gson()
) : PreferencesRepository {

    constructor(context: Context) : this(context.applicationContext.havaDataStore)

    override val unit: Flow<TemperatureUnit> = dataStore.data.map { prefs ->
        prefs[UNIT_KEY]?.let { name -> TemperatureUnit.entries.firstOrNull { it.name == name } }
            ?: TemperatureUnit.CELSIUS
    }

    override val savedCities: Flow<List<City>> = dataStore.data.map { prefs -> parseCities(prefs[CITIES_KEY]) }

    override val selectedCity: Flow<City> = combine(savedCities, dataStore.data.map { it[SELECTED_KEY] }) { cities, id ->
        cities.firstOrNull { it.id == id } ?: cities.first()
    }

    override suspend fun setUnit(unit: TemperatureUnit) {
        dataStore.edit { it[UNIT_KEY] = unit.name }
    }

    override suspend fun addCity(city: City) {
        dataStore.edit { prefs ->
            val current = parseCities(prefs[CITIES_KEY])
            if (current.none { it.id == city.id }) {
                prefs[CITIES_KEY] = gson.toJson(current + city)
            }
        }
    }

    override suspend fun selectCity(cityId: Long) {
        dataStore.edit { it[SELECTED_KEY] = cityId }
    }

    private fun parseCities(json: String?): List<City> {
        if (json.isNullOrBlank()) return listOf(DEFAULT_CITY)
        return try {
            gson.fromJson<List<City>>(json, CITY_LIST_TYPE).orEmpty().ifEmpty { listOf(DEFAULT_CITY) }
        } catch (e: JsonParseException) {
            listOf(DEFAULT_CITY)
        }
    }

    private companion object {
        val UNIT_KEY = stringPreferencesKey("temperature_unit")
        val CITIES_KEY = stringPreferencesKey("saved_cities")
        val SELECTED_KEY = longPreferencesKey("selected_city_id")
        val CITY_LIST_TYPE = object : TypeToken<List<City>>() {}.type
    }
}
