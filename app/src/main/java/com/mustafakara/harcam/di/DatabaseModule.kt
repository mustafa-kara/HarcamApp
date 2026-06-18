package com.mustafakara.harcam.di

import android.content.Context
import androidx.room.Room
import com.mustafakara.harcam.data.local.dao.BudgetDao
import com.mustafakara.harcam.data.local.dao.CategoryDao
import com.mustafakara.harcam.data.local.dao.ExchangeRateDao
import com.mustafakara.harcam.data.local.dao.ExpenseDao
import com.mustafakara.harcam.data.local.dao.RecurringDao
import com.mustafakara.harcam.data.local.database.HarcamDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HarcamDatabase =
        Room.databaseBuilder(context, HarcamDatabase::class.java, HarcamDatabase.NAME)
            .addMigrations(HarcamDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideExpenseDao(db: HarcamDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideCategoryDao(db: HarcamDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideBudgetDao(db: HarcamDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideRecurringDao(db: HarcamDatabase): RecurringDao = db.recurringDao()
    @Provides fun provideExchangeRateDao(db: HarcamDatabase): ExchangeRateDao = db.exchangeRateDao()
}
