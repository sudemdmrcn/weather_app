# Hava

Türkiye şehirleri ve dünya genelinden şehirler için anlık durum, saatlik ve 7 günlük hava tahmini gösteren
Android uygulaması. Kotlin ve Jetpack Compose ile yazılmıştır, veri [Open-Meteo](https://open-meteo.com/)
servisinden gelir (API anahtarı gerekmez).

Bilişim Kampı referans projesi
([tc-yusufburakdemir/weather-app](https://github.com/tc-yusufburakdemir/weather-app)) temel alınarak geliştirilmiştir.

## Özellikler

- **Bugün:** Anlık sıcaklık, hissedilen sıcaklık, rüzgâr, nem ve yağış olasılığı. Saatlik şeritten bir saate
  dokununca ekran o saatin değerlerine göre güncellenir. 7 günlük tahmin, günlük sıcaklık aralık çubukları,
  UV indeksi, gün doğumu/batımı, basınç, görüş ve rüzgâr yönü.
- **Şehirler:** Kayıtlı şehirlerin özet kartları ("Konumum" etiketi, telefonla saat farkı), arama kutusuyla
  süzme. Şehir seçilince Bugün ekranına geçilir.
- **Şehir ekleme:** Harf yazdıkça arama. Önce Türkiye'nin 81 ili (doğru il merkeziyle), 2+ harfte dünya
  şehirleri.
- **°C / °F** geçişi, seçim uygulama kapansa da korunur.
- Açık ve koyu tema, her ekranda yükleme (iskelet), liste, boş ve hata (tekrar dene) durumları.
- Erişilebilirlik: seçili durumu okuyan düğmeler, ikonlara açıklama, en az 48dp dokunma alanı.

## Teknoloji

Kotlin · Jetpack Compose (Material 3) · Navigation Compose · ViewModel + StateFlow · Coroutines ·
Retrofit + Gson · DataStore (Preferences) · JUnit + MockWebServer

## Mimari

```
UI (Composable)  →  ViewModel (StateFlow)  →  Repository (suspend)  →  Retrofit  →  Open-Meteo
```

Ekranlar yalnızca `LoadState` (Loading / Success / Empty / Error) görüntüler; veri kaynağını bilmez.

```
com.kampplus.hava
├── MainActivity.kt          Uygulamanın giriş noktası
├── model/                   Veri sınıfları (City, Forecast, TemperatureUnit, WeatherCondition)
├── data/                    Retrofit arayüzleri, repository'ler, tercihler (DataStore), il listesi
├── ui/
│   ├── HavaNavHost.kt       Alt gezinme ve ekranlar arası geçiş
│   ├── today/               Bugün ekranı
│   ├── cities/              Şehirler ekranı
│   ├── city/                Şehir arama / ekleme ekranı
│   ├── component/           Ortak bileşenler (kart, alt çubuk, ikon, iskelet, durum görünümleri)
│   └── theme/               Renk, tipografi ve hava durumu tasarım jetonları
└── res/                     Metinler, ikonlar, yazı tipleri
```

## Çalıştırma

Gereksinimler: Android Studio (güncel), JDK 17 veya 21, Android SDK 36.

1. `File > Open` ile klasörü aç ve Gradle sync'in bitmesini bekle.
2. Bir emülatör başlat ya da cihaz bağla, **Run ▶** düğmesine bas.

SDK yolu `local.properties` içinde `sdk.dir=...` ile verilir (git'e girmez). Android Studio'nun kendi
JDK'sı Gradle sürümüyle uyumsuzsa `Settings > Build Tools > Gradle > Gradle JDK` altından 17 veya 21 seç.

```bash
./gradlew assembleDebug        # debug APK
./gradlew assembleRelease      # release APK (R8 açık)
./gradlew testDebugUnitTest    # birim testleri
./gradlew spotlessCheck        # ktlint biçim denetimi (düzeltmek için spotlessApply)
./gradlew lintDebug            # Android Lint
```

### Veri kaynağı

`gradle.properties` içindeki bayrak gerçek servis ile örnek veri arasında seçim yapar
(`WeatherRepositoryProvider` içinde tek yerde uygulanır, değiştirince Gradle sync gerekir):

```
weather.useSampleData=false   # gerçek Open-Meteo (varsayılan)
weather.useSampleData=true    # örnek veri, internet gerekmez
```

### Release imzası

`keystore.properties.example` dosyasını `keystore.properties` olarak kopyalayıp doldur (git'e girmez).
Dosya yoksa release APK debug anahtarıyla imzalanır.

## Testler

Saf Kotlin birim testleri, emülatör gerekmez: ağ yanıtlarının (sütun → satır çevrimi) doğru okunması,
ViewModel durumları ve arama akışı, tercihlerin saklanması, birim dönüşümü, saat dilimi hesapları ve
il listesinin doğruluğu.

## Android temelleri (ders notları)

- **Activity** (`MainActivity`): Android'in uygulamaya verdiği pencere; yaşam döngüsünü yönetir ve Manifest'te
  kayıtlıdır. **Fragment** klasik XML arayüzlerde Activity içindeki ekran parçasıdır. Compose'da yerini
  `@Composable` fonksiyonlar alır (`TodayScreen`, `CitiesScreen`), bu yüzden projede Fragment yoktur.
- **Ön plan / arka plan geçişi** Logcat'te `Lifecycle` etiketiyle izlenir:

  | Eylem | Logcat |
  |---|---|
  | Uygulama açılır | `onCreate` → `onStart` → `onResume` |
  | Home'a basılır (arka plan) | `onPause` → `onStop` |
  | Uygulamaya dönülür (ön plan) | `onStart` → `onResume` |
  | Uygulama kapatılır | `onPause` → `onStop` → `onDestroy` |

- **Build / Run / Logcat:** Build derler ve APK üretir, cihaza kurmaz. Run önce build eder, kurar ve
  başlatır. Logcat çalışan uygulamanın log akışını gösterir, kod çalıştırmaz.

## Üçüncü taraf

- Yazı tipleri: Space Grotesk ve Inter (SIL Open Font License 1.1), `res/font/`
- İkonlar: [Lucide](https://lucide.dev/) (ISC lisansı), `res/drawable/ic_*.xml`
- Hava verisi: [Open-Meteo](https://open-meteo.com/) (ticari olmayan kullanım için ücretsiz)
