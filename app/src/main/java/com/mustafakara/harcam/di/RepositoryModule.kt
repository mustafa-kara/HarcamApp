package com.mustafakara.harcam.di

import com.mustafakara.harcam.core.common.Clock
import com.mustafakara.harcam.core.common.DefaultDispatcherProvider
import com.mustafakara.harcam.core.common.DispatcherProvider
import com.mustafakara.harcam.core.common.SystemClock
import com.mustafakara.harcam.data.repository.BudgetRepositoryImpl
import com.mustafakara.harcam.data.repository.CategoryRepositoryImpl
import com.mustafakara.harcam.data.repository.ExchangeRateRepositoryImpl
import com.mustafakara.harcam.data.repository.ExpenseRepositoryImpl
import com.mustafakara.harcam.data.repository.PreferencesRepositoryImpl
import com.mustafakara.harcam.data.repository.RecurringRepositoryImpl
import com.mustafakara.harcam.data.repository.SecurityRepositoryImpl
import com.mustafakara.harcam.domain.repository.BudgetRepository
import com.mustafakara.harcam.domain.repository.CategoryRepository
import com.mustafakara.harcam.domain.repository.ExchangeRateRepository
import com.mustafakara.harcam.domain.repository.ExpenseRepository
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.repository.RecurringRepository
import com.mustafakara.harcam.domain.repository.SecurityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds repository implementations to their domain interfaces (the seam) — architecture.md §7. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds @Singleton
    abstract fun bindRecurringRepository(impl: RecurringRepositoryImpl): RecurringRepository

    @Binds @Singleton
    abstract fun bindExchangeRateRepository(impl: ExchangeRateRepositoryImpl): ExchangeRateRepository

    @Binds @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds @Singleton
    abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

    @Binds @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds @Singleton
    abstract fun bindClock(impl: SystemClock): Clock
}
