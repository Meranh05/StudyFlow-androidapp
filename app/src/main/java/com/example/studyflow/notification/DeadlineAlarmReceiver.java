package com.example.studyflow.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class DeadlineAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_DEADLINE_ALARM = "com.example.studyflow.DEADLINE_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_DEADLINE_ALARM.equals(intent.getAction())) return;

        Context app = context.getApplicationContext();
        PowerManager pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm != null
                ? pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StudyFlow:DeadlineNotif")
                : null;
        if (wakeLock != null) {
            wakeLock.acquire(15_000L);
        }

        try {
            NotificationHelper.showDeadlineNotification(
                    app,
                    intent.getStringExtra(DeadlineWorker.KEY_DEADLINE_ID),
                    intent.getStringExtra(DeadlineWorker.KEY_TYPE),
                    intent.getStringExtra(DeadlineWorker.KEY_TITLE),
                    intent.getStringExtra(DeadlineWorker.KEY_DUE),
                    intent.getStringExtra(DeadlineWorker.KEY_SUBJECT),
                    intent.getStringExtra(DeadlineWorker.KEY_PRIORITY),
                    intent.getIntExtra(DeadlineWorker.KEY_REMINDER_MINUTES, 0),
                    intent.getLongExtra(DeadlineWorker.KEY_WHEN, System.currentTimeMillis()));
        } finally {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }
}
