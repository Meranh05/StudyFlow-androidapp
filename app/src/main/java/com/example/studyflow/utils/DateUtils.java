package com.example.studyflow.utils;

import com.example.studyflow.data.model.Deadline;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {
    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        Calendar now = Calendar.getInstance();
        Calendar due = Calendar.getInstance();
        due.setTime(date);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeStr = timeFormat.format(date);

        if (isSameDay(now, due)) {
            return "Hôm nay • " + timeStr;
        }

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(tomorrow, due)) {
            return "Ngày mai • " + timeStr;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM • HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatTimeOnly(Timestamp timestamp) {
        if (timestamp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    public static String formatDateLabel(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        Calendar now = Calendar.getInstance();
        Calendar due = Calendar.getInstance();
        due.setTime(date);

        if (isSameDay(now, due)) return "HÔM NAY";

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        if (isSameDay(tomorrow, due)) return "NGÀY MAI";

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (isSameDay(yesterday, due)) return "HÔM QUA";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatTimeLabel(Timestamp timestamp) {
        return formatTimeOnly(timestamp);
    }

    public static String formatDeadlinePicker(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("d 'Tháng' M, HH:mm", new Locale("vi"));
        return sdf.format(cal.getTime());
    }

    public static String formatCalendarSubtitle(Deadline deadline) {
        if (deadline.getDueDate() == null) return "";
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = timeFormat.format(deadline.getDueDate().toDate());
        if ("DONE".equals(deadline.getStatus())) {
            return time + " • Hoàn thành";
        }
        if (isLate(deadline)) {
            return time + " • Trễ";
        }
        return time + " • Chưa hoàn thành";
    }

    public static String formatGroupHeader(Calendar day) {
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        String dateStr = sdf.format(day.getTime());

        if (isSameDay(today, day)) {
            return "HÔM NAY - " + dateStr;
        }
        if (isSameDay(yesterday, day)) {
            return "HÔM QUA - " + dateStr;
        }
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("vi"));
        return dayFormat.format(day.getTime()).toUpperCase(Locale.getDefault()) + " - " + dateStr;
    }

    public static boolean isLate(Deadline deadline) {
        if (deadline.getDueDate() == null) return false;
        long due = deadline.getDueDate().toDate().getTime();
        if ("DONE".equals(deadline.getStatus())) {
            Timestamp updated = deadline.getUpdatedAt();
            long completed = updated != null ? updated.toDate().getTime() : due;
            return completed > due;
        }
        return System.currentTimeMillis() > due;
    }

    public static boolean isCompletedLate(Deadline deadline) {
        if (!"DONE".equals(deadline.getStatus()) || deadline.getDueDate() == null) return false;
        long due = deadline.getDueDate().toDate().getTime();
        Timestamp updated = deadline.getUpdatedAt();
        long completed = updated != null ? updated.toDate().getTime() : due;
        return completed > due;
    }

    public static String formatLateMinutes(Deadline deadline) {
        if (deadline.getDueDate() == null) return "TRỄ";
        long due = deadline.getDueDate().toDate().getTime();
        long diff = System.currentTimeMillis() - due;
        if (diff <= 0) return "TRỄ";
        int minutes = (int) (diff / 60_000);
        if (minutes < 60) return "TRỄ " + minutes + "P";
        int hours = minutes / 60;
        return "TRỄ " + hours + "G";
    }

    public static String formatLateBadge(Deadline deadline) {
        if (deadline.getDueDate() == null) return "TRỄ";
        long due = deadline.getDueDate().toDate().getTime();
        Timestamp updated = deadline.getUpdatedAt();
        long completed = updated != null ? updated.toDate().getTime() : due;
        long diff = completed - due;
        if (diff <= 0) return "ĐÚNG HẠN";
        int minutes = (int) (diff / 60_000);
        if (minutes < 60) return "TRỄ " + minutes + "P";
        int hours = minutes / 60;
        return "TRỄ " + hours + "G";
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return isSameDay(c1, c2);
    }

    public static String getGreeting(String name) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Chào buổi sáng, " + name + "!";
        else if (hour < 18) return "Buổi chiều vui vẻ, " + name + "!";
        else return "Chào buổi tối, " + name + "!";
    }
}
