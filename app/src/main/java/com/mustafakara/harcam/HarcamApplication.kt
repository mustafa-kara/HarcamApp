package com.mustafakara.harcam

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mustafakara.harcam.work.WorkScheduler
import com.mustafakara.harcam.work.Notifications
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Hilt DI Application. Also the WorkManager [Configuration.Provider] so workers can be
 * Hilt-injected via [HiltWorkerFactory], and the entry point that schedules the periodic
 * recurring/budget background work.
 */
@HiltAndroidApp
class HarcamApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var workScheduler: WorkScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Notifications.ensureChannels(this)
        workScheduler.schedulePeriodicWork()
    }
}
