package com.kampplus.hava.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** İl listesinin tamlığı ve eşleştirmesi; "Kocaeli yanlış yerde çıkıyor" hatasının tekrarını önler. */
class TurkishProvincesTest {

    private val all = TurkishProvinces.all

    @Test
    fun `81 il vardir ve kimlikler benzersizdir`() {
        assertEquals(81, all.size)
        assertEquals(81, all.map { it.id }.toSet().size)
        assertEquals(81, all.map { it.name }.toSet().size)
    }

    @Test
    fun `tum iller turkiye sinirlari icindedir`() {
        // Türkiye yaklaşık 36-42 enlem, 26-45 boylam arasındadır; yanlış yere düşen kayıt burada yakalanır.
        all.forEach { city ->
            assertTrue("${city.name} enlemi: ${city.latitude}", city.latitude in 35.8..42.2)
            assertTrue("${city.name} boylamı: ${city.longitude}", city.longitude in 25.6..44.9)
            assertEquals("TR", city.countryCode)
        }
    }

    @Test
    fun `kocaeli iznitin koordinatini tasir`() {
        val kocaeli = all.first { it.name == "Kocaeli" }

        // İl merkezi İzmit'tir (Marmara'da); Balıkesir'deki "Kocaeli" köyü (39.6, 27.5) değildir.
        assertEquals(745028L, kocaeli.id)
        assertEquals(40.765, kocaeli.latitude, 0.01)
        assertEquals(29.929, kocaeli.longitude, 0.01)
    }

    @Test
    fun `merkezi farkli adli iller dogru yerdedir`() {
        assertEquals(36.207, all.first { it.name == "Hatay" }.latitude, 0.01) // Antakya
        assertEquals(40.781, all.first { it.name == "Sakarya" }.latitude, 0.01) // Adapazarı
        assertNotNull(all.firstOrNull { it.name == "Bursa" })
        assertNotNull(all.firstOrNull { it.name == "Samsun" })
    }

    @Test
    fun `iller turkce alfabe sirasindadir`() {
        val names = all.map { it.name }
        assertEquals("Adana", names.first())
        assertEquals("Zonguldak", names.last())
        // Ç, C'den sonra gelir (Burdur, Bursa, Çanakkale...), Isparta İstanbul'dan önce.
        assertTrue(names.indexOf("Bursa") < names.indexOf("Çanakkale"))
        assertTrue(names.indexOf("Çorum") < names.indexOf("Denizli"))
        assertTrue(names.indexOf("Isparta") < names.indexOf("İstanbul"))
    }

    @Test
    fun `bos arama tum illeri verir`() {
        assertEquals(all, TurkishProvinces.startingWith(""))
        assertEquals(all, TurkishProvinces.startingWith("   "))
    }

    @Test
    fun `yalnizca baslayan iller eslesir`() {
        // "an" ile başlayan: Ankara, Antalya. İçinde "an" geçenler (Bartın değil, "Kahramanmaraş" vb.) gelmez.
        val names = TurkishProvinces.startingWith("an").map { it.name }
        assertEquals(listOf("Ankara", "Antalya"), names)
    }

    @Test
    fun `eslestirme turkce karakterleri ve harf boyutunu yok sayar`() {
        assertEquals("Şanlıurfa", TurkishProvinces.startingWith("sanli").single().name)
        assertEquals("Çanakkale", TurkishProvinces.startingWith("CANAK").single().name)
        assertEquals("Iğdır", TurkishProvinces.startingWith("igd").single().name)
        assertEquals("Hakkâri", TurkishProvinces.startingWith("hakkari").single().name)
        assertEquals("Kocaeli", TurkishProvinces.startingWith("  Kocaeli ").single().name)
    }

    @Test
    fun `sadelestirme buyuk I ve noktali I ayni harf sayar`() {
        assertEquals(TurkishProvinces.fold("İstanbul"), TurkishProvinces.fold("Istanbul"))
        assertEquals(TurkishProvinces.fold("ISPARTA"), TurkishProvinces.fold("ısparta"))
    }
}
