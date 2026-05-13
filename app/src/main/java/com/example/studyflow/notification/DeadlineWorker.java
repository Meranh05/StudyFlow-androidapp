package com.example.studyflow.notification;

import android.app.*;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.studyflow.R;
import com.example.studyflow.utils.AppPreferences;

public class DeadlineWorker extends Worker {
    public static final String CHANNEL_ID = "deadline_reminders";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DUE = "due_date";
    public static final String KEY_REMINDER_MINUTES = "reminder_minutes";

    public DeadlineWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        if (!AppPreferences.areNotificationsEnabled(getApplicationContext())) {
            return Result.success();
        }

        String title = getInputData().getString(KEY_TITLE);
        String due = getInputData().getString(KEY_DUE);
        int reminderMinutes = getInputData().getInt(KEY_REMINDER_MINUTES, 60);

        createNotificationChannel();

        String reminderText = reminderMinutes >= 1440
                ? "Nhắc trước 1 ngày"
                : reminderMinutes >= 60
                ? "Nhắc trước " + (reminderMinutes / 60) + " giờ"
                : "Nhắc trước " + reminderMinutes + " phút";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ Deadline sắp đến!")
                .setContentText(title + " — " + reminderText + " • Hạn: " + due)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }

        return Result.success();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở deadline",
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Thông báo deadline sắp đến hạn");
        NotificationManager manager = getApplicationContext()
                .getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }
}