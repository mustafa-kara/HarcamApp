package com.mustafakara.harcam.core.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mustafakara.harcam.presentation.auth.LockGateViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mustafakara.harcam.presentation.auth.LockScreen
import com.mustafakara.harcam.presentation.auth.OnboardingScreen
import com.mustafakara.harcam.presentation.auth.SplashDestination
import com.mustafakara.harcam.presentation.auth.SplashScreen
import com.mustafakara.harcam.presentation.budget.BudgetScreen
import com.mustafakara.harcam.presentation.budget.EditBudgetScreen
import com.mustafakara.harcam.presentation.categories.CategoryDetailScreen
import com.mustafakara.harcam.presentation.categories.CategoryListScreen
import com.mustafakara.harcam.presentation.dashboard.DashboardScreen
import com.mustafakara.harcam.presentation.exchange.ExchangeScreen
import com.mustafakara.harcam.presentation.expenses.AddEditExpenseScreen
import com.mustafakara.harcam.presentation.expenses.ExpenseListScreen
import com.mustafakara.harcam.presentation.recurring.AddEditRecurringScreen
import com.mustafakara.harcam.presentation.recurring.RecurringListScreen
import com.mustafakara.harcam.presentation.reports.ReportsScreen
import com.mustafakara.harcam.presentation.settings.SettingsScreen

/**
 * Root navigation (architecture.md §6). The five bottom-nav destinations keep independent back
 * stacks; secondary screens (categories, detail, budget edit, add/edit expense) sit outside the
 * bottom bar. Auth (splash/onboarding/lock) sits outside the bottom-nav graph; on resume from
 * background the host re-routes to `lock` when the app lock is enabled.
 */
@Composable
fun HarcamNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val topLevelRoutes = TopLevelDestination.entries.map { it.route }
    val showBottomBar = currentRoute?.hierarchy?.any { it.route in topLevelRoutes } == true

    // Re-lock on resume from background (pin_lock.md §2). Auth routes are exempt.
    val lockGate: LockGateViewModel = hiltViewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    val authRoutes = setOf(Routes.SPLASH, Routes.ONBOARDING, Routes.LOCK)
    DisposableEffect(lifecycleOwner, currentRoute?.route) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                currentRoute?.route !in authRoutes &&
                lockGate.shouldRelock()
            ) {
                navController.navigate(Routes.LOCK) { popUpTo(Routes.LOCK) { inclusive = true } }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                FloatingNavBar(
                    destinations = TopLevelDestination.entries,
                    isSelected = { dest ->
                        currentRoute?.hierarchy?.any { it.route == dest.route } == true
                    },
                    onSelect = { dest ->
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onDestination = { dest ->
                        val target = when (dest) {
                            SplashDestination.ONBOARDING -> Routes.ONBOARDING
                            SplashDestination.LOCK -> Routes.LOCK
                            SplashDestination.DASHBOARD -> Routes.DASHBOARD
                        }
                        navController.navigate(target) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onDone = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.LOCK) {
                LockScreen(
                    onUnlocked = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOCK) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onAddExpense = { navController.navigate(Routes.expenseEdit()) },
                    onOpenBudget = { navController.navigate(Routes.BUDGET) },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                    onOpenExchange = { navController.navigate(Routes.EXCHANGE) },
                )
            }

            composable(Routes.EXPENSES) {
                ExpenseListScreen(
                    onAddExpense = { navController.navigate(Routes.expenseEdit()) },
                    onEditExpense = { id -> navController.navigate(Routes.expenseEdit(id)) },
                )
            }

            composable(
                route = Routes.EXPENSE_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L }),
            ) {
                AddEditExpenseScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(Routes.REPORTS) { ReportsScreen() }

            composable(Routes.BUDGET) {
                BudgetScreen(onEdit = { navController.navigate(Routes.BUDGET_EDIT) })
            }
            composable(Routes.BUDGET_EDIT) {
                EditBudgetScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(Routes.CATEGORIES) {
                CategoryListScreen(
                    onBack = { navController.popBackStack() },
                    onOpenDetail = { id -> navController.navigate(Routes.categoryDetail(id)) },
                )
            }
            composable(
                route = Routes.CATEGORY_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) {
                CategoryDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                    onOpenRecurring = { navController.navigate(Routes.RECURRING) },
                    onOpenExchange = { navController.navigate(Routes.EXCHANGE) },
                )
            }
            composable(Routes.EXCHANGE) {
                ExchangeScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.RECURRING) {
                RecurringListScreen(
                    onBack = { navController.popBackStack() },
                    onAdd = { navController.navigate(Routes.recurringEdit()) },
                    onEdit = { id -> navController.navigate(Routes.recurringEdit(id)) },
                )
            }
            composable(
                route = Routes.RECURRING_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L }),
            ) {
                AddEditRecurringScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
        }
    }
}
