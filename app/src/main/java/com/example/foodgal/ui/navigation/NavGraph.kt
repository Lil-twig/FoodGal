package com.example.foodgal.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.foodgal.ui.component.AppSidebar
import com.example.foodgal.ui.pos.CheckoutScreen
import com.example.foodgal.ui.pos.PosScreen
import com.example.foodgal.ui.pos.PosViewModel
import com.example.foodgal.ui.pos.ProductListScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String) {
    object POS : Screen("pos", "POS")
    object ProductList : Screen("product_list", "Daftar Product")
    object Profile : Screen("profile", "Profile")
    object Settings : Screen("settings", "Settings")
    object Checkout : Screen("checkout", "Checkout")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Shared ViewModel for POS and Checkout
    val posViewModel: PosViewModel = viewModel()

    val navigationItems = listOf(
        Screen.POS,
        Screen.ProductList,
        Screen.Profile,
        Screen.Settings
    )

    val selectedItemIndex = navigationItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppSidebar(
                selectedItemIndex = selectedItemIndex,
                onItemClick = { index ->
                    val screen = navigationItems[index]
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.POS.route
        ) {
            composable(Screen.POS.route) {
                PosScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onCartClick = { navController.navigate(Screen.Checkout.route) },
                    viewModel = posViewModel
                )
            }
            composable(Screen.ProductList.route) {
                ProductListScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = posViewModel
                )
            }
            composable(Screen.Profile.route) { }
            composable(Screen.Settings.route) { }
        }
    }
}
