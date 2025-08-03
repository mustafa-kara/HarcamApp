package com.mustafakara.harcam.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.clip
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.mustafakara.harcam.data.entity.ExpenseEntity
import com.mustafakara.harcam.presentation.components.AddExpenseDialog
import com.mustafakara.harcam.presentation.dialogs.EditExpenseDialog
import com.mustafakara.harcam.presentation.state.AddExpenseUiState
import com.mustafakara.harcam.presentation.state.ExpenseUiState
import com.mustafakara.harcam.presentation.viewmodel.ExpenseViewModel
import com.mustafakara.harcam.ui.theme.HarcamTheme
import com.mustafakara.harcam.ui.theme.SunsetGradient
import com.mustafakara.harcam.ui.theme.ForestGradient
import com.mustafakara.harcam.ui.theme.ExpenseBlue
import com.mustafakara.harcam.ui.theme.ExpenseGreen
import com.mustafakara.harcam.ui.theme.ExpenseOrange
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ana ekran
 * View katmanı
 */
@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val addExpenseState by viewModel.addExpenseState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.showAddExpenseDialog() },
                    containerColor = ExpenseBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Harcama Ekle",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        ExpenseContent(
            uiState = uiState,
            addExpenseState = addExpenseState,
            onDeleteExpense = viewModel::deleteExpense,
            onUpdateExpense = viewModel::updateExpense,
            onHideAddDialog = viewModel::hideAddExpenseDialog,
            onShowEditDialog = viewModel::showEditExpenseDialog,
            onHideEditDialog = viewModel::hideEditExpenseDialog,
            onDescriptionChange = viewModel::updateDescription,
            onAmountChange = viewModel::updateAmount,
            onAddExpense = viewModel::addExpense,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun ExpenseContent(
    uiState: ExpenseUiState,
    addExpenseState: AddExpenseUiState,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    onHideAddDialog: () -> Unit,
    onShowEditDialog: (ExpenseEntity) -> Unit,
    onHideEditDialog: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = ExpenseBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Bugünün Harcamaları",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = ExpenseBlue
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toplam tutar kartı
        TotalAmountCard(
            totalAmount = uiState.totalAmount,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Harcama listesi animasyonlu
            AnimatedVisibility(
                visible = uiState.expenses.isEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                EmptyExpenseList(
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            AnimatedVisibility(
                visible = uiState.expenses.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                ExpenseList(
                    expenses = uiState.expenses,
                    onDeleteExpense = onDeleteExpense,
                    onEditExpense = onShowEditDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                )
            }
        }
    }

    if (uiState.isAddExpenseDialogVisible) {
        AddExpenseDialog(
            addExpenseState = addExpenseState,
            onDescriptionChange = onDescriptionChange,
            onAmountChange = onAmountChange,
            onConfirm = onAddExpense,
            onDismiss = onHideAddDialog
        )
    }
    
    // EditExpenseDialog
    uiState.editingExpense?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            onDismiss = onHideEditDialog,
            onUpdateExpense = onUpdateExpense
        )
    }
}

@Composable
private fun TotalAmountCard(
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = SunsetGradient,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bugünün Toplam Harcaması",
                        style = MaterialTheme.typography.titleMedium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = formatCurrency(totalAmount),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}

@Composable
private fun ExpenseList(
    expenses: List<ExpenseEntity>,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(expenses) { expense ->
            ExpenseItem(
                expense = expense,
                onDeleteClick = { onDeleteExpense(expense) },
                onEditClick = { onEditExpense(expense) }
            )
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: ExpenseEntity,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = ForestGradient,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expense icon
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
                        text = expense.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDate(Date(expense.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCurrency(expense.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseOrange
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                tint = ExpenseBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyExpenseList(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bugün henüz harcama kaydınız yok",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bugünün ilk harcamanızı eklemek için + butonuna tıklayın",
            style = MaterialTheme.typography.bodyMedium,
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

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr", "TR"))
    return formatter.format(date)
}

// Preview
@Preview(showBackground = true)
@Composable
fun ExpenseScreenPreview() {
    HarcamTheme {
        ExpenseContent(
            uiState = ExpenseUiState(
                expenses = listOf(
                    ExpenseEntity(
                        id = 1,
                        description = "Market alışverişi",
                        amount = 125.50,
                        createdAt = System.currentTimeMillis()
                    ),
                    ExpenseEntity(
                        id = 2,
                        description = "Benzin",
                        amount = 200.00,
                        createdAt = System.currentTimeMillis() - 86400000
                    )
                ),
                totalAmount = 325.50
            ),
            addExpenseState = AddExpenseUiState(),
            onDeleteExpense = {},
            onUpdateExpense = {},
            onHideAddDialog = {},
            onShowEditDialog = {},
            onHideEditDialog = {},
            onDescriptionChange = {},
            onAmountChange = {},
            onAddExpense = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyExpenseScreenPreview() {
    HarcamTheme {
        ExpenseContent(
            uiState = ExpenseUiState(),
            addExpenseState = AddExpenseUiState(),
            onDeleteExpense = {},
            onUpdateExpense = {},
            onHideAddDialog = {},
            onShowEditDialog = {},
            onHideEditDialog = {},
            onDescriptionChange = {},
            onAmountChange = {},
            onAddExpense = {}
        )
    }
}