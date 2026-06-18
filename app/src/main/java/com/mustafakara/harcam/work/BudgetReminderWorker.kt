package com.mustafakara.harcam.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mustafakara.harcam.core.util.MoneyFormatter
import com.mustafakara.harcam.domain.model.BudgetLevel
import com.mustafakara.harcam.domain.model.BudgetStatus
import com.mustafakara.harcam.domain.repository.PreferencesRepository
import com.mustafakara.harcam.domain.usecase.ObserveBudgetStatusUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Notifies the user when the monthly budget crosses 80% (warning) or 100% (over) — architecture.md
 * §7. Reads a single budget-status snapshot; gated by the [PreferencesRepository] budgetAlerts
 * toggle. Calm wording, never an "OVER BUDGET!!" scare (design.md §11).
 */
@HiltWorker
class BudgetReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val observeBudgetStatus: ObserveBudgetStatusUseCase,
    private val preferences: PreferencesRepository,
    private val formatter: MoneyFormatter,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = preferences.observe().first()
            if (!prefs.budgetAlerts) return Result.success()

            val monthly = observeBudgetStatus().first().monthly
            if (monthly != null && monthly.level != BudgetLevel.NORMAL) {
                Notifications.post(
                    context = appContext,
                    channelId = Notifications.CHANNEL_BUDGET,
                    notificationId = NOTIFICATION_ID,
                    title = title(monthly.level),
                    text = body(monthly, prefs.currency),
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun title(level: BudgetLevel): String = when (level) {
        BudgetLevel.WARNING -> "You're nearing your monthly budget"
        BudgetLevel.OVER -> "You're over your monthly budget"
        BudgetLevel.NORMAL -> "Budget update"
    }

    private fun body(status: BudgetStatus, currency: com.mustafakara.harcam.domain.model.Currency): String =
        when (status.level) {
            BudgetLevel.OVER -> "Spent ${formatter.formatBudget(status.spent, status.limit, currency)} this month."
            else -> "${formatter.formatPercent(status.ratio)} used · " +
                formatter.formatBudget(status.spent, status.limit, currency)
        }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
