package com.example.studyflow.ui.subjects;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.*;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.studyflow.data.model.Subject;
import com.example.studyflow.databinding.SheetAddSubjectBinding;
import com.example.studyflow.viewmodel.SubjectViewModel;

public class AddEditSubjectSheet extends BottomSheetDialogFragment {

    private SheetAddSubjectBinding binding;
    private SubjectViewModel subjectViewModel;
    private String userId;
    private Subject editingSubject;
    private String selectedColor = "#5E92F3"; // default blue

    // Bảng màu gợi ý
    private static final String[] COLORS = {
            "#5E92F3", "#34A853", "#EA4335",
            "#FBBC04", "#9C27B0", "#FF6D00"
    };

    public static AddEditSubjectSheet newInstance(String userId,
                                                  @Nullable Subject subject) {
        AddEditSubjectSheet sheet = new AddEditSubjectSheet();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        sheet.setArguments(args);
        sheet.editingSubject = subject;
        return sheet;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = SheetAddSubjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = requireArguments().getString("userId");
        subjectViewModel = new ViewModelProvider(requireActivity())
                .get(SubjectViewModel.class);

        buildColorPicker();
        setupButtons();

        if (editingSubject != null) {
            prefillForm();
            binding.tvSheetTitle.setText("Chỉnh sửa môn học");
        }
    }

    /** Tạo 6 circle color dots bằng code */
    private void buildColorPicker() {
        int sizePx  = (int) (40 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (10 * getResources().getDisplayMetrics().density);

        for (String color : COLORS) {
            FrameLayout frame = new FrameLayout(requireContext());
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMarginEnd(marginPx);
            frame.setLayoutParams(params);

            // Circle background
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor(color));
            frame.setBackground(circle);

            // Stroke khi được chọn
            frame.setOnClickListener(v -> {
                selectedColor = color;
                updateColorSelection();
            });

            binding.layoutColors.addView(frame);
        }
        updateColorSelection();
    }

    private void updateColorSelection() {
        int sizePx   = (int) (40 * getResources().getDisplayMetrics().density);
        int strokePx = (int) (3 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < binding.layoutColors.getChildCount(); i++) {
            View child = binding.layoutColors.getChildAt(i);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor(COLORS[i]));

            if (COLORS[i].equals(selectedColor)) {
                circle.setStroke((int)(3 * getResources().getDisplayMetrics().density),
                        Color.parseColor("#1A1F36"));
            }
            child.setBackground(circle);
        }
    }

    private void setupButtons() {
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> saveSubject());
    }

    private void saveSubject() {
        String name     = binding.etName.getText().toString().trim();
        String lecturer = binding.etLecturer.getText().toString().trim();
        String creditsStr = binding.etCredits.getText().toString().trim();

        if (name.isEmpty()) {
            binding.tilName.setError("Vui lòng nhập tên môn học"); return;
        }

        int credits = 0;
        try { credits = Integer.parseInt(creditsStr); } catch (Exception ignored) {}

        if (editingSubject == null) {
            Subject s = new Subject(name, lecturer, credits, selectedColor);
            subjectViewModel.addSubject(userId, s);
        } else {
            editingSubject.setName(name);
            editingSubject.setLecturer(lecturer);
            editingSubject.setCredits(credits);
            editingSubject.setColorTag(selectedColor);
            subjectViewModel.updateSubject(userId, editingSubject);
        }
        dismiss();
    }

    private void prefillForm() {
        binding.etName.setText(editingSubject.getName());
        binding.etLecturer.setText(editingSubject.getLecturer());
        binding.etCredits.setText(String.valueOf(editingSubject.getCredits()));
        selectedColor = editingSubject.getColorTag();
        updateColorSelection();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}