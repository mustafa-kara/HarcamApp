package com.mustafakara.harcam.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enqueues the periodic background work (architecture.md §7). Both run daily as unique periodic
 * work (KEEP so re-scheduling on each launch doesn't reset the cycle).
 */
@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun schedulePeriodicWork() {
        val workManager = WorkManager.getInstance(context)

        val recurring = PeriodicWorkRequestBuilder<RecurringWorker>(1, TimeUnit.DAYS).build()
        workManager.enqueueUniquePeriodicWork(
            WORK_RECURRING,
            ExistingPeriodicWorkPolicy.KEEP,
            recurring,
        )

        val budget = PeriodicWorkRequestBuilder<BudgetReminderWorker>(1, TimeUnit.DAYS).build()
        workManager.enqueueUniquePeriodicWork(
            WORK_BUDGET,
            ExistingPeriodicWorkPolicy.KEEP,
            budget,
        )
    }

    companion object {
        private const val WORK_RECURRING = "harcam_recurring_worker"
        private const val WORK_BUDGET = "harcam_budget_reminder_worker"
    }
}
