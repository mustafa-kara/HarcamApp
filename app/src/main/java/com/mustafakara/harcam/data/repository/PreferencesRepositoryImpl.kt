package com.mustafakara.harcam.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mustafakara.harcam.domain.model.Currency
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.repository.PreferencesRepository.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "harcam_prefs")

/** User preferences backed by Jetpack DataStore — architecture.md §5. */
@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PreferencesRepository {

    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val CURRENCY = stringPreferencesKey("currency")
        val THEME = stringPreferencesKey("theme_mode")
        val MASK_AMOUNTS = booleanPreferencesKey("mask_amounts")
        val BUDGET_ALERTS = booleanPreferencesKey("budget_alerts")
        val RECURRING_REMINDERS = booleanPreferencesKey("recurring_reminders")
    }

    override fun observe(): Flow<PreferencesRepository.Preferences> = context.dataStore.data.map { p ->
        PreferencesRepository.Preferences(
            onboarded = p[Keys.ONBOARDED] ?: false,
            currency = Currency.fromCode(p[Keys.CURRENCY] ?: Currency.TRY.code),
            themeMode = runCatching { ThemeMode.valueOf(p[Keys.THEME] ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            maskAmounts = p[Keys.MASK_AMOUNTS] ?: false,
            budgetAlerts = p[Keys.BUDGET_ALERTS] ?: true,
            recurringReminders = p[Keys.RECURRING_REMINDERS] ?: true,
        )
    }

    override suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDED] = value }
    }

    override suspend fun setCurrency(currency: Currency) {
        context.dataStore.edit { it[Keys.CURRENCY] = currency.code }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    override suspend fun setMaskAmounts(value: Boolean) {
        context.dataStore.edit { it[Keys.MASK_AMOUNTS] = value }
    }

    override suspend fun setBudgetAlerts(value: Boolean) {
        context.dataStore.edit { it[Keys.BUDGET_ALERTS] = value }
    }

    override suspend fun setRecurringReminders(value: Boolean) {
        context.dataStore.edit { it[Keys.RECURRING_REMINDERS] = value }
    }
}
