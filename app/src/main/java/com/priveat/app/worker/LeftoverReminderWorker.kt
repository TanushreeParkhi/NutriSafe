package com.priveat.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.priveat.app.R

class LeftoverReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val mealName = inputData.getString(KEY_MEAL_NAME) ?: "leftover"
        val action = inputData.getString(KEY_ACTION) ?: "Check Manually"
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("PrivEat leftover check")
            .setContentText("$mealName is reaching its safety window. Suggested action: $action.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(mealName.hashCode(), notification) }
        return Result.success()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Leftover safety",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Local reminders for PrivEat leftover safety timers."
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "leftover_safety"
        const val KEY_TIMER_ID = "timer_id"
        const val KEY_MEAL_NAME = "meal_name"
        const val KEY_ACTION = "action"
    }
}
