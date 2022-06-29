package dev.isxander.pronounify.utils

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val counter = AtomicInteger(0)

val POOL = ThreadPoolExecutor(
    20, 20, 0L,
    TimeUnit.SECONDS,
    LinkedBlockingQueue()
) { r -> Thread(r, "PronounMC " + counter.incrementAndGet()) }

val SCHEDULED_POOL = ScheduledThreadPoolExecutor(1) { r ->
    Thread(r, "Pronounify (Scheduled) " + counter.incrementAndGet())
}

fun runAsync(runnable: () -> Unit) {
    POOL.execute(runnable)
}

fun scheduleAsync(delay: Long, runnable: Runnable) {
    SCHEDULED_POOL.schedule(runnable, delay, TimeUnit.MILLISECONDS)
}
