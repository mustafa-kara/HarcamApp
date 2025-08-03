package com.mustafakara.harcam.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustafakara.harcam.data.dao.DailySummary
import com.mustafakara.harcam.data.dao.MonthlySummary
import com.mustafakara.harcam.data.dao.WeeklySummary
import com.mustafakara.harcam.data.dao.YearlySummary
import com.mustafakara.harcam.data.entity.ExpenseEntity
import com.mustafakara.harcam.presentation.dialogs.EditExpenseDialog
import com.mustafakara.harcam.presentation.state.ReportPeriod
import com.mustafakara.harcam.presentation.state.ReportsUiState
import com.mustafakara.harcam.presentation.state.PeriodStats
import com.mustafakara.harcam.presentation.viewmodel.ReportsViewModel
import com.mustafakara.harcam.presentation.dialogs.EditExpenseDialog
import com.mustafakara.harcam.ui.theme.HarcamTheme
import com.mustafakara.harcam.ui.theme.ExpenseBlue
import com.mustafakara.harcam.ui.theme.ExpenseGreen
import com.mustafakara.harcam.ui.theme.ExpenseOrange
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Raporlar ekranı
 * View katmanı
 */
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        ReportsContent(
            uiState = uiState,
            onPeriodSelected = viewModel::selectPeriod,
            onDeleteExpense = viewModel::deleteExpense,
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues)
        )
        
        // EditDialog
        uiState.editingExpense?.let { expense ->
            EditExpenseDialog(
                expense = expense,
                onDismiss = { viewModel.hideEditExpenseDialog() },
                onUpdateExpense = { updatedExpense ->
                    viewModel.updateExpense(updatedExpense)
                }
            )
        }
    }
}

