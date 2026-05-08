package com.example.studyflow.ui.calendar;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.FragmentCalendarBinding;
import com.example.studyflow.ui.deadlines.DeadlineAdapter;
import com.example.studyflow.ui.deadlines.AddEditDeadlineSheet;
import com.example.studyflow.viewmodel.DeadlineViewModel;
import com.example.studyflow.utils.DateUtils;
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
                    @Override
                    public void onClick(Deadline d) {
                        showDeadlineDetail(d);
                    }

                    @Override
                    public void onEdit(Deadline d) {
                        AddEditDeadlineSheet sheet = AddEditDeadlineSheet.newInstance(userId, d);
                        sheet.show(getChildFragmentManager(), "edit_deadline_cal");
                    }

                    @Override
                    public void onDelete(Deadline d) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Xóa deadline?")
                                .setMessage("Xóa \"" + d.getTitle() + "\"?")
                                .setPositiveButton("Xóa", (dialog, which) -> {
                                    deadlineViewModel.deleteDeadline(userId, d.getId());
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }

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

    private void showDeadlineDetail(Deadline deadline) {
        String statusText;
        switch (deadline.getStatus()) {
            case "DONE": statusText = "Hoàn thành"; break;
            case "IN_PROGRESS": statusText = "Đang làm"; break;
            default: statusText = "Chưa làm"; break;
        }

        String message = "Môn học: " + deadline.getSubjectName() + "\n" +
                "Hạn nộp: " + DateUtils.formatDateTime(deadline.getDueDate()) + "\n" +
                "Độ ưu tiên: " + deadline.getPriority() + "\n" +
                "Trạng thái: " + statusText + "\n\n" +
                "Mô tả: " + (deadline.getDescription() == null || deadline.getDescription().isEmpty() ? "(Trống)" : deadline.getDescription());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(deadline.getTitle())
                .setMessage(message)
                .setNegativeButton("Đóng", null);

        if (!"DONE".equals(deadline.getStatus())) {
            builder.setPositiveButton("Đã hoàn thành", (d, w) -> {
                deadline.setStatus("DONE");
                deadlineViewModel.updateDeadline(userId, deadline);
            });
        }

        builder.setNeutralButton("Sửa", (d, w) -> {
            AddEditDeadlineSheet sheet = AddEditDeadlineSheet.newInstance(userId, deadline);
            sheet.show(getChildFragmentManager(), "edit_deadline_cal");
        });

        builder.show();
    }

    private void setupCalendar() {
        binding.calendarView.setOnDateChangeListener(
                (calView, year, month, dayOfMonth) -> {
                    showDeadlinesForDate(year, month + 1, dayOfMonth);

                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf =
                            new SimpleDateFormat("EEEE, dd/MM/yyyy",
                                    new Locale("vi"));
                    binding.tvSelectedDateLabel.setText(sdf.format(cal.getTime()));
                });

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
