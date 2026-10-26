package com.kampplus.hava

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kampplus.hava.ui.HavaNavHost
import com.kampplus.hava.ui.theme.HavaTheme

/**
 * Uygulamanın tek Activity'si; kullanıcının girdiği kapı.
 *
 * **Activity'nin rolü:** Android'in uygulamaya verdiği *pencereyi* yönetir ve
 * yaşam döngüsünü (açıldı, görünür oldu, arka plana gitti...) bildirir.
 * Manifest'te kayıtlı olması gerekir; sistem onu ana ekran simgesinden başlatır.
 *
 * **Fragment'in rolü:** Klasik (XML) Android'de bir Activity'nin içine yerleştirilen,
 * kendi yaşam döngüsü olan *ekran parçasıdır* (örn. hava durumu listesi + detay).
 * Bu proje Jetpack Compose kullandığı için Fragment yoktur: aynı işi
 * `TodayScreen` / `CitiesScreen` gibi `@Composable` fonksiyonlar yapar. Yani
 * Activity = sahne, Composable (eski adıyla Fragment) = sahnedeki ekran parçası.
 */
class MainActivity : ComponentActivity() {

    /**
     * Ekran oluşturulurken Android bu fonksiyonu **kendisi** çağırır; biz çağırmayız.
     * Yaşam döngüsünün ilk adımıdır.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        // İçeriği durum ve navigasyon çubuklarının altına kadar yayar.
        enableEdgeToEdge()

        // Süslü parantezin içi Compose dünyasıdır.
        setContent {
            HavaTheme {
                HavaNavHost()
            }
        }
    }

    /** Ekran kullanıcıya görünür olmaya başladı (henüz etkileşim yok). */
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    /** Uygulama ön planda ve kullanıcı etkileşime girebilir. */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    /** Uygulama ön plandan çıkıyor (Home'a basıldı, başka uygulama açıldı). */
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    /** Ekran artık görünmüyor; uygulama arka planda. */
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private companion object {
        /** Logcat'te filtrelemek için: arama kutusuna `Lifecycle` yaz. */
        const val TAG = "Lifecycle"
    }
}
