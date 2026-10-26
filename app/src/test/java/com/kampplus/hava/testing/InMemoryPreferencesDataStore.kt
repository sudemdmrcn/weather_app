package com.kampplus.hava.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Bellekte çalışan DataStore. Gerçek DataStore dosyaya yazarken geçici dosyayı üzerine yeniden
 * adlandırır; Windows'ta art arda yazmalarda bu adım test ortamında patlar. Bu sınıf aynı
 * arayüzü ([DataStore]) dosyasız sunar, böylece [com.kampplus.hava.data.DataStorePreferencesRepository]
 * mantığı (varsayılanlar, tekrar eklememe, bozuk veri) her işletim sisteminde aynı şekilde sınanır.
 */
class InMemoryPreferencesDataStore : DataStore<Preferences> {

    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
