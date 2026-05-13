package com.example.studyflow.ui.deadlines;

import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.TimePicker;
import androidx.annotation.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.example.studyflow.R;
import com.example.studyflow.databinding.SheetTimePickerBinding;

public class TimePickerSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onTimePicked(int hour, int minute, boolean hasTime);
    }

    private static final String ARG_HOUR = "hour";
    private static final String ARG_MINUTE = "minute";
    private static final String ARG_HAS_TIME = "has_time";

    private static final String[][] QUICK_TIMES = {
            {"Không có thời gian", "-1"},
            {"07:00", "7:0"}, {"09:00", "9:0"}, {"10:00", "10:0"},
            {"12:00", "12:0"}, {"14:00", "14:0"}, {"16:00", "16:0"},
            {"18:00", "18:0"}, {"20:00", "20:0"}, {"22:00", "22:0"}
    };

    private SheetTimePickerBinding binding;
    private Listener listener;
    private boolean hasTime = true;

    public static TimePickerSheet newInstance(int hour, int minute, boolean hasTime) {
        TimePickerSheet sheet = new TimePickerSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_HOUR, hour);
        args.putInt(ARG_MINUTE, minute);
        args.putBoolean(ARG_HAS_TIME, hasTime);
        sheet.setArguments(args);
        return sheet;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SheetTimePickerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        int hour = args.getInt(ARG_HOUR, 12);
        int minute = args.getInt(ARG_MINUTE, 0);
        hasTime = args.getBoolean(ARG_HAS_TIME, true);

        binding.timePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.timePicker.setHour(hour);
            binding.timePicker.setMinute(minute);
        } else {
            binding.timePicker.setCurrentHour(hour);
            binding.timePicker.setCurrentMinute(minute);
        }

        setupQuickChips();
        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> confirmSelection());
    }

    private void setupQuickChips() {
        binding.chipGroupTimes.removeAllViews();
        for (String[] entry : QUICK_TIMES) {
            Chip chip = new Chip(requireContext());
            chip.setText(entry[0]);
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(R.color.chip_filter_bg);
            chip.setTextColor(requireContext().getColor(R.color.text_secondary));
            chip.setChipStrokeWidth(0);
            chip.setOnClickListener(v -> {
                if ("-1".equals(entry[1])) {
                    hasTime = false;
                    confirmSelection();
                    return;
                }
                hasTime = true;
                String[] parts = entry[1].split(":");
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.timePicker.setHour(h);
                    binding.timePicker.setMinute(m);
                } else {
                    binding.timePicker.setCurrentHour(h);
                    binding.timePicker.setCurrentMinute(m);
                }
            });
            binding.chipGroupTimes.addView(chip);
        }
    }

    private void confirmSelection() {
        if (listener != null) {
            if (hasTime) {
                int h, m;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    h = binding.timePicker.getHour();
                    m = binding.timePicker.getMinute();
                } else {
                    h = binding.timePicker.getCurrentHour();
                    m = binding.timePicker.getCurrentMinute();
                }
                listener.onTimePicked(h, m, true);
            } else {
                listener.onTimePicked(23, 59, false);
            }
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
