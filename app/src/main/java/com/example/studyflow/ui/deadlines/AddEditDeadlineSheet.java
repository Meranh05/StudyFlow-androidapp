package com.example.studyflow.ui.deadlines;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import androidx.annotation.*;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.example.studyflow.R;
import com.example.studyflow.data.model.*;
import com.example.studyflow.databinding.SheetAddDeadlineBinding;
import com.example.studyflow.viewmodel.*;
import java.util.*;

public class AddEditDeadlineSheet extends BottomSheetDialogFragment {
    private SheetAddDeadlineBinding binding;
    private DeadlineViewModel deadlineViewModel;
    private SubjectViewModel subjectViewModel;
    private String userId;
    private Deadline editingDeadline; // null = thêm mới
    private Calendar selectedDateTime = Calendar.getInstance();
    private List<Subject> subjectList = new ArrayList<>();

    public static AddEditDeadlineSheet newInstance(String userId, @Nullable Deadline deadline) {
        AddEditDeadlineSheet sheet = new AddEditDeadlineSheet();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        if (deadline != null) args.putString("deadlineId", deadline.getId());
        sheet.setArguments(args);
        sheet.editingDeadline = deadline;
        return sheet;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SheetAddDeadlineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = requireArguments().getString("userId");
        deadlineViewModel = new ViewModelProvider(requireActivity()).get(DeadlineViewModel.class);
        subjectViewModel = new ViewModelProvider(requireActivity()).get(SubjectViewModel.class);

        setupSubjectDropdown();
        setupDateTimePicker();
        setupPriorityChips();
        setupButtons();

        // Pre-fill nếu đang edit
        if (editingDeadline != null) prefillForm();
    }

    private void setupSubjectDropdown() {
        subjectViewModel.subjects.observe(getViewLifecycleOwner(), subjects -> {
            subjectList = subjects;
            List<String> names = new ArrayList<>();
            names.add("Không có môn học");
            for (Subject s : subjects) names.add(s.getName());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    names);
            binding.spinnerSubject.setAdapter(adapter);
        });
    }

    private void setupDateTimePicker() {
        binding.btnPickDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (dp, y, m, d) -> {
                selectedDateTime.set(Calendar.YEAR, y);
                selectedDateTime.set(Calendar.MONTH, m);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, d);
                new TimePickerDialog(requireContext(), (tp, h, min) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, h);
                    selectedDateTime.set(Calendar.MINUTE, min);
                    binding.tvSelectedDate.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%04d %02d:%02d",
                                    d, m + 1, y, h, min));
                }, selectedDateTime.get(Calendar.HOUR_OF_DAY),
                        selectedDateTime.get(Calendar.MINUTE), true).show();
            }, selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupPriorityChips() {
        // Default: MEDIUM
        binding.chipMedium.setChecked(true);
    }

    private void setupButtons() {
        binding.btnSave.setOnClickListener(v -> saveDeadline());
        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveDeadline() {
        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            binding.tilTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }

        String priority;
        if (binding.chipHigh.isChecked()) priority = "HIGH";
        else if (binding.chipLow.isChecked()) priority = "LOW";
        else priority = "MEDIUM";

        // Subject
        String subjectId = "", subjectName = "";
        int selectedPos = binding.spinnerSubject.getSelectedItemPosition();
        if (selectedPos > 0 && selectedPos - 1 < subjectList.size()) {
            Subject s = subjectList.get(selectedPos - 1);
            subjectId = s.getId();
            subjectName = s.getName();
        }

        Timestamp dueDate = new Timestamp(selectedDateTime.getTime());

        if (editingDeadline == null) {
            // Thêm mới
            Deadline d = new Deadline(title, desc, subjectId, subjectName,
                    dueDate, priority, "TODO");
            deadlineViewModel.addDeadline(userId, d);
        } else {
            // Cập nhật
            editingDeadline.setTitle(title);
            editingDeadline.setDescription(desc);
            editingDeadline.setSubjectId(subjectId);
            editingDeadline.setSubjectName(subjectName);
            editingDeadline.setDueDate(dueDate);
            editingDeadline.setPriority(priority);
            editingDeadline.setUpdatedAt(Timestamp.now());
            deadlineViewModel.updateDeadline(userId, editingDeadline);
        }

        dismiss();
    }

    private void prefillForm() {
        binding.etTitle.setText(editingDeadline.getTitle());
        binding.etDescription.setText(editingDeadline.getDescription());
        if ("HIGH".equals(editingDeadline.getPriority())) binding.chipHigh.setChecked(true);
        else if ("LOW".equals(editingDeadline.getPriority())) binding.chipLow.setChecked(true);
        else binding.chipMedium.setChecked(true);
    }
}