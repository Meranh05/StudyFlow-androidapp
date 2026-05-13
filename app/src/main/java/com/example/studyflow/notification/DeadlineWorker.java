package com.example.studyflow.notification;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class DeadlineWorker extends Worker {

    public static final String KEY_DEADLINE_ID = "deadline_id";
    public static final String KEY_TYPE = "type";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DUE = "due_date";
    public static final String KEY_SUBJECT = "subject";
    public static final String KEY_PRIORITY = "priority";
    public static final String KEY_REMINDER_MINUTES = "reminder_minutes";
    public static final String KEY_WHEN = "when_millis";

    public DeadlineWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        String deadlineId = getInputData().getString(KEY_DEADLINE_ID);
        String type = getInputData().getString(KEY_TYPE);
        String title = getInputData().getString(KEY_TITLE);
        String due = getInputData().getString(KEY_DUE);
        String subject = getInputData().getString(KEY_SUBJECT);
        String priority = getInputData().getString(KEY_PRIORITY);
        int reminderMinutes = getInputData().getInt(KEY_REMINDER_MINUTES, 0);
        long whenMillis = getInputData().getLong(KEY_WHEN, System.currentTimeMillis());

        NotificationHelper.showDeadlineNotification(
                getApplicationContext(),
                deadlineId,
                type,
                title != null ? title : "Deadline",
                due != null ? due : "",
                subject,
                priority,
                reminderMinutes,
                whenMillis);

        return Result.success();
    }
}
