package com.mustafakara.harcam.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.util.DateUtil
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.ObserveBudgetStatusUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import com.mustafakara.harcam.domain.usecase.ObserveExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Dashboard ViewModel — composes the month summary, monthly budget status, recent expenses, and
 * categories into one reactive [DashboardUiState] (architecture.md §2/§4). All reads observe Room,
 * so adding an expense elsewhere updates the dashboard automatically.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeExpenses: ObserveExpensesUseCase,
    observeCategories: ObserveCategoriesUseCase,
    observeBudgetStatus: ObserveBudgetStatusUseCase,
    preferences: PreferencesRepository,
    clock: Clock,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        observeExpenses(),
        observeCategories(),
        observeBudgetStatus(),
        preferences.observe(),
    ) { expenses, categories, budgetOverview, prefs ->
        val now = clock.nowMs()
        val monthStart = DateUtil.startOfMonth(now)
        val monthEnd = DateUtil.endOfMonth(now)
        val monthExpenses = expenses.filter { it.createdAt in monthStart..monthEnd }

        DashboardUiState(
            isLoading = false,
            currency = prefs.currency,
            monthSpent = monthExpenses.sumOf { it.amount },
            monthIncome = 0.0, // income tracking is a Phase 3+ concept; expenses are the focus
            monthlyBudget = budgetOverview.monthly,
            recent = expenses.take(RECENT_LIMIT),
            categoriesById = categories.associateBy { it.id },
            maskAmounts = prefs.maskAmounts,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = DashboardUiState(isLoading = true),
    )

    private companion object {
        const val RECENT_LIMIT = 5
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
