package com.example.studyflow.ui.deadlines;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.example.studyflow.R;
import com.example.studyflow.databinding.SheetDatetimePickerBinding;
import com.example.studyflow.ui.calendar.CalendarDayAdapter;
import com.example.studyflow.ui.common.BottomSheetHelper;
import com.example.studyflow.utils.DateUtils;
import com.example.studyflow.utils.ReminderUtils;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateTimePickerSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "datetime_picker_result";
    public static final String KEY_MILLIS = "millis";
    public static final String KEY_HAS_TIME = "has_time";
    public static final String KEY_REMINDER = "reminder_minutes";

    private static final String ARG_MILLIS = "millis";
    private static final String ARG_HAS_TIME = "has_time";
    private static final String ARG_REMINDER = "reminder";

    private static final String[] REMINDER_LABELS = ReminderUtils.REMINDER_LABELS;
    private static final int[] REMINDER_VALUES = ReminderUtils.REMINDER_VALUES;

    private SheetDatetimePickerBinding binding;
    private CalendarDayAdapter dayAdapter;
    private final Calendar displayMonth = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();
    private boolean hasTime = true;
    private int reminderMinutes = 60;

    public static DateTimePickerSheet newInstance(long millis, boolean hasTime, int reminderMinutes) {
        DateTimePickerSheet sheet = new DateTimePickerSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_MILLIS, millis);
        args.putBoolean(ARG_HAS_TIME, hasTime);
        args.putInt(ARG_REMINDER, reminderMinutes);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SheetDatetimePickerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        selectedDate.setTimeInMillis(args.getLong(ARG_MILLIS, System.currentTimeMillis()));
        hasTime = args.getBoolean(ARG_HAS_TIME, true);
        reminderMinutes = args.getInt(ARG_REMINDER, 60);
        displayMonth.setTime(selectedDate.getTime());

        setupCalendar();
        setupQuickDateChips();
        updateLabels();

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> confirmSelection());
        binding.btnPrevMonth.setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, -1);
            refreshCalendar();
        });
        binding.btnNextMonth.setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, 1);
            refreshCalendar();
        });
        binding.rowTime.setOnClickListener(v -> openTimePicker());
        binding.rowReminder.setOnClickListener(v -> showReminderPicker());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (binding != null) {
            BottomSheetHelper.fitToContent(this, binding.getRoot());
        }
    }

    private void setupCalendar() {
        dayAdapter = new CalendarDayAdapter((year, month, day) -> {
            selectedDate.set(year, month - 1, day);
            refreshCalendar();
            binding.chipGroupDates.clearCheck();
        });
        binding.rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendar.setAdapter(dayAdapter);
        refreshCalendar();
    }

    private void setupQuickDateChips() {
        String[][] quickDates = {
                {"Hôm nay", "0"},
                {"Ngày mai", "1"},
                {"Chủ nhật này", "sun"},
                {"3 ngày sau", "3"}
        };
        for (String[] entry : quickDates) {
            Chip chip = new Chip(requireContext());
            chip.setText(entry[0]);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_filter_bg);
            chip.setTextColor(requireContext().getColorStateList(R.color.chip_filter_text));
            chip.setChipStrokeWidth(0);
            chip.setOnClickListener(v -> applyQuickDate(entry[1]));
            binding.chipGroupDates.addView(chip);
        }
    }

    private void applyQuickDate(String key) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, selectedDate.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, selectedDate.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        switch (key) {
            case "0": break;
            case "1": cal.add(Calendar.DAY_OF_YEAR, 1); break;
            case "3": cal.add(Calendar.DAY_OF_YEAR, 3); break;
            case "sun":
                int dow = cal.get(Calendar.DAY_OF_WEEK);
                int daysUntilSunday = (Calendar.SUNDAY - dow + 7) % 7;
                if (daysUntilSunday == 0) daysUntilSunday = 7;
                cal.add(Calendar.DAY_OF_YEAR, daysUntilSunday);
                break;
        }
        selectedDate.setTime(cal.getTime());
        displayMonth.setTime(selectedDate.getTime());
        refreshCalendar();
    }

    private void refreshCalendar() {
        int year = displayMonth.get(Calendar.YEAR);
        int month = displayMonth.get(Calendar.MONTH);

        SimpleDateFormat monthFormat = new SimpleDateFormat("'thg' M, yyyy", new Locale("vi"));
        binding.tvMonthYear.setText(monthFormat.format(displayMonth.getTime()));

        Calendar first = Calendar.getInstance();
        first.set(year, month, 1);
        int startDayOfWeek = first.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH);

        List<CalendarDayAdapter.DayItem> days = new ArrayList<>();
        for (int i = 0; i < startDayOfWeek; i++) {
            days.add(new CalendarDayAdapter.DayItem(0, false, false, false));
        }
        for (int d = 1; d <= daysInMonth; d++) {
            boolean selected = selectedDate.get(Calendar.YEAR) == year
                    && selectedDate.get(Calendar.MONTH) == month
                    && selectedDate.get(Calendar.DAY_OF_MONTH) == d;
            days.add(new CalendarDayAdapter.DayItem(d, true, selected, false));
        }
        while (days.size() % 7 != 0) {
            days.add(new CalendarDayAdapter.DayItem(0, false, false, false));
        }
        dayAdapter.setMonth(year, month + 1, days);
    }

    private void openTimePicker() {
        TimePickerSheet sheet = TimePickerSheet.newInstance(
                selectedDate.get(Calendar.HOUR_OF_DAY),
                selectedDate.get(Calendar.MINUTE),
                hasTime);
        sheet.setListener((hour, minute, hasTimeValue) -> {
            hasTime = hasTimeValue;
            selectedDate.set(Calendar.HOUR_OF_DAY, hour);
            selectedDate.set(Calendar.MINUTE, minute);
            selectedDate.set(Calendar.SECOND, 0);
            selectedDate.set(Calendar.MILLISECOND, 0);
            updateLabels();
        });
        sheet.show(getChildFragmentManager(), "time_picker");
    }

    private void showReminderPicker() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Lời nhắc lúc")
                .setItems(REMINDER_LABELS, (dialog, which) -> {
                    reminderMinutes = REMINDER_VALUES[which];
                    updateLabels();
                })
                .show();
    }

    private void updateLabels() {
        if (hasTime) {
            binding.tvTimeValue.setText(DateUtils.formatTimeLabel(
                    new com.google.firebase.Timestamp(selectedDate.getTime())));
            binding.tvTimeValue.setTextColor(requireContext().getColor(R.color.primary));
        } else {
            binding.tvTimeValue.setText("Không");
            binding.tvTimeValue.setTextColor(requireContext().getColor(R.color.text_secondary));
        }
        binding.tvReminderValue.setText(ReminderUtils.getLabel(reminderMinutes));
    }

    private String getReminderLabel(int minutes) {
        return ReminderUtils.getLabel(minutes);
    }

    private void confirmSelection() {
        if (!hasTime) {
            selectedDate.set(Calendar.HOUR_OF_DAY, 23);
            selectedDate.set(Calendar.MINUTE, 59);
        }
        Bundle result = new Bundle();
        result.putLong(KEY_MILLIS, selectedDate.getTimeInMillis());
        result.putBoolean(KEY_HAS_TIME, hasTime);
        result.putInt(KEY_REMINDER, reminderMinutes);
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
