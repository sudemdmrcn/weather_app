# Hava

Hava durumu uygulaması. Kotlin · Jetpack Compose (Material 3). Başlangıç noktası:
[tc-yusufburakdemir/weather-app](https://github.com/tc-yusufburakdemir/weather-app) `cp1-baslangic`.

## Android Studio'da açma ve çalıştırma

1. `File > Open` ile bu klasörü seç, Gradle sync bitene kadar bekle.
2. `Device Manager`'dan bir emülatör başlat (örn. `Medium_Phone`).
3. Üstteki cihaz listesinden emülatörü seç, yeşil **Run ▶** düğmesine bas.

Ekranda `strings.xml` içindeki `welcome_message` metni görünür.
Metni değiştirip tekrar Run edersen ekranda yeni metni görürsün.

## Run, Build ve Logcat farkı

| Araç | Ne işe yarar |
|---|---|
| **Build** (`Build > Make Project`, `./gradlew assembleDebug`) | Kodu derler ve APK üretir. Cihaza **kurmaz**, çalıştırmaz. Hata varsa burada çıkar. |
| **Run ▶** | Önce build eder, APK'yı seçili emülatöre/cihaza **kurar** ve uygulamayı **başlatır**. |
| **Logcat** | Uygulama çalışırken cihazın ürettiği **log akışını** gösterir. Kod çalıştırmaz; yalnızca olanı izlersin. |

## Activity ve Fragment

- **Activity** (`MainActivity`): Android'in verdiği pencere. Yaşam döngüsünü yönetir, Manifest'te kayıtlıdır.
- **Fragment**: XML dünyasında Activity içindeki ekran parçası. Compose'da yerini `@Composable`
  fonksiyonlar alır (`HavaScreen`), bu yüzden projede Fragment sınıfı yok.

## Ön plan / arka plan geçişini Logcat'te izleme

Logcat'te arama kutusuna `Lifecycle` yaz (etiket: `Lifecycle`).

| Eylem | Logcat'te görülen |
|---|---|
| Uygulama ilk açılır | `onCreate` → `onStart` → `onResume` |
| Home'a basılır (arka plana) | `onPause` → `onStop` |
| Uygulamaya geri dönülür (ön plana) | `onStart` → `onResume` |
| Uygulama kapatılır | `onPause` → `onStop` → `onDestroy` |

## Komutlar

```bash
./gradlew assembleDebug        # debug APK
./gradlew spotlessApply        # ktlint formatı
```

SDK yolu `local.properties` içinde `sdk.dir=...` ile verilir (git'e girmez).
