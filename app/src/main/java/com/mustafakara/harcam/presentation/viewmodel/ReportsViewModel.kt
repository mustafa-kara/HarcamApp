package com.mustafakara.harcam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.data.repository.ExpenseRepository
import com.mustafakara.harcam.presentation.state.ReportPeriod
import com.mustafakara.harcam.presentation.state.ReportsUiState
import com.mustafakara.harcam.presentation.state.PeriodStats
import com.mustafakara.harcam.data.entity.ExpenseEntity
import java.util.Calendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Raporlar ekranı için ViewModel
 * ViewModel katmanı
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        startReactiveReports()
    }


    fun selectPeriod(period: ReportPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        calculatePeriodStats()
    }


    private fun startReactiveReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                repository.getAllExpenses()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Raporlar yüklenirken hata oluştu: ${exception.message}"
                        )
                    }
                    .collect { expenses ->
                        loadAllSummaries()
                        calculatePeriodStats()
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }

    //eski
    private fun loadReports() {
        viewModelScope.launch {
            loadAllSummaries()
            calculatePeriodStats()
        }
    }


    private suspend fun loadAllSummaries() {
        try {
            loadDailySummary()
            loadWeeklySummary()
            loadMonthlySummary()
            loadYearlySummary()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Veriler yüklenirken hata oluştu: ${e.message}"
            )
        }
    }


    private suspend fun loadDailySummary() {
        try {
            val dailySummaries = repository.getDailySummary()
            _uiState.value = _uiState.value.copy(dailySummaries = dailySummaries)
        } catch (e: Exception) {

            _uiState.value = _uiState.value.copy(dailySummaries = emptyList())
        }
    }


    private suspend fun loadWeeklySummary() {
        try {
            val weeklySummaries = repository.getWeeklySummary()
            _uiState.value = _uiState.value.copy(weeklySummaries = weeklySummaries)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(weeklySummaries = emptyList())
        }
    }


    private suspend fun loadMonthlySummary() {
        try {
            val monthlySummaries = repository.getMonthlySummary()
            _uiState.value = _uiState.value.copy(monthlySummaries = monthlySummaries)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(monthlySummaries = emptyList())
        }
    }


    private suspend fun loadYearlySummary() {
        try {
            val yearlySummaries = repository.getYearlySummary()
            _uiState.value = _uiState.value.copy(yearlySummaries = yearlySummaries)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(yearlySummaries = emptyList())
        }
    }


    fun refreshReports() {
        loadReports()
    }


    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }


    private fun calculatePeriodStats() {
        val currentState = _uiState.value
        
        val (periodStats, chartData, chartLabels) = when (currentState.selectedPeriod) {
            ReportPeriod.DAILY -> {
                val summaries = currentState.dailySummaries
                val stats = PeriodStats(
                    totalAmount = summaries.sumOf { it.totalAmount },
                    totalExpenses = summaries.sumOf { it.expenseCount },
                    averagePerPeriod = if (summaries.isNotEmpty()) 
                        summaries.sumOf { it.totalAmount } / summaries.size else 0.0,
                    periodCount = summaries.size,
                    periodName = "Günlük"
                )
                val data = summaries.takeLast(7).map { it.totalAmount }
                val labels = summaries.takeLast(7).map { 
                    try {
                        it.date.split("-").lastOrNull() ?: "?"
                    } catch (e: Exception) { "?" }
                }
                Triple(stats, data, labels)
            }
            ReportPeriod.WEEKLY -> {
                val summaries = currentState.weeklySummaries
                val stats = PeriodStats(
                    totalAmount = summaries.sumOf { it.totalAmount },
                    totalExpenses = summaries.sumOf { it.expenseCount },
                    averagePerPeriod = if (summaries.isNotEmpty()) 
                        summaries.sumOf { it.totalAmount } / summaries.size else 0.0,
                    periodCount = summaries.size,
                    periodName = "Haftalık"
                )
                val data = summaries.takeLast(6).map { it.totalAmount }
                val labels = summaries.takeLast(6).mapIndexed { index, _ -> 
                    "H${index + 1}"
                }
                Triple(stats, data, labels)
            }
            ReportPeriod.MONTHLY -> {
                val summaries = currentState.monthlySummaries
                val stats = PeriodStats(
                    totalAmount = summaries.sumOf { it.totalAmount },
                    totalExpenses = summaries.sumOf { it.expenseCount },
                    averagePerPeriod = if (summaries.isNotEmpty()) 
                        summaries.sumOf { it.totalAmount } / summaries.size else 0.0,
                    periodCount = summaries.size,
                    periodName = "Aylık"
                )
                val data = summaries.takeLast(6).map { it.totalAmount }
                val labels = summaries.takeLast(6).map { 
                    try {
                        it.month.split("-").lastOrNull() ?: "?"
                    } catch (e: Exception) { "?" }
                }
                Triple(stats, data, labels)
            }
            ReportPeriod.YEARLY -> {
                val summaries = currentState.yearlySummaries
                val stats = PeriodStats(
                    totalAmount = summaries.sumOf { it.totalAmount },
                    totalExpenses = summaries.sumOf { it.expenseCount },
                    averagePerPeriod = if (summaries.isNotEmpty()) 
                        summaries.sumOf { it.totalAmount } / summaries.size else 0.0,
                    periodCount = summaries.size,
                    periodName = "Yıllık"
                )
                val data = summaries.map { it.totalAmount }
                val labels = summaries.map { it.year }
                Triple(stats, data, labels)
            }
        }
        

        _uiState.value = _uiState.value.copy(
            periodStats = periodStats
        )

        viewModelScope.launch {
            val periodExpenses = when (currentState.selectedPeriod) {
                ReportPeriod.DAILY -> {

                    val today = System.currentTimeMillis()
                    val startOfDay = getStartOfDay(today)
                    val endOfDay = getEndOfDay(today)
                    getExpensesByDateRange(startOfDay, endOfDay)
                }
                ReportPeriod.WEEKLY -> {
                    val today = System.currentTimeMillis()
                    val startOfWeek = getStartOfWeek(today)
                    val endOfWeek = getEndOfWeek(today)
                    getExpensesByDateRange(startOfWeek, endOfWeek)
                }
                ReportPeriod.MONTHLY -> {
                    val today = System.currentTimeMillis()
                    val startOfMonth = getStartOfMonth(today)
                    val endOfMonth = getEndOfMonth(today)
                    getExpensesByDateRange(startOfMonth, endOfMonth)
                }
                ReportPeriod.YEARLY -> {
                    val today = System.currentTimeMillis()
                    val startOfYear = getStartOfYear(today)
                    val endOfYear = getEndOfYear(today)
                    getExpensesByDateRange(startOfYear, endOfYear)
                }
            }

            val realPeriodTotal = periodExpenses.sumOf { it.amount }
            val realExpenseCount = periodExpenses.size

            val updatedPeriodStats = periodStats.copy(
                totalAmount = realPeriodTotal,
                totalExpenses = realExpenseCount
            )

            _uiState.value = _uiState.value.copy(
                selectedPeriodExpenses = periodExpenses,
                periodStats = updatedPeriodStats
            )
        }

    }


    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Harcama silinirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
                hideEditExpenseDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Harcama güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }
    

    fun showEditExpenseDialog(expense: ExpenseEntity) {
        _uiState.value = _uiState.value.copy(editingExpense = expense)
    }

    fun hideEditExpenseDialog() {
        _uiState.value = _uiState.value.copy(editingExpense = null)
    }
    
    //eski
    fun refreshData() {
        loadReports()
    }


    private suspend fun getExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity> {
        return try {
            repository.getExpensesByDateRange(startDate, endDate).first()
        } catch (e: Exception) {
            emptyList()
        }
    }


    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfWeek(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun getStartOfMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun getStartOfYear(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfYear(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}

