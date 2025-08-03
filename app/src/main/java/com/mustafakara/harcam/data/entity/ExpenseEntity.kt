package com.mustafakara.harcam.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Harcama veritabanı
 * Room database için harcama verileri
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val description: String,

    val amount: Double,

    val createdAt: Long = System.currentTimeMillis()
)