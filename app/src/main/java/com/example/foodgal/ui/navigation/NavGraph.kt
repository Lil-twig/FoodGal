package com.example.foodgal.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.foodgal.ui.auth.AuthViewModel
import com.example.foodgal.ui.auth.LoginScreen
import com.example.foodgal.ui.auth.ProfileScreen
import com.example.foodgal.ui.component.AppSidebar
import com.example.foodgal.ui.pos.CheckoutScreen
import com.example.foodgal.ui.pos.PosScreen
import com.example.foodgal.ui.pos.PosViewModel
import com.example.foodgal.ui.pos.ProductListScreen
import com.example.foodgal.ui.pos.ReceiptScreen
import com.example.foodgal.ui.pos.SuccessScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object POS : Screen("pos", "POS")
    object ProductList : Screen("product_list", "Daftar Product")
    object Profile : Screen("profile", "Profile")
    object Settings : Screen("settings", "Settings")
    object Checkout : Screen("checkout", "Checkout")
    object Success : Screen("success", "Success")
    object Receipt : Screen("receipt", "Receipt")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val authViewModel: AuthViewModel = viewModel()
    val posViewModel: PosViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

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
        },
        gesturesEnabled = currentRoute != Screen.Login.route
    ) {
        NavHost(
            navController = navController,
            startDestination = if (currentUser == null) Screen.Login.route else Screen.POS.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.POS.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }
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
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    viewModel = authViewModel
                )
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = posViewModel,
                    onComplete = { navController.navigate(Screen.Success.route) }
                )
            }
            composable(Screen.Success.route) {
                SuccessScreen(
                    onComplete = {
                        navController.navigate(Screen.Receipt.route) {
                            popUpTo(Screen.Checkout.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Receipt.route) {
                ReceiptScreen(
                    viewModel = posViewModel,
                    onDoneClick = {
                        posViewModel.clearCart()
                        navController.navigate(Screen.POS.route) {
                            popUpTo(Screen.POS.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Settings.route) { }
        }
    }
}
