package com.mustafakara.harcam.presentation

import app.cash.turbine.test
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.usecase.AddExpenseUseCase
import com.mustafakara.harcam.domain.usecase.DeleteExpenseUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import com.mustafakara.harcam.domain.usecase.ObserveExpensesUseCase
import com.mustafakara.harcam.fakes.FakeCategoryRepository
import com.mustafakara.harcam.fakes.FakeClock
import com.mustafakara.harcam.fakes.FakeExpenseRepository
import com.mustafakara.harcam.fakes.FakePreferencesRepository
import com.mustafakara.harcam.presentation.expenses.ExpenseListViewModel
import com.mustafakara.harcam.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseListViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val now = 1_700_000_000_000L
    private fun expense(id: Long, categoryId: Long) =
        Expense(id, amount = 10.0, currency = Currency.TRY, categoryId = categoryId, note = "", createdAt = now)

    private fun viewModel(
        expenses: FakeExpenseRepository,
        categories: FakeCategoryRepository = FakeCategoryRepository(
            listOf(
                Category(1, "Food", "food", "restaurant", true),
                Category(2, "Transport", "transport", "directions_bus", true),
            ),
        ),
    ): ExpenseListViewModel {
        val prefs = FakePreferencesRepository()
        return ExpenseListViewModel(
            observeExpenses = ObserveExpensesUseCase(expenses),
            observeCategories = ObserveCategoriesUseCase(categories),
            preferences = prefs,
            addExpense = AddExpenseUseCase(expenses, FakeClock(now)),
            deleteExpense = DeleteExpenseUseCase(expenses),
        )
    }

    @Test
    fun `emits loading then success`() = runTest {
        val vm = viewModel(FakeExpenseRepository(listOf(expense(1, 1))))
        vm.uiState.test {
            assertTrue(awaitItem().isLoading) // initial
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(1, loaded.days.sumOf { it.items.size })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `category filter narrows the list`() = runTest {
        val repo = FakeExpenseRepository(listOf(expense(1, 1), expense(2, 1), expense(3, 2)))
        val vm = viewModel(repo)
        vm.uiState.test {
            // Drain to the loaded (3-item) state.
            var loaded = awaitItem()
            while (loaded.isLoading || loaded.days.sumOf { it.items.size } != 3) loaded = awaitItem()

            vm.selectCategory(2)

            var filtered = awaitItem()
            while (filtered.selectedCategoryId != 2L) filtered = awaitItem()
            assertEquals(1, filtered.days.sumOf { d -> d.items.size })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty repository reports empty state`() = runTest {
        val vm = viewModel(FakeExpenseRepository())
        vm.uiState.test {
            awaitItem() // loading
            val loaded = awaitItem()
            assertTrue(loaded.isEmpty)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
