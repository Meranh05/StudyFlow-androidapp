package com.example.studyflow.ui.calendar;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.FragmentCalendarBinding;
import com.example.studyflow.ui.deadlines.DeadlineAdapter;
import com.example.studyflow.viewmodel.DeadlineViewModel;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private DeadlineViewModel deadlineViewModel;
    private DeadlineAdapter adapter;
    private List<Deadline> allDeadlines = new ArrayList<>();
    private String userId;

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

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        deadlineViewModel = new ViewModelProvider(requireActivity())
                .get(DeadlineViewModel.class);

        setupRecyclerView();
        setupCalendar();
        setupObservers();

        deadlineViewModel.startListening(userId);
    }


    private void setupRecyclerView() {
        adapter = new DeadlineAdapter(
                new DeadlineAdapter.OnDeadlineClickListener() {
                    @Override public void onEdit(Deadline d) {}
                    @Override public void onDelete(Deadline d) {}
                    @Override
                    public void onStatusChange(Deadline d, String status) {
                        d.setStatus(status);
                        deadlineViewModel.updateDeadline(userId, d);
                    }
                });
        binding.rvDayDeadlines.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.rvDayDeadlines.setAdapter(adapter);
    }

        private Calendar selectedDate = Calendar.getInstance();
    private void setupCalendar() {
        // Lắng nghe khi chọn ngày
        binding.calendarView.setOnDateChangeListener(
                (calView, year, month, dayOfMonth) -> {
                    // month bắt đầu từ 0 → +1
                    showDeadlinesForDate(year, month + 1, dayOfMonth);

                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf =
                            new SimpleDateFormat("EEEE, dd/MM/yyyy",
                                    new Locale("vi"));
                    binding.tvSelectedDateLabel.setText(sdf.format(cal.getTime()));
                });

        // Mặc định hiển thị hôm nay
        Calendar today = Calendar.getInstance();
        showDeadlinesForDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void setupObservers() {
        deadlineViewModel.deadlines.observe(
                getViewLifecycleOwner(), deadlines -> {
                    allDeadlines = deadlines;
                    // Refresh ngày đang hiển thị
                    Calendar today = Calendar.getInstance();
                    showDeadlinesForDate(
                            today.get(Calendar.YEAR),
                            today.get(Calendar.MONTH) + 1,
                            today.get(Calendar.DAY_OF_MONTH)
                    );
                });
    }

    private void showDeadlinesForDate(int year, int month, int day) {
        List<Deadline> filtered = new ArrayList<>();
        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null) continue;
            Calendar dCal = Calendar.getInstance();
            dCal.setTime(d.getDueDate().toDate());
            if (dCal.get(Calendar.YEAR)         == year  &&
                    dCal.get(Calendar.MONTH) + 1    == month &&
                    dCal.get(Calendar.DAY_OF_MONTH) == day) {
                filtered.add(d);
            }
        }
        adapter.submitList(filtered);
        boolean empty = filtered.isEmpty();
        binding.layoutEmptyDay.setVisibility(
                empty ? View.VISIBLE : View.GONE);
        binding.rvDayDeadlines.setVisibility(
                empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}