@Composable
private fun ReportsContent(
    uiState: ReportsUiState,
    onPeriodSelected: (ReportPeriod) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = ExpenseBlue,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Harcama Raporları",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseBlue
                )
            }
        }
        
        item {
            // Dönem
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            item {
                StatsCards(
                    periodStats = uiState.periodStats,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.selectedPeriodExpenses.isNotEmpty()) {
                item {
                    PeriodExpensesList(
                        expenses = uiState.selectedPeriodExpenses,
                        periodName = uiState.periodStats.periodName,
                        onDeleteExpense = onDeleteExpense,
                        onEditExpense = { expense -> viewModel.showEditExpenseDialog(expense) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "${uiState.periodStats.periodName} dönemde harcama bulunamadı",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            item {
                PeriodTotalCard(
                    periodStats = uiState.periodStats,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: ReportPeriod,
    onPeriodSelected: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(ReportPeriod.values()) { period ->
            FilterChip(
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName) },
                selected = selectedPeriod == period,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ExpenseBlue,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun StatsCards(
    periodStats: PeriodStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Toplam
        StatsCard(
            title = "Toplam",
            value = formatCurrency(periodStats.totalAmount),
            icon = Icons.Default.TrendingUp,
            modifier = Modifier.weight(1f)
        )
        
        // Ortalama
        StatsCard(
            title = "${periodStats.periodName}\nOrtalama",
            value = formatCurrency(periodStats.averagePerPeriod),
            icon = Icons.Default.BarChart,
            modifier = Modifier.weight(1f)
        )
        
        // Toplam harcama
        StatsCard(
            title = "Toplam\nHarcama",
            value = periodStats.totalExpenses.toString(),
            icon = Icons.Default.TrendingDown,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when (title) {
                    "Toplam" -> ExpenseGreen
                    else -> if (title.contains("Ortalama")) ExpenseBlue else ExpenseOrange
                },
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MinimalistChartSection(
    data: List<Double>,
    labels: List<String>,
    periodName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "$periodName Trend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = ExpenseBlue
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            MinimalistBarChart(
                data = data,
                labels = labels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

@Composable
private fun MinimalistBarChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Henüz veri yok",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }
    
    val maxValue = data.maxOrNull() ?: 1.0
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.zip(labels).forEachIndexed { index, (value, label) ->
            val barHeight = if (maxValue > 0) (value / maxValue * 80).toInt() else 0
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(barHeight.dp)
                        .background(
                            color = ExpenseBlue.copy(alpha = 0.7f + (index * 0.1f)),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Label
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun ReportsList(
    uiState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    when (uiState.selectedPeriod) {
        ReportPeriod.DAILY -> {
            if (uiState.dailySummaries.isEmpty()) {
                EmptyReportsMessage("Günlük harcama verisi bulunamadı")
            } else {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.dailySummaries) { summary ->
                        DailyReportItem(summary = summary)
                    }
                }
            }
        }
        ReportPeriod.WEEKLY -> {
            if (uiState.weeklySummaries.isEmpty()) {
                EmptyReportsMessage("Haftalık harcama verisi bulunamadı")
            } else {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.weeklySummaries) { summary ->
                        WeeklyReportItem(summary = summary)
                    }
                }
            }
        }
        ReportPeriod.MONTHLY -> {
            if (uiState.monthlySummaries.isEmpty()) {
                EmptyReportsMessage("Aylık harcama verisi bulunamadı")
            } else {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.monthlySummaries) { summary ->
                        MonthlyReportItem(summary = summary)
                    }
                }
            }
        }
        ReportPeriod.YEARLY -> {
            if (uiState.yearlySummaries.isEmpty()) {
                EmptyReportsMessage("Yıllık harcama verisi bulunamadı")
            } else {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.yearlySummaries) { summary ->
                        YearlyReportItem(summary = summary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyReportItem(
    summary: DailySummary,
    modifier: Modifier = Modifier
) {
    ReportItemCard(
        period = formatDate(summary.date),
        amount = summary.totalAmount,
        expenseCount = summary.expenseCount,
        modifier = modifier
    )
}

@Composable
private fun WeeklyReportItem(
    summary: WeeklySummary,
    modifier: Modifier = Modifier
) {
    ReportItemCard(
        period = summary.week,
        amount = summary.totalAmount,
        expenseCount = summary.expenseCount,
        modifier = modifier
    )
}

@Composable
private fun MonthlyReportItem(
    summary: MonthlySummary,
    modifier: Modifier = Modifier
) {
    ReportItemCard(
        period = formatMonth(summary.month),
        amount = summary.totalAmount,
        expenseCount = summary.expenseCount,
        modifier = modifier
    )
}

@Composable
private fun YearlyReportItem(
    summary: YearlySummary,
    modifier: Modifier = Modifier
) {
    ReportItemCard(
        period = "${summary.year} Yılı",
        amount = summary.totalAmount,
        expenseCount = summary.expenseCount,
        modifier = modifier
    )
}

@Composable
private fun ReportItemCard(
    period: String,
    amount: Double,
    expenseCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = ExpenseGreen.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = ExpenseGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = period,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$expenseCount harcama",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ExpenseOrange
            )
        }
    }
}

@Composable
private fun PeriodExpensesList(
    expenses: List<ExpenseEntity>,
    periodName: String,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$periodName Harcamaları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseBlue
                )
                Text(
                    text = "${expenses.size} adet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Harcama listesi
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(
                    items = expenses,
                    key = { it.id }
                ) { expense ->
                    PeriodExpenseItem(
                        expense = expense,
                        onDeleteExpense = onDeleteExpense,
                        onEditExpense = onEditExpense
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodExpenseItem(
    expense: ExpenseEntity,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Harcama bilgileri
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(expense.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Tutar ve butonlar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatCurrency(expense.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseOrange
                )
                
                // Düzenle butonu
                IconButton(
                    onClick = { onEditExpense(expense) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = ExpenseBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Sil butonu
                IconButton(
                    onClick = { onDeleteExpense(expense) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = ExpenseOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodTotalCard(
    periodStats: PeriodStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = ExpenseGreen.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Üst satır: Icon + Dönem adı
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = ExpenseGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${getPeriodDisplayName(periodStats.periodName)} Toplam:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Alt satır: Toplam tutar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatCurrency(periodStats.totalAmount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseGreen,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


private fun getPeriodDisplayName(periodName: String): String {
    return when (periodName) {
        "Günlük" -> "Gün"
        "Haftalık" -> "Hafta"
        "Aylık" -> "Ay"
        "Yıllık" -> "Yıl"
        else -> periodName
    }
}

@Composable
private fun EmptyReportsMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// fonksiyonlar
private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    return formatter.format(amount)
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

private fun formatMonth(monthString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM yyyy", Locale("tr", "TR"))
        val date = inputFormat.parse(monthString)
        date?.let { outputFormat.format(it) } ?: monthString
    } catch (e: Exception) {
        monthString
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    HarcamTheme {
                                ReportsContent(
            uiState = ReportsUiState(
                selectedPeriod = ReportPeriod.DAILY,
                dailySummaries = listOf(
                    DailySummary("2024-01-15", 125.50, 3),
                    DailySummary("2024-01-14", 89.75, 2),
                    DailySummary("2024-01-13", 200.00, 5)
                ),
                            periodStats = PeriodStats(
                totalAmount = 415.25,
                totalExpenses = 3,
                averagePerPeriod = 138.42,
                periodCount = 3,
                periodName = "Günlük"
            ),
            selectedPeriodExpenses = listOf(
                ExpenseEntity(
                    id = 1,
                    description = "Market alışverişi",
                    amount = 125.50,
                    createdAt = System.currentTimeMillis()
                ),
                ExpenseEntity(
                    id = 2,
                    description = "Benzin",
                    amount = 89.75,
                    createdAt = System.currentTimeMillis() - 86400000
                ),
                ExpenseEntity(
                    id = 3,
                    description = "Restoran",
                    amount = 200.00,
                    createdAt = System.currentTimeMillis() - 172800000
                )
            )
                    ),
        onPeriodSelected = {},
        onDeleteExpense = {},
        viewModel = null!!
        )
    }
}