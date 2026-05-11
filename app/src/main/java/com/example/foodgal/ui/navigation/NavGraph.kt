package com.example.foodgal.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.foodgal.models.Transaction
import com.example.foodgal.ui.auth.AuthViewModel
import com.example.foodgal.ui.screens.LoginScreen
import com.example.foodgal.ui.auth.ProfileScreen
import com.example.foodgal.ui.component.AppSidebar
import com.example.foodgal.ui.pos.*
import com.example.foodgal.ui.screens.CheckoutScreen
import com.example.foodgal.ui.screens.HistoryScreen
import com.example.foodgal.ui.screens.PosScreen
import com.example.foodgal.ui.screens.ProductListScreen
import com.example.foodgal.ui.screens.ReceiptScreen
import com.example.foodgal.ui.screens.SuccessScreen
import com.example.foodgal.ui.screens.SummaryScreen
import com.example.foodgal.ui.screens.TransactionDetailScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Login")
    object POS : Screen("pos", "POS")
    object ProductList : Screen("product_list", "Daftar Product")
    object History : Screen("history", "History Transaction")
    object TransactionDetail : Screen("transaction_detail", "Transaction Detail")
    object Summary : Screen("summary", "Summary")
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
    val historyViewModel: HistoryViewModel = viewModel()
    val summaryViewModel: SummaryViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Urutan harus SAMA dengan AppSidebar.kt
    val navigationItems = listOf(
        Screen.POS,
        Screen.History,
        Screen.Summary,
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
        gesturesEnabled = currentRoute != Screen.Login.route && currentRoute != Screen.Success.route
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
            composable(Screen.History.route) {
                HistoryScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onTransactionClick = { transaction ->
                        selectedTransaction = transaction
                        navController.navigate(Screen.TransactionDetail.route)
                    },
                    viewModel = historyViewModel
                )
            }
            composable(Screen.TransactionDetail.route) {
                selectedTransaction?.let { transaction ->
                    TransactionDetailScreen(
                        transaction = transaction,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Summary.route) {
                SummaryScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    viewModel = summaryViewModel
                )
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
