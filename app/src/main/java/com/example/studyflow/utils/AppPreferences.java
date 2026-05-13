package com.example.studyflow.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPreferences {
    private static final String PREFS = "studyflow_prefs";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

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
}
