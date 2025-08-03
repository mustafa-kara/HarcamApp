package com.mustafakara.harcam.data.repository

import com.mustafakara.harcam.data.dao.DailySummary
import com.mustafakara.harcam.data.dao.ExpenseDao
import com.mustafakara.harcam.data.dao.MonthlySummary
import com.mustafakara.harcam.data.dao.WeeklySummary
import com.mustafakara.harcam.data.dao.YearlySummary
import com.mustafakara.harcam.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Harcama verilerine erişim için Repository katmanı
 * MVVM mimarisinde Single Source of Truth prensibi uygular
 * (bir uygulamadaki her bir veri parçasının yalnızca tek bir kaynaktan yönetilmesi
 * ve bu kaynağın verinin tek ve güvenilir doğrusu olarak kabul edilmesi anlamına gelir.)
 * Database operasyonlarını abstract eder
 * (doğrudan veritabanı ile konuşmasını engellemr)
 */
@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    
    /**
     * UI katmanına otomatik güncellenme sağlar
     */
    fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses()
    }

    fun getTodayExpenses(): Flow<List<ExpenseEntity>> {
        return expenseDao.getTodayExpenses()
    }

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }

    fun getTotalAmount(): Flow<Double> {
        return expenseDao.getTotalAmount()
    }

    fun getTodayTotalAmount(): Flow<Double> {
        return expenseDao.getTodayTotalAmount()
    }

    fun getTotalAmountByDateRange(startDate: Long, endDate: Long): Flow<Double> {
        return expenseDao.getTotalAmountByDateRange(startDate, endDate)
    }

    suspend fun getDailySummary(): List<DailySummary> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.timeInMillis
        return expenseDao.getDailySummary(startDate)
    }

    suspend fun getWeeklySummary(): List<WeeklySummary> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, -12)
        val startDate = calendar.timeInMillis
        return expenseDao.getWeeklySummary(startDate)
    }

    suspend fun getMonthlySummary(): List<MonthlySummary> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -12)
        val startDate = calendar.timeInMillis
        return expenseDao.getMonthlySummary(startDate)
    }

    suspend fun getYearlySummary(): List<YearlySummary> {
        return expenseDao.getYearlySummary()
    }

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun addExpense(description: String, amount: Double) {
        val expense = ExpenseEntity(
            description = description,
            amount = amount
        )
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(expenseId: Long) {
        expenseDao.deleteExpenseById(expenseId)
    }

    suspend fun deleteAllExpenses() {
        expenseDao.deleteAllExpenses()
    }
}