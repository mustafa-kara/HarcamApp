package com.mustafakara.harcam.data.mapper

import com.mustafakara.harcam.data.local.entity.BudgetEntity
import com.mustafakara.harcam.data.local.entity.CategoryEntity
import com.mustafakara.harcam.data.local.entity.ExchangeRateEntity
import com.mustafakara.harcam.data.local.entity.ExpenseEntity
import com.mustafakara.harcam.data.local.entity.RecurringEntity
import com.mustafakara.harcam.domain.model.Budget
import com.mustafakara.harcam.domain.model.Category
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.model.ExchangeRate
import com.mustafakara.harcam.domain.model.Expense
import com.mustafakara.harcam.domain.model.RecurrenceCadence
import com.mustafakara.harcam.domain.model.RecurringExpense

/** Entity ↔ domain mapping — architecture.md §1 (data/mapper). */

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    amount = amount,
    currency = Currency.fromCode(currency),
    categoryId = categoryId,
    note = description,
    createdAt = createdAt,
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    description = note,
    amount = amount,
    createdAt = createdAt,
    categoryId = categoryId,
    currency = currency.code,
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    colorKey = colorKey,
    iconKey = iconKey,
    isDefault = isDefault,
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    colorKey = colorKey,
    iconKey = iconKey,
    isDefault = isDefault,
)

fun BudgetEntity.toDomain() = Budget(
    id = id,
    categoryId = categoryId,
    limit = amountLimit,
    monthKey = monthKey,
)

fun RecurringEntity.toDomain() = RecurringExpense(
    id = id,
    name = name,
    amount = amount,
    currency = Currency.fromCode(currency),
    categoryId = categoryId,
    cadence = RecurrenceCadence.valueOf(cadence),
    nextDueDate = nextDueDate,
    reminderDaysBefore = reminderDaysBefore,
    isPaused = isPaused,
)

fun RecurringExpense.toEntity() = RecurringEntity(
    id = id,
    name = name,
    amount = amount,
    currency = currency.code,
    categoryId = categoryId,
    cadence = cadence.name,
    nextDueDate = nextDueDate,
    reminderDaysBefore = reminderDaysBefore,
    isPaused = isPaused,
)

fun ExchangeRateEntity.toDomain() = ExchangeRate(
    currency = Currency.fromCode(quote),
    rate = rate,
)
