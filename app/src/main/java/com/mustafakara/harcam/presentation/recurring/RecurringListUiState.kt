package com.mustafakara.harcam.presentation.recurring

import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.RecurringExpense

/** Recurring list state — recurring_list.md §7; four-state via [isLoading]/[isEmpty]. */
data class RecurringListUiState(
    val isLoading: Boolean = true,
    val currency: Currency = Currency.TRY,
    val items: List<RecurringExpense> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}
