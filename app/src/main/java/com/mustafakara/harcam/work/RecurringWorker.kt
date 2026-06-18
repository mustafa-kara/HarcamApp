package com.mustafakara.harcam.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mustafakara.harcam.domain.usecase.MaterializeDueRecurringUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Materializes due recurring templates into real expenses (architecture.md §7). Delegates the
 * idempotent catch-up logic to [MaterializeDueRecurringUseCase] (unit-tested separately).
 */
@HiltWorker
class RecurringWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val materializeDueRecurring: MaterializeDueRecurringUseCase,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        materializeDueRecurring()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
