package com.kampplus.hava.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kampplus.hava.R
import com.kampplus.hava.ui.cities.CitiesRoute
import com.kampplus.hava.ui.cities.CitiesViewModel
import com.kampplus.hava.ui.city.CityRoute
import com.kampplus.hava.ui.component.HavaBottomBar
import com.kampplus.hava.ui.component.HavaTab
import com.kampplus.hava.ui.theme.WeatherTheme
import com.kampplus.hava.ui.today.TodayRoute
import kotlinx.coroutines.launch

/**
 * Uygulamanın ekranları ve aralarındaki geçiş.
 *
 * Dört sekme alt gezinmede durur: Bugün, Şehirler, Harita, Ayarlar ([HavaTab]). Beşinci ekran
 * "şehir ekle" (Open-Meteo şehir araması) bir sekme değildir; alt çubuk onun üstünde gizlenir
 * ve sistem geri tuşu önceki ekrana döner.
 *
 * Sekmeler arası geçişte her sekmenin durumu (kaydırma, seçili saat...) saklanır ve geri gelince
 * yüklenir; böylece sekme değiştirince veri baştan istenmez.
 */
@Composable
fun HavaNavHost(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = HavaTab.entries.firstOrNull { it.route == backStackEntry?.destination?.route }

    Scaffold(
        modifier = modifier,
        containerColor = WeatherTheme.colors.bg,
        // Ekranlar durum çubuğu boşluğunu kendileri yönetir; alt çubuk gezinme çubuğunu kendisi yönetir.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentTab != null) {
                HavaBottomBar(selected = currentTab, onSelect = { navController.navigateToTab(it) })
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HavaTab.TODAY.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HavaTab.TODAY.route) {
                TodayRoute(onSearchClick = { navController.navigate(ADD_CITY_ROUTE) })
            }
            composable(HavaTab.CITIES.route) {
                CitiesRoute(
                    onAddClick = { navController.navigate(ADD_CITY_ROUTE) },
                    onCityClick = { navController.navigateToTab(HavaTab.TODAY) }
                )
            }
            composable(HavaTab.MAP.route) {
                ComingSoonScreen(title = R.string.tab_map, icon = HavaTab.MAP.icon)
            }
            composable(HavaTab.SETTINGS.route) {
                ComingSoonScreen(title = R.string.tab_settings, icon = HavaTab.SETTINGS.icon)
            }
            composable(ADD_CITY_ROUTE) {
                // Bu ViewModel burada yalnızca şehri kaydetmek için kullanılır; liste dinlenmediği için ağ isteği atmaz.
                val citiesViewModel: CitiesViewModel = viewModel(factory = CitiesViewModel.Factory)
                val scope = rememberCoroutineScope()
                CityRoute(
                    onCityClick = { city ->
                        // Önce kaydın bitmesini bekle, sonra ekrandan çık: ViewModel kapanırsa yazma yarıda kalırdı.
                        scope.launch {
                            citiesViewModel.addAndSelect(city)
                            navController.finishAddCity()
                        }
                    }
                )
            }
        }
    }
}

/** Şehir ekleme akışının adresi; alt gezinmede yer almaz. */
private const val ADD_CITY_ROUTE = "add_city"

/**
 * Şehir ekleme akışını bitirir ve Bugün ekranına döner.
 *
 * Akış, Bugün ya da Şehirler sekmesinin **üstüne** bindirilmiştir; bu yüzden sekme geçişi
 * ([navigateToTab]) yerine Bugün'e kadar geri sarılır. (Hedefle `popUpTo` aynı ekran olunca sekme geçişi
 * bu akıştan çıkaramıyordu.)
 */
private fun NavHostController.finishAddCity() {
    if (!popBackStack(HavaTab.TODAY.route, inclusive = false)) navigateToTab(HavaTab.TODAY)
}

/**
 * Bir sekmeye geçer: başlangıç ekranına kadar yığını temizler, her sekmenin durumunu saklar ve
 * geri geldiğinde yükler, aynı sekmeye tekrar basınca kopya açmaz.
 */
private fun NavHostController.navigateToTab(tab: HavaTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
