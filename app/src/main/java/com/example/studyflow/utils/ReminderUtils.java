package com.example.studyflow.utils;

public final class ReminderUtils {
    public static final int[] REMINDER_VALUES = {0, 5, 15, 30, 60, 120, 1440};
    public static final String[] REMINDER_LABELS = {
            "Không nhắc",
            "5 phút trước",
            "15 phút trước",
            "30 phút trước",
            "1 giờ trước",
            "2 giờ trước",
            "1 ngày trước"
    };

    private ReminderUtils() {}

    public static String getLabel(int minutes) {
        for (int i = 0; i < REMINDER_VALUES.length; i++) {
            if (REMINDER_VALUES[i] == minutes) return REMINDER_LABELS[i];
        }
        if (minutes <= 0) return REMINDER_LABELS[0];
        if (minutes < 60) return minutes + " phút trước";
        if (minutes < 1440) return (minutes / 60) + " giờ trước";
        return (minutes / 1440) + " ngày trước";
    }

    public static int indexOf(int minutes) {
        for (int i = 0; i < REMINDER_VALUES.length; i++) {
            if (REMINDER_VALUES[i] == minutes) return i;
        }
        return 4;
    }
}
