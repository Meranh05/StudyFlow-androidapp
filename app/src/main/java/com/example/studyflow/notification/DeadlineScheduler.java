package com.example.studyflow.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.utils.DateUtils;
import java.util.List;

public class DeadlineScheduler {

    private static String alarmRequestKey(String deadlineId, String type) {
        return deadlineId + "_" + type;
    }

    public static void scheduleReminder(Context context, Deadline deadline) {
        if (deadline == null || deadline.getId() == null || deadline.getDueDate() == null) return;

        cancelReminder(context, deadline.getId());

        if ("DONE".equals(deadline.getStatus())) return;

        long dueDateMs = deadline.getDueDate().toDate().getTime();
        long now = System.currentTimeMillis();
        int reminderMinutes = deadline.getReminderMinutes();

        if (reminderMinutes > 0) {
            long remindAt = dueDateMs - (reminderMinutes * 60_000L);
            scheduleAt(context, deadline, NotificationHelper.TYPE_BEFORE, remindAt, remindAt);
        }

        scheduleAt(context, deadline, NotificationHelper.TYPE_DUE, dueDateMs, dueDateMs);
    }

    public static void rescheduleAll(Context context, List<Deadline> deadlines) {
        if (deadlines == null) return;
        for (Deadline deadline : deadlines) {
            scheduleReminder(context, deadline);
        }
    }

    private static void scheduleAt(Context context, Deadline deadline, String type,
                                   long triggerAt, long whenMillis) {
        long now = System.currentTimeMillis();
        long delay = triggerAt - now;

        // Đã qua giờ — không hiện lại khi mở app / reschedule
        if (delay <= 0) return;

        long alarmAt = triggerAt;
        if (delay < 1_000L) {
            alarmAt = now + 1_000L;
        }

        Intent intent = buildAlarmIntent(context, deadline, type, whenMillis);

        int requestCode = NotificationHelper.notificationId(
                alarmRequestKey(deadline.getId(), type), type);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmAt, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmAt, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmAt, pendingIntent);
        }
    }

    private static Intent buildAlarmIntent(Context context, Deadline deadline,
                                           String type, long whenMillis) {
        Intent intent = new Intent(context, DeadlineAlarmReceiver.class);
        intent.setAction(DeadlineAlarmReceiver.ACTION_DEADLINE_ALARM);
        intent.putExtra(DeadlineWorker.KEY_DEADLINE_ID, deadline.getId());
        intent.putExtra(DeadlineWorker.KEY_TYPE, type);
        intent.putExtra(DeadlineWorker.KEY_TITLE, deadline.getTitle());
        intent.putExtra(DeadlineWorker.KEY_DUE, DateUtils.formatDateTime(deadline.getDueDate()));
        intent.putExtra(DeadlineWorker.KEY_SUBJECT, deadline.getSubjectName());
        intent.putExtra(DeadlineWorker.KEY_PRIORITY, deadline.getPriority());
        intent.putExtra(DeadlineWorker.KEY_REMINDER_MINUTES, deadline.getReminderMinutes());
        intent.putExtra(DeadlineWorker.KEY_WHEN, whenMillis);
        return intent;
    }

    public static void cancelReminder(Context context, String deadlineId) {
        if (deadlineId == null) return;
        cancelAlarm(context, deadlineId, NotificationHelper.TYPE_BEFORE);
        cancelAlarm(context, deadlineId, NotificationHelper.TYPE_DUE);
    }

    private static void cancelAlarm(Context context, String deadlineId, String type) {
        Intent intent = new Intent(context, DeadlineAlarmReceiver.class);
        intent.setAction(DeadlineAlarmReceiver.ACTION_DEADLINE_ALARM);
        int requestCode = NotificationHelper.notificationId(alarmRequestKey(deadlineId, type), type);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pendingIntent.cancel();
    }
}
