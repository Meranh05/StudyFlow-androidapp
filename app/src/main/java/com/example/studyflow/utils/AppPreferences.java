package com.example.studyflow.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPreferences {
    private static final String PREFS = "studyflow_prefs";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_NOTIF_INTRO_COMPLETED = "notif_intro_completed";
    private static final String KEY_LAST_USER_ID = "last_user_id";

    private AppPreferences() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean areNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    public static boolean isNotificationIntroCompleted(Context context) {
        return prefs(context).getBoolean(KEY_NOTIF_INTRO_COMPLETED, false);
    }

    public static void setNotificationIntroCompleted(Context context, boolean completed) {
        prefs(context).edit().putBoolean(KEY_NOTIF_INTRO_COMPLETED, completed).apply();
    }

    public static void setLastUserId(Context context, String userId) {
        prefs(context).edit().putString(KEY_LAST_USER_ID, userId).apply();
    }

    public static String getLastUserId(Context context) {
        return prefs(context).getString(KEY_LAST_USER_ID, null);
    }
}
