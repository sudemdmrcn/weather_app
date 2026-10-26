package com.kampplus.hava.data

import com.kampplus.hava.model.City
import java.text.Collator
import java.util.Locale

/**
 * Türkiye'nin 81 ili, doğru il merkezi koordinatlarıyla.
 *
 * Neden yerel liste? Open-Meteo geocoding tek harfe sonuç vermez ve il adlarını her zaman il
 * merkeziyle eşleştirmez: "Kocaeli" aratınca Balıkesir'deki küçük bir köy gelir (il merkezi
 * veritabanında "İzmit" adıyla kayıtlıdır). Bu liste her ilin **merkez** kaydının kimliğini ve
 * koordinatını taşır (Kocaeli → İzmit, Hatay → Antakya, Sakarya → Adapazarı...), böylece il adıyla
 * aranınca doğru yerin havası gelir.
 *
 * Veri Open-Meteo geocoding servisinden alınmıştır (il merkezi, PPLA/PPLC kayıtları); `id` o kaydın
 * kimliğidir, bu yüzden internetten aynı şehir gelirse tekrar eklenmez.
 */
object TurkishProvinces {

    // Sıra önemli: nesne alanları yukarıdan aşağı başlatılır, \`all\` bunu kullandığı için önce tanımlanır.
    private val COLLATOR: Collator = Collator.getInstance(Locale.forLanguageTag("tr"))

    /** İller, Türkçe alfabe sırasıyla (Ç, Ğ, İ, Ö, Ş, Ü yerlerinde). */
    val all: List<City> = listOf(
        province(325363L, "Adana", 36.98615, 35.32531),
        province(325330L, "Adıyaman", 37.76441, 38.27629),
        province(325303L, "Afyonkarahisar", 38.75667, 30.54333),
        province(309647L, "Ağrı", 39.71467, 43.04015),
        province(752015L, "Amasya", 40.65333, 35.83306),
        province(323786L, "Ankara", 39.91987, 32.85427),
        province(323777L, "Antalya", 36.90812, 30.69556),
        province(751817L, "Artvin", 41.18161, 41.82172),
        province(322830L, "Aydın", 37.84501, 27.83963),
        province(322165L, "Balıkesir", 39.64917, 27.88611),
        province(750598L, "Bilecik", 40.14192, 29.97932),
        province(321082L, "Bingöl", 38.88472, 40.49389),
        province(321025L, "Bitlis", 38.40115, 42.10784),
        province(750516L, "Bolu", 40.73583, 31.60611),
        province(320392L, "Burdur", 37.72028, 30.29083),
        province(750269L, "Bursa", 40.19559, 29.06013),
        province(749780L, "Çanakkale", 40.15552, 26.41271),
        province(749748L, "Çankırı", 40.59995, 33.6153),
        province(748879L, "Çorum", 40.54889, 34.95333),
        province(317109L, "Denizli", 37.77417, 29.0875),
        province(316541L, "Diyarbakır", 37.91363, 40.21721),
        province(747712L, "Edirne", 41.67719, 26.55597),
        province(315808L, "Elazığ", 38.67431, 39.22321),
        province(315373L, "Erzincan", 39.73919, 39.49015),
        province(315368L, "Erzurum", 39.90861, 41.27694),
        province(315202L, "Eskişehir", 39.77667, 30.52056),
        province(314830L, "Gaziantep", 37.05944, 37.3825),
        province(746881L, "Giresun", 40.91698, 38.38741),
        province(746425L, "Gümüşhane", 40.46001, 39.47176),
        province(318137L, "Hakkâri", 37.57444, 43.74083),
        province(323779L, "Hatay", 36.20655, 36.15722),
        province(311073L, "Isparta", 37.76444, 30.55222),
        province(304531L, "Mersin", 36.81196, 34.63886),
        province(745044L, "İstanbul", 41.01384, 28.94966),
        province(311046L, "İzmir", 38.41273, 27.13838),
        province(743952L, "Kars", 40.59825, 43.08548),
        province(743882L, "Kastamonu", 41.37805, 33.77528),
        province(308464L, "Kayseri", 38.73222, 35.48528),
        province(743166L, "Kırklareli", 41.73508, 27.22521),
        province(307515L, "Kırşehir", 39.14583, 34.16389),
        province(745028L, "Kocaeli", 40.76499, 29.92928),
        province(306571L, "Konya", 37.87135, 32.48464),
        province(305268L, "Kütahya", 39.42417, 29.98333),
        province(304922L, "Malatya", 38.35018, 38.31667),
        province(304827L, "Manisa", 38.61202, 27.42647),
        province(310859L, "Kahramanmaraş", 37.5847, 36.92641),
        province(304797L, "Mardin", 37.31309, 40.74357),
        province(304184L, "Muğla", 37.21807, 28.3665),
        province(304081L, "Muş", 38.73163, 41.48482),
        province(303831L, "Nevşehir", 38.625, 34.71222),
        province(303827L, "Niğde", 37.96583, 34.67935),
        province(741100L, "Ordu", 40.97782, 37.89047),
        province(740483L, "Rize", 41.02083, 40.52194),
        province(752850L, "Sakarya", 40.78056, 30.40333),
        province(740264L, "Samsun", 41.27976, 36.3361),
        province(300822L, "Siirt", 37.9293, 41.94134),
        province(739600L, "Sinop", 42.02683, 35.16253),
        province(300619L, "Sivas", 39.74833, 37.01611),
        province(738927L, "Tekirdağ", 40.9781, 27.51101),
        province(738743L, "Tokat", 40.31389, 36.55444),
        province(738648L, "Trabzon", 41.005, 39.72694),
        province(298846L, "Tunceli", 39.09921, 39.54351),
        province(298333L, "Şanlıurfa", 37.16708, 38.79392),
        province(298299L, "Uşak", 38.67351, 29.4058),
        province(298117L, "Van", 38.49457, 43.38323),
        province(296562L, "Yozgat", 39.82, 34.80444),
        province(737022L, "Zonguldak", 41.45139, 31.79305),
        province(324496L, "Aksaray", 38.37255, 34.02537),
        province(750938L, "Bayburt", 40.25631, 40.22289),
        province(309527L, "Karaman", 37.18111, 33.215),
        province(307654L, "Kırıkkale", 39.84528, 33.50639),
        province(321836L, "Batman", 37.88738, 41.13221),
        province(300640L, "Şırnak", 37.51393, 42.45432),
        province(751057L, "Bartın", 41.63583, 32.3375),
        province(751952L, "Ardahan", 41.10871, 42.70222),
        province(311665L, "Iğdır", 39.92371, 44.045),
        province(738025L, "Yalova", 40.65501, 29.27693),
        province(744562L, "Karabük", 41.20488, 32.62768),
        province(307864L, "Kilis", 36.71611, 37.115),
        province(303195L, "Osmaniye", 37.07417, 36.24778),
        province(747764L, "Düzce", 40.83889, 31.16389)
    ).sortedWith { a, b -> COLLATOR.compare(a.name, b.name) }

