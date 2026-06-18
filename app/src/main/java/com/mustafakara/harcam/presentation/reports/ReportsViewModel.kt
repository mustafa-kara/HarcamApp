package com.mustafakara.harcam.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustafakara.harcam.domain.model.ReportPeriod
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.GetPeriodReportUseCase
import com.mustafakara.harcam.domain.usecase.ObserveCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Reports ViewModel — the period drives [GetPeriodReportUseCase]; changing the tab re-aggregates
 * reactively (architecture.md §2). Joins with categories + currency for the charts/legend.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getPeriodReport: GetPeriodReportUseCase,
    observeCategories: ObserveCategoriesUseCase,
    preferences: PreferencesRepository,
) : ViewModel() {

    private val period = MutableStateFlow(ReportPeriod.MONTH)

    val uiState: StateFlow<ReportsUiState> = combine(
        period,
        period.flatMapLatest { getPeriodReport(it) },
        observeCategories(),
        preferences.observe(),
    ) { selectedPeriod, stats, categories, prefs ->
        ReportsUiState(
            isLoading = false,
            period = selectedPeriod,
            currency = prefs.currency,
            stats = stats,
            categoriesById = categories.associateBy { it.id },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReportsUiState(isLoading = true),
    )

    fun selectPeriod(value: ReportPeriod) {
        period.value = value
    }
}
