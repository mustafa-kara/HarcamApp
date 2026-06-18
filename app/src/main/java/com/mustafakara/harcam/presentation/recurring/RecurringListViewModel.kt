package com.mustafakara.harcam.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.DeleteRecurringUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import com.mustafakara.harcam.domain.usecase.ObserveRecurringUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Recurring list — observes templates + categories + currency (recurring_list.md §7). */
@HiltViewModel
class RecurringListViewModel @Inject constructor(
    observeRecurring: ObserveRecurringUseCase,
    observeCategories: ObserveCategoriesUseCase,
    preferences: PreferencesRepository,
    private val deleteRecurring: DeleteRecurringUseCase,
) : ViewModel() {

    val uiState: StateFlow<RecurringListUiState> = combine(
        observeRecurring(),
        observeCategories(),
        preferences.observe(),
    ) { items, categories, prefs ->
        RecurringListUiState(
            isLoading = false,
            currency = prefs.currency,
            items = items.sortedBy { it.nextDueDate },
            categoriesById = categories.associateBy { it.id },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecurringListUiState(isLoading = true),
    )

    fun delete(id: Long) {
        viewModelScope.launch { deleteRecurring(id) }
    }
}
