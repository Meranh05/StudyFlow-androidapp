package com.example.studyflow.ui.calendar;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.FragmentCalendarBinding;
import com.example.studyflow.ui.deadlines.AddEditDeadlineSheet;
import com.example.studyflow.utils.DateUtils;
import com.example.studyflow.viewmodel.DeadlineViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private DeadlineViewModel deadlineViewModel;
    private CalendarDayAdapter dayAdapter;
    private CalendarTaskAdapter taskAdapter;
    private List<Deadline> allDeadlines = new ArrayList<>();
    private String userId;

    private final Calendar displayMonth = Calendar.getInstance();
    private final Calendar selectedDay = Calendar.getInstance();

    private static final int FILTER_DAY = 0;
    private static final int FILTER_ALL = 1;
    private int listFilter = FILTER_DAY;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        userId = user.getUid();

        deadlineViewModel = new ViewModelProvider(requireActivity())
                .get(DeadlineViewModel.class);

        setupCalendar();
        setupTaskList();
        setupFilterChips();
        setupObservers();

        deadlineViewModel.startListening(userId);
        refreshCalendar();
        refreshTaskList();
    }

    private void setupCalendar() {
        dayAdapter = new CalendarDayAdapter((year, month, day) -> {
            selectedDay.set(year, month - 1, day);
            listFilter = FILTER_DAY;
            updateFilterChips();
            refreshCalendar();
            refreshTaskList();
        });
        binding.rvCalendarDays.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendarDays.setAdapter(dayAdapter);

        binding.btnPrevMonth.setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, -1);
            refreshCalendar();
        });
        binding.btnNextMonth.setOnClickListener(v -> {
            displayMonth.add(Calendar.MONTH, 1);
            refreshCalendar();
        });
    }

    private void setupFilterChips() {
        binding.chipToday.setOnClickListener(v -> goToToday());
        binding.chipAll.setOnClickListener(v -> {
            listFilter = FILTER_ALL;
            updateFilterChips();
            refreshCalendar();
            refreshTaskList();
        });
    }

    private void goToToday() {
        listFilter = FILTER_DAY;
        Calendar today = Calendar.getInstance();
        selectedDay.setTime(today.getTime());
        displayMonth.setTime(today.getTime());
        updateFilterChips();
        refreshCalendar();
        refreshTaskList();
    }

    private void updateFilterChips() {
        boolean isToday = DateUtils.isSameDay(selectedDay, Calendar.getInstance());
        binding.chipToday.setChecked(listFilter == FILTER_DAY && isToday);
        binding.chipAll.setChecked(listFilter == FILTER_ALL);
    }

    private void setupTaskList() {
        taskAdapter = new CalendarTaskAdapter(deadline ->
                com.example.studyflow.ui.deadlines.DeadlineDetailSheet.newInstance(userId, deadline)
                        .show(getChildFragmentManager(), "detail_deadline_cal"));
        binding.rvDayDeadlines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDayDeadlines.setAdapter(taskAdapter);
    }

    private void setupObservers() {
        deadlineViewModel.deadlines.observe(getViewLifecycleOwner(), deadlines -> {
            allDeadlines = deadlines != null ? deadlines : new ArrayList<>();
            refreshCalendar();
            refreshTaskList();
        });
    }

    private void refreshCalendar() {
        int year = displayMonth.get(Calendar.YEAR);
        int month = displayMonth.get(Calendar.MONTH);

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi"));
        String label = monthFormat.format(displayMonth.getTime());
        label = label.substring(0, 1).toUpperCase() + label.substring(1);
        binding.tvMonthYear.setText(label);

        Calendar first = Calendar.getInstance();
        first.set(year, month, 1);
        int startDayOfWeek = first.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH);

        Set<Integer> daysWithTasks = new HashSet<>();
        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null) continue;
            Calendar dCal = Calendar.getInstance();
            dCal.setTime(d.getDueDate().toDate());
            if (dCal.get(Calendar.YEAR) == year && dCal.get(Calendar.MONTH) == month) {
                daysWithTasks.add(dCal.get(Calendar.DAY_OF_MONTH));
            }
        }

        List<CalendarDayAdapter.DayItem> days = new ArrayList<>();
        for (int i = 0; i < startDayOfWeek; i++) {
            days.add(new CalendarDayAdapter.DayItem(0, false, false, false));
        }
        for (int d = 1; d <= daysInMonth; d++) {
            boolean selected = listFilter == FILTER_DAY
                    && selectedDay.get(Calendar.YEAR) == year
                    && selectedDay.get(Calendar.MONTH) == month
                    && selectedDay.get(Calendar.DAY_OF_MONTH) == d;
            days.add(new CalendarDayAdapter.DayItem(d, true, selected, daysWithTasks.contains(d)));
        }
        while (days.size() % 7 != 0) {
            days.add(new CalendarDayAdapter.DayItem(0, false, false, false));
        }

        dayAdapter.setMonth(year, month + 1, days);
    }

    private void refreshTaskList() {
        List<CalendarTaskAdapter.TaskListItem> items = new ArrayList<>();
        if (listFilter == FILTER_ALL) {
            appendAllTasksGrouped(items);
        } else {
            appendTasksForDay(items, selectedDay);
        }

        boolean empty = items.isEmpty();
        binding.layoutEmptyDay.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvDayDeadlines.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            binding.layoutEmptyDay.setText(listFilter == FILTER_ALL
                    ? "Không có deadline nào"
                    : "Không có deadline trong ngày này");
        }
        taskAdapter.submitList(items);
        updateFilterChips();
    }

    private void appendAllTasksGrouped(List<CalendarTaskAdapter.TaskListItem> items) {
        Map<Long, List<Deadline>> byDay = new TreeMap<>();
        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null) continue;
            Calendar dCal = Calendar.getInstance();
            dCal.setTime(d.getDueDate().toDate());
            dCal.set(Calendar.HOUR_OF_DAY, 0);
            dCal.set(Calendar.MINUTE, 0);
            dCal.set(Calendar.SECOND, 0);
            dCal.set(Calendar.MILLISECOND, 0);
            long key = dCal.getTimeInMillis();
            byDay.computeIfAbsent(key, k -> new ArrayList<>()).add(d);
        }

        for (Map.Entry<Long, List<Deadline>> entry : byDay.entrySet()) {
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(entry.getKey());
            List<Deadline> dayTasks = entry.getValue();
            Collections.sort(dayTasks, (a, b) -> a.getDueDate().compareTo(b.getDueDate()));
            items.add(new CalendarTaskAdapter.TaskListItem(DateUtils.formatGroupHeader(day)));
            for (Deadline d : dayTasks) {
                items.add(new CalendarTaskAdapter.TaskListItem(d));
            }
        }
    }

    private void appendTasksForDay(List<CalendarTaskAdapter.TaskListItem> items, Calendar day) {
        List<Deadline> dayTasks = new ArrayList<>();
        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null) continue;
            Calendar dCal = Calendar.getInstance();
            dCal.setTime(d.getDueDate().toDate());
            if (DateUtils.isSameDay(day, dCal)) {
                dayTasks.add(d);
            }
        }
        if (dayTasks.isEmpty()) return;

        Collections.sort(dayTasks, (a, b) -> a.getDueDate().compareTo(b.getDueDate()));
        items.add(new CalendarTaskAdapter.TaskListItem(DateUtils.formatGroupHeader(day)));
        for (Deadline d : dayTasks) {
            items.add(new CalendarTaskAdapter.TaskListItem(d));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
