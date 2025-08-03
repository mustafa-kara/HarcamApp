package com.mustafakara.harcam.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mustafakara.harcam.presentation.state.AddExpenseUiState
import com.mustafakara.harcam.ui.theme.HarcamTheme
import com.mustafakara.harcam.ui.theme.ExpenseBlue
import com.mustafakara.harcam.ui.theme.ExpenseGreen
import com.mustafakara.harcam.ui.theme.TwilightGradient

/**
 * Harcama ekleme dialog
 * Material Design 3 AlertDialog
 */
@Composable
fun AddExpenseDialog(
    addExpenseState: AddExpenseUiState,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = TwilightGradient,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Yeni Harcama",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseBlue
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Açıklama alanı
                OutlinedTextField(
                    value = addExpenseState.description,
                    onValueChange = onDescriptionChange,
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ExpenseBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Açıklama")
                        }
                    },
                    placeholder = { Text("Ör: Market alışverişi") },
                    isError = addExpenseState.isDescriptionError,
                    supportingText = if (addExpenseState.isDescriptionError) {
                        { Text(addExpenseState.descriptionErrorMessage) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ExpenseBlue,
                        focusedLabelColor = ExpenseBlue
                    )
                )
                
                // Tutar alanı
                OutlinedTextField(
                    value = addExpenseState.amount,
                    onValueChange = onAmountChange,
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ExpenseGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tutar (₺)")
                        }
                    },
                    placeholder = { Text("0.00") },
                    isError = addExpenseState.isAmountError,
                    supportingText = if (addExpenseState.isAmountError) {
                        { Text(addExpenseState.amountErrorMessage) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ExpenseGreen,
                        focusedLabelColor = ExpenseGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = addExpenseState.isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExpenseBlue,
                    disabledContainerColor = ExpenseBlue.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ekle",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "İptal",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun AddExpenseDialogPreview() {
    HarcamTheme {
        AddExpenseDialog(
            addExpenseState = AddExpenseUiState(
                description = "Market alışverişi",
                amount = "125.50"
            ),
            onDescriptionChange = {},
            onAmountChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddExpenseDialogErrorPreview() {
    HarcamTheme {
        AddExpenseDialog(
            addExpenseState = AddExpenseUiState(
                description = "",
                amount = "-50",
                isDescriptionError = true,
                isAmountError = true,
                descriptionErrorMessage = "Açıklama boş bırakılamaz",
                amountErrorMessage = "Tutar 0'dan büyük olmalıdır"
            ),
            onDescriptionChange = {},
            onAmountChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}