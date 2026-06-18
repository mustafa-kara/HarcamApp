package com.mustafakara.harcam.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.ObserveBudgetStatusUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Budget overview ViewModel — composes the monthly budget status, per-category statuses, the
 * category lookup, and the user's currency into one reactive [BudgetUiState] (budget_overview.md
 * §7, architecture.md §2/§4). All reads observe Room, so adding an expense elsewhere recomputes
 * the bars automatically.
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    observeBudgetStatus: ObserveBudgetStatusUseCase,
    observeCategories: ObserveCategoriesUseCase,
    preferences: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<BudgetUiState> = combine(
        observeBudgetStatus(),
        observeCategories(),
        preferences.observe(),
    ) { budgetOverview, categories, prefs ->
        val categoriesById = categories.associateBy { it.id }
        val rows = budgetOverview.perCategory.mapNotNull { status ->
            val category = categoriesById[status.categoryId] ?: return@mapNotNull null
            CategoryBudgetRow(category = category, status = status)
        }

        BudgetUiState(
            isLoading = false,
            currency = prefs.currency,
            monthly = budgetOverview.monthly,
            categoryRows = rows,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = BudgetUiState(isLoading = true),
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