    private fun province(id: Long, name: String, latitude: Double, longitude: Double) = City(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        countryCode = "TR",
        country = "Türkiye",
        admin1 = name
    )

    /**
     * Aranan metinle **başlayan** illeri döndürür; boş arama tüm illeri verir.
     *
     * Eşleştirme harf farkına bakmaz: büyük/küçük harf ve Türkçe karakterler yok sayılır, böylece
     * "sanliurfa", "Sanli" ya da "ISPARTA" yazınca da Şanlıurfa ve Isparta bulunur.
     */
    fun startingWith(query: String, provinces: List<City> = all): List<City> {
        val prefix = fold(query)
        if (prefix.isEmpty()) return provinces
        return provinces.filter { fold(it.name).startsWith(prefix) }
    }

    /** Karşılaştırma için: küçük harfe çevirir, Türkçe ve şapkalı harfleri sadeleştirir, boşlukları kırpar. */
    internal fun fold(text: String): String = buildString {
        text.trim().forEach { ch ->
            append(
                when (ch) {
                    'İ', 'I', 'ı', 'i' -> 'i'
                    'Ş', 'ş' -> 's'
                    'Ğ', 'ğ' -> 'g'
                    'Ü', 'ü' -> 'u'
                    'Ö', 'ö' -> 'o'
                    'Ç', 'ç' -> 'c'
                    'Â', 'â' -> 'a'
                    'Î', 'î' -> 'i'
                    'Û', 'û' -> 'u'
                    else -> ch.lowercaseChar()
                }
            )
        }
    }
}
