package com.example.studyflow.utils;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {
    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    public static String getGreeting(String name) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Chào buổi sáng, " + name + "!";
        else if (hour < 18) return "Buổi chiều vui vẻ, " + name + "!";
        else return "Chào buổi tối, " + name + "!";
    }

    public static boolean isOverdue(Timestamp dueDate) {
        if (dueDate == null) return false;
        return dueDate.toDate().before(new Date());
    }

    public static boolean isDueSoon(Timestamp dueDate, int hoursThreshold) {
        if (dueDate == null) return false;
        long diff = dueDate.toDate().getTime() - System.currentTimeMillis();
        return diff > 0 && diff <= (long) hoursThreshold * 3600 * 1000;
    }
}