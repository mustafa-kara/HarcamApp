package com.mustafakara.harcam.presentation.dialogs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mustafakara.harcam.data.entity.ExpenseEntity
import com.mustafakara.harcam.ui.theme.ExpenseBlue
import com.mustafakara.harcam.ui.theme.ExpenseGreen
import com.mustafakara.harcam.ui.theme.ExpenseOrange
import com.mustafakara.harcam.ui.theme.ExpenseRed
import com.mustafakara.harcam.ui.theme.OceanGradient

/**
 * Harcama düzenleme için dialog
 */
@Composable
fun EditExpenseDialog(
    expense: ExpenseEntity,
    onDismiss: () -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var description by remember { mutableStateOf(expense.description) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var isAmountError by remember { mutableStateOf(false) }

    LaunchedEffect(expense) {
        description = expense.description
        amount = expense.amount.toString()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // gradient background ve icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = OceanGradient,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Harcamayı Düzenle",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = ExpenseBlue
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ExpenseBlue,
                        focusedLabelColor = ExpenseBlue,
                        cursorColor = ExpenseBlue
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        amount = newValue
                        isAmountError = newValue.isBlank() || newValue.toDoubleOrNull() == null || newValue.toDoubleOrNull()!! <= 0
                    },
                    label = { Text("Tutar (₺)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (isAmountError) ExpenseRed else ExpenseGreen
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isAmountError) ExpenseRed else ExpenseGreen,
                        focusedLabelColor = if (isAmountError) ExpenseRed else ExpenseGreen,
                        cursorColor = if (isAmountError) ExpenseRed else ExpenseGreen,
                        errorBorderColor = ExpenseRed,
                        errorLabelColor = ExpenseRed
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isAmountError,
                    supportingText = if (isAmountError) {
                        { Text("Geçerli bir tutar giriniz", color = ExpenseRed) }
                    } else null,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ExpenseOrange
                        )
                    ) {
                        Text(
                            text = "İptal",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            val newAmount = amount.toDoubleOrNull()
                            if (description.isNotBlank() && newAmount != null && newAmount > 0) {
                                val updatedExpense = expense.copy(
                                    description = description.trim(),
                                    amount = newAmount
                                )
                                onUpdateExpense(updatedExpense)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = description.isNotBlank() && !isAmountError,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExpenseBlue,
                            contentColor = Color.White,
                            disabledContainerColor = ExpenseBlue.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = "Güncelle",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}