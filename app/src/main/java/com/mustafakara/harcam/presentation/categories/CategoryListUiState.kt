package com.mustafakara.harcam.presentation.categories

import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.usecase.CategoryWithSpend

/** Immutable category-list state — category_list.md §7, four-state mapping via [isLoading]. */
data class CategoryListUiState(
    val isLoading: Boolean = true,
    val currency: Currency = Currency.TRY,
    val items: List<CategoryWithSpend> = emptyList(),
    val editing: CategoryEditState? = null,
    val deleteTarget: Category? = null,
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}

/** Add/edit sheet state — null [CategoryListUiState.editing] means the sheet is closed. */
data class CategoryEditState(
    val id: Long? = null,
    val name: String = "",
    val colorKey: String = "other",
    val iconKey: String = "category",
    val nameError: Boolean = false,
)
