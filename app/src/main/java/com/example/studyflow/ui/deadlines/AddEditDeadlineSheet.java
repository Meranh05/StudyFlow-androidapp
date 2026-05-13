package com.example.studyflow.ui.deadlines;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.example.studyflow.R;
import com.example.studyflow.data.model.*;
import com.example.studyflow.data.repository.UserRepository;
import com.example.studyflow.databinding.SheetAddDeadlineBinding;
import com.example.studyflow.notification.DeadlineScheduler;
import com.example.studyflow.utils.DateUtils;
import com.example.studyflow.utils.ReminderUtils;
import com.example.studyflow.viewmodel.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddEditDeadlineSheet extends BottomSheetDialogFragment {
    private SheetAddDeadlineBinding binding;
    private DeadlineViewModel deadlineViewModel;
    private SubjectViewModel subjectViewModel;
    private final UserRepository userRepo = new UserRepository();
    private String userId;
    private Deadline editingDeadline;
    private final Calendar selectedDateTime = Calendar.getInstance();
    private String selectedPriority = "HIGH";
    private String selectedSubjectId = "";
    private String selectedSubjectName = "";
    private int reminderMinutes = 60;
    private boolean hasSpecificTime = true;
    private Deadline pendingSchedule;

    public static AddEditDeadlineSheet newInstance(String userId, @Nullable Deadline deadline) {
        AddEditDeadlineSheet sheet = new AddEditDeadlineSheet();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        sheet.setArguments(args);
        sheet.editingDeadline = deadline;
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
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

        setupListeners();
        setupPriorityButtons();
        observeSubjects();
        observeSaveResult();
        loadDefaultReminder();

        if (editingDeadline != null) {
            prefillForm();
        } else {
            selectedDateTime.add(Calendar.DAY_OF_YEAR, 1);
            selectedDateTime.set(Calendar.SECOND, 0);
            selectedDateTime.set(Calendar.MILLISECOND, 0);
            updateDateTimeLabels();
            selectPriority("HIGH");
        }

        binding.tvReminderValue.setText(ReminderUtils.getLabel(reminderMinutes));
        subjectViewModel.startListening(userId);
    }

    private void loadDefaultReminder() {
        if (editingDeadline != null) return;
        userRepo.getUser(userId, user -> {
            if (user != null && user.getDefaultReminderMinutes() > 0) {
                reminderMinutes = user.getDefaultReminderMinutes();
                if (isAdded()) {
                    binding.tvReminderValue.setText(ReminderUtils.getLabel(reminderMinutes));
                }
            }
        });
    }

    private void observeSaveResult() {
        deadlineViewModel.operationResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null || pendingSchedule == null) return;
            if (result.startsWith("success:")) {
                String id = result.substring("success:".length());
                pendingSchedule.setId(id);
                DeadlineScheduler.scheduleReminder(
                        requireContext().getApplicationContext(), pendingSchedule);
                pendingSchedule = null;
            }
        });
    }

    private void setupListeners() {
        getChildFragmentManager().setFragmentResultListener(
                DateTimePickerSheet.REQUEST_KEY, this, (key, bundle) -> {
            selectedDateTime.setTimeInMillis(bundle.getLong(DateTimePickerSheet.KEY_MILLIS));
            hasSpecificTime = bundle.getBoolean(DateTimePickerSheet.KEY_HAS_TIME, true);
            reminderMinutes = bundle.getInt(DateTimePickerSheet.KEY_REMINDER, reminderMinutes);
            updateDateTimeLabels();
            binding.tvReminderValue.setText(ReminderUtils.getLabel(reminderMinutes));
        });

        binding.btnPickDeadline.setOnClickListener(v -> showDateTimePicker());
        binding.btnPickReminder.setOnClickListener(v -> showReminderPicker());
        binding.btnSave.setOnClickListener(v -> saveDeadline());
        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnMore.setOnClickListener(v -> {
            if (editingDeadline != null) {
                DeadlineScheduler.cancelReminder(requireContext().getApplicationContext(), editingDeadline.getId());
                deadlineViewModel.deleteDeadline(userId, editingDeadline.getId());
                dismiss();
            }
        });
        binding.btnAddSubjectTag.setOnClickListener(v -> {
            dismiss();
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    .navigate(R.id.nav_subjects);
        });
    }

    private void setupPriorityButtons() {
        binding.btnPriorityHigh.setOnClickListener(v -> selectPriority("HIGH"));
        binding.btnPriorityMedium.setOnClickListener(v -> selectPriority("MEDIUM"));
        binding.btnPriorityLow.setOnClickListener(v -> selectPriority("LOW"));
    }

    private void selectPriority(String priority) {
        selectedPriority = priority;
        binding.btnPriorityHigh.setSelected("HIGH".equals(priority));
        binding.btnPriorityMedium.setSelected("MEDIUM".equals(priority));
        binding.btnPriorityLow.setSelected("LOW".equals(priority));
    }

    private void observeSubjects() {
        subjectViewModel.subjects.observe(getViewLifecycleOwner(), subjects -> {
            binding.chipGroupSubjects.removeAllViews();
            if (subjects == null) return;
            for (Subject s : subjects) {
                Chip chip = new Chip(requireContext());
                chip.setText(s.getName());
                chip.setCheckable(true);
                chip.setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(),
                        R.color.chip_subject_bg));
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                chip.setChipStrokeWidth(0);
                chip.setOnCheckedChangeListener((button, isChecked) -> {
                    if (isChecked) {
                        selectedSubjectId = s.getId();
                        selectedSubjectName = s.getName();
                        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                    } else {
                        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                    }
                });
                if (s.getId() != null && s.getId().equals(selectedSubjectId)) {
                    chip.setChecked(true);
                }
                binding.chipGroupSubjects.addView(chip);
            }
        });
    }

    private void showDateTimePicker() {
        DateTimePickerSheet.newInstance(
                selectedDateTime.getTimeInMillis(),
                hasSpecificTime,
                reminderMinutes
        ).show(getChildFragmentManager(), "datetime_picker");
    }

    private void showReminderPicker() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nhắc nhở")
                .setSingleChoiceItems(ReminderUtils.REMINDER_LABELS,
                        ReminderUtils.indexOf(reminderMinutes),
                        (dialog, which) -> {
                            reminderMinutes = ReminderUtils.REMINDER_VALUES[which];
                            binding.tvReminderValue.setText(ReminderUtils.REMINDER_LABELS[which]);
                            dialog.dismiss();
                        })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateDateTimeLabels() {
        if (hasSpecificTime) {
            binding.tvDeadlineValue.setText(DateUtils.formatDeadlinePicker(selectedDateTime));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("d 'Tháng' M", new Locale("vi"));
            binding.tvDeadlineValue.setText(sdf.format(selectedDateTime.getTime()) + " • Cả ngày");
        }
    }

    private void prefillForm() {
        binding.etTitle.setText(editingDeadline.getTitle());
        binding.etDescription.setText(editingDeadline.getDescription());
        if (editingDeadline.getDueDate() != null) {
            selectedDateTime.setTime(editingDeadline.getDueDate().toDate());
            updateDateTimeLabels();
        }
        reminderMinutes = editingDeadline.getReminderMinutes() > 0
                ? editingDeadline.getReminderMinutes() : 60;
        binding.tvReminderValue.setText(ReminderUtils.getLabel(reminderMinutes));
        selectedSubjectId = editingDeadline.getSubjectId() != null ? editingDeadline.getSubjectId() : "";
        selectedSubjectName = editingDeadline.getSubjectName() != null ? editingDeadline.getSubjectName() : "";
        selectPriority(editingDeadline.getPriority() != null ? editingDeadline.getPriority() : "MEDIUM");
        binding.tvHeaderTitle.setText("Chỉnh sửa Task");
        binding.btnSave.setText("Lưu thay đổi");
    }

    private Deadline buildDeadlineFromForm() {
        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        Timestamp dueDate = new Timestamp(selectedDateTime.getTime());
        Deadline d = new Deadline(title, desc, selectedSubjectId, selectedSubjectName,
                dueDate, selectedPriority, "TODO");
        d.setReminderMinutes(reminderMinutes);
        return d;
    }

    private void saveDeadline() {
        String title = binding.etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            binding.etTitle.setError("Nhập tên nhiệm vụ");
            return;
        }

        Timestamp dueDate = new Timestamp(selectedDateTime.getTime());
        String desc = binding.etDescription.getText().toString().trim();

        if (editingDeadline == null) {
            pendingSchedule = buildDeadlineFromForm();
            deadlineViewModel.addDeadline(userId, pendingSchedule);
        } else {
            editingDeadline.setTitle(title);
            editingDeadline.setDescription(desc);
            editingDeadline.setDueDate(dueDate);
            editingDeadline.setPriority(selectedPriority);
            editingDeadline.setSubjectId(selectedSubjectId);
            editingDeadline.setSubjectName(selectedSubjectName);
            editingDeadline.setReminderMinutes(reminderMinutes);
            editingDeadline.setUpdatedAt(Timestamp.now());
            deadlineViewModel.updateDeadline(userId, editingDeadline);
            DeadlineScheduler.scheduleReminder(
                    requireContext().getApplicationContext(), editingDeadline);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
