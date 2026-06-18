package com.mustafakara.harcam.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation Compose routes — must match architecture.md §6 verbatim. Centralized here so the
 * NavHost, bottom nav, and deep links share one source of truth.
 */
object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOCK = "lock"

    const val DASHBOARD = "dashboard"
    const val EXPENSES = "expenses"
    const val REPORTS = "reports"
    const val BUDGET = "budget"
    const val SETTINGS = "settings"

    const val BUDGET_EDIT = "budget/edit"
    const val CATEGORIES = "categories"
    const val CATEGORY_DETAIL = "categories/{id}"
    const val RECURRING = "recurring"
    const val EXCHANGE = "exchange"

    // Parameterized create/edit routes.
    const val EXPENSE_EDIT = "expenses/edit?id={id}"
    const val RECURRING_EDIT = "recurring/edit?id={id}"

    fun expenseEdit(id: Long? = null) = if (id == null) "expenses/edit" else "expenses/edit?id=$id"
    fun recurringEdit(id: Long? = null) = if (id == null) "recurring/edit" else "recurring/edit?id=$id"
    fun categoryDetail(id: Long) = "categories/$id"
}

/** The five bottom-navigation destinations — design.md §9 (≤5, labels always shown). */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DASHBOARD(Routes.DASHBOARD, "Home", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    EXPENSES(Routes.EXPENSES, "Expenses", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    REPORTS(Routes.REPORTS, "Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BUDGET(Routes.BUDGET, "Budget", Icons.Filled.Savings, Icons.Outlined.Savings),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
}
