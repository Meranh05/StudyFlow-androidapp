package com.example.studyflow.notification;

import android.content.Context;
import androidx.work.*;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.utils.DateUtils;
import java.util.concurrent.TimeUnit;

public class DeadlineScheduler {

    public static void scheduleReminder(Context context, Deadline deadline) {
        if (deadline == null || deadline.getDueDate() == null) return;
        if ("DONE".equals(deadline.getStatus())) {
            cancelReminder(context, deadline.getId());
            return;
        }

        int reminderMinutes = deadline.getReminderMinutes();
        if (reminderMinutes <= 0) {
            cancelReminder(context, deadline.getId());
            return;
        }

        long dueDateMs = deadline.getDueDate().toDate().getTime();
        long remindAt = dueDateMs - (reminderMinutes * 60_000L);
        long delay = remindAt - System.currentTimeMillis();

        if (delay <= 0) return;

        Data inputData = new Data.Builder()
                .putString(DeadlineWorker.KEY_TITLE, deadline.getTitle())
                .putString(DeadlineWorker.KEY_DUE, DateUtils.formatDateTime(deadline.getDueDate()))
                .putInt(DeadlineWorker.KEY_REMINDER_MINUTES, reminderMinutes)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DeadlineWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("deadline_" + deadline.getId())
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "deadline_" + deadline.getId(),
                ExistingWorkPolicy.REPLACE,
                workRequest
        );
    }

    public static void cancelReminder(Context context, String deadlineId) {
        if (deadlineId == null) return;
        WorkManager.getInstance(context).cancelUniqueWork("deadline_" + deadlineId);
    }
}
