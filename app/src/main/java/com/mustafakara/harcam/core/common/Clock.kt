package com.mustafakara.harcam.core.common

import javax.inject.Inject

/** Injectable time source so use cases that depend on "now" are testable. */
interface Clock {
    fun nowMs(): Long
}

class SystemClock @Inject constructor() : Clock {
    override fun nowMs(): Long = System.currentTimeMillis()
}
