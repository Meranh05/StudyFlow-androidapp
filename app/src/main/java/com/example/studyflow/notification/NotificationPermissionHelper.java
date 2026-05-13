package com.example.studyflow.notification;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import com.example.studyflow.R;
import com.example.studyflow.utils.AppPreferences;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class NotificationPermissionHelper {

    private NotificationPermissionHelper() {}

    public static boolean needsRuntimePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    public static boolean hasRuntimePermission(Context context) {
        if (!needsRuntimePermission()) return true;
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** True when app can show notifications on this device. */
    public static boolean canPostNotifications(Context context) {
        if (needsRuntimePermission() && !hasRuntimePermission(context)) {
            return false;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * First launch after install: always show prompt (Android 12 has no runtime permission).
     * Android 13+: dialog then system POST_NOTIFICATIONS.
     * Android 12-: dialog then open notification settings.
     */
    public static void promptOnFirstLaunch(Activity activity,
                                           ActivityResultLauncher<String> launcher) {
        if (AppPreferences.isNotificationIntroCompleted(activity)) return;

        int messageRes = needsRuntimePermission()
                ? R.string.notif_permission_message
                : R.string.notif_permission_message_legacy;

        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.notif_permission_title)
                .setMessage(messageRes)
                .setCancelable(false)
                .setPositiveButton(R.string.notif_permission_allow, (d, w) -> {
                    AppPreferences.setNotificationIntroCompleted(activity, true);
                    AppPreferences.setNotificationsEnabled(activity, true);
                    grantNotificationAccess(activity, launcher);
                })
                .setNegativeButton(R.string.notif_permission_later, (d, w) ->
                        AppPreferences.setNotificationIntroCompleted(activity, true))
                .show();
    }

    public static void promptOnFirstLaunch(Fragment fragment,
                                           ActivityResultLauncher<String> launcher) {
        promptOnFirstLaunch(fragment.requireActivity(), launcher);
    }

    public static void grantNotificationAccess(Activity activity,
                                             ActivityResultLauncher<String> launcher) {
        if (needsRuntimePermission()) {
            if (!hasRuntimePermission(activity)) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            return;
        }
        openAppNotificationSettings(activity);
    }

    public static void ensureNotificationAccess(Activity activity,
                                                ActivityResultLauncher<String> launcher) {
        if (needsRuntimePermission() && !hasRuntimePermission(activity)) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            showOpenSettingsDialog(activity);
        }
    }

    public static void ensureNotificationAccess(Fragment fragment,
                                                ActivityResultLauncher<String> launcher) {
        ensureNotificationAccess(fragment.requireActivity(), launcher);
    }

    public static void showOpenSettingsDialog(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.notif_permission_title)
                .setMessage(R.string.notif_permission_settings_message)
                .setPositiveButton(R.string.notif_open_settings,
                        (d, w) -> openAppNotificationSettings(context))
                .setNegativeButton(R.string.notif_permission_later, null)
                .show();
    }

    public static void openAppNotificationSettings(Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }
}
