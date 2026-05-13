package com.example.studyflow.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.studyflow.R;
import com.example.studyflow.ui.main.MainActivity;
import com.example.studyflow.utils.AppPreferences;

public final class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    /** v2: IMPORTANCE_HIGH + standard style for reliable heads-up popup */
    public static final String CHANNEL_DEADLINES = "studyflow_deadlines_v2";
    private static final String CHANNEL_LEGACY = "studyflow_deadlines";
    public static final String TYPE_BEFORE = "before";
    public static final String TYPE_DUE = "due";
    private static final long[] VIBRATE_PATTERN = {0, 280, 140, 280};

    private NotificationHelper() {}

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        manager.deleteNotificationChannel(CHANNEL_LEGACY);

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        AudioAttributes audio = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_DEADLINES,
                context.getString(R.string.notif_channel_deadlines),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(context.getString(R.string.notif_channel_deadlines_desc));
        channel.enableLights(true);
        channel.setLightColor(Color.parseColor("#FF8A00"));
        channel.enableVibration(true);
        channel.setVibrationPattern(VIBRATE_PATTERN);
        channel.setSound(sound, audio);
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setBypassDnd(false);
        manager.createNotificationChannel(channel);
    }

    public static int notificationId(String deadlineId, String type) {
        String key = deadlineId != null ? deadlineId : "unknown";
        return (key + "_" + type).hashCode();
    }

    public static void showDeadlineNotification(
            Context context,
            String deadlineId,
            String type,
            String title,
            String dueFormatted,
            String subjectName,
            String priority,
            int reminderMinutes,
            long whenMillis) {

        if (!AppPreferences.areNotificationsEnabled(context)) {
            Log.w(TAG, "Skipped: in-app notifications disabled");
            return;
        }
        if (!NotificationPermissionHelper.canPostNotifications(context)) {
            Log.w(TAG, "Skipped: system notification permission missing");
            return;
        }

        ensureChannels(context);

        boolean isDue = TYPE_DUE.equals(type);
        String safeTitle = title != null && !title.isEmpty() ? title : "Deadline";
        String priorityText = priorityCompact(context, priority);
        String status = isDue
                ? context.getString(R.string.notif_status_due)
                : context.getString(R.string.notif_status_before);

        int notifId = notificationId(deadlineId, type);
        PendingIntent pendingIntent = buildContentIntent(context, notifId);
        Bitmap largeIcon = loadLargeIcon(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DEADLINES)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setLargeIcon(largeIcon)
                .setColor(context.getColor(R.color.primary))
                .setContentTitle(safeTitle)
                .setContentText(priorityText)
                .setSubText(status)
                .setContentIntent(pendingIntent)
                .addAction(0, context.getString(R.string.notif_action_open), pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(whenMillis)
                .setShowWhen(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .setVibrate(VIBRATE_PATTERN)
                    .setLights(context.getColor(R.color.primary), 600, 400);
        }

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build());
            Log.d(TAG, "Posted notification id=" + notifId + " title=" + safeTitle);
        } catch (SecurityException e) {
            Log.e(TAG, "notify failed", e);
        }
    }

    private static Bitmap loadLargeIcon(Context context) {
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_logo);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static PendingIntent buildContentIntent(Context context, int requestCode) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static String priorityCompact(Context context, String priority) {
        if (priority == null) {
            return context.getString(R.string.notif_priority_compact_medium);
        }
        switch (priority) {
            case "HIGH":
                return context.getString(R.string.notif_priority_compact_high);
            case "MEDIUM":
                return context.getString(R.string.notif_priority_compact_medium);
            case "LOW":
                return context.getString(R.string.notif_priority_compact_low);
            default:
                return context.getString(R.string.notif_priority_compact_medium);
        }
    }

    public static boolean hasPostPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }
}
