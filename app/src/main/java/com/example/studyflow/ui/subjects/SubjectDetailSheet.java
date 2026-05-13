package com.example.studyflow.ui.subjects;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Subject;
import com.example.studyflow.databinding.SheetSubjectDetailBinding;
import com.example.studyflow.viewmodel.SubjectViewModel;
import java.util.Locale;

public class SubjectDetailSheet extends BottomSheetDialogFragment {

    private SheetSubjectDetailBinding binding;
    private SubjectViewModel subjectViewModel;
    private String userId;
    private Subject subject;

    public static SubjectDetailSheet newInstance(String userId, Subject subject) {
        SubjectDetailSheet sheet = new SubjectDetailSheet();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("id", subject.getId());
        args.putString("name", subject.getName());
        args.putString("lecturer", subject.getLecturer());
        args.putInt("credits", subject.getCredits());
        args.putString("colorTag", subject.getColorTag());
        sheet.setArguments(args);
        sheet.subject = subject;
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = SheetSubjectDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userId = requireArguments().getString("userId");
        subjectViewModel = new ViewModelProvider(requireActivity()).get(SubjectViewModel.class);
        bindData();
        setupActions();
    }

    private void bindData() {
        Bundle args = requireArguments();
        String name = args.getString("name", "Môn học");
        String lecturer = args.getString("lecturer", "");
        int credits = args.getInt("credits", 0);
        String colorTag = args.getString("colorTag", "#5E92F3");

        binding.tvSubjectName.setText(name);

        String initial = name.trim().isEmpty()
                ? "?"
                : name.trim().substring(0, 1).toUpperCase(Locale.ROOT);
        binding.tvInitial.setText(initial);

        binding.tvCreditsChip.setText(credits + " tín chỉ");
        binding.tvCredits.setText(credits + " tín chỉ");

        if (lecturer == null || lecturer.isEmpty()) {
            binding.tvLecturer.setText("Chưa cập nhật");
            binding.tvLecturer.setTextColor(
                    requireContext().getColor(R.color.text_hint));
        } else {
            binding.tvLecturer.setText(lecturer);
        }

        int color = parseColor(colorTag);
        applySubjectAccent(color);
    }

    private void applySubjectAccent(int color) {
        int headerBg = ColorUtils.setAlphaComponent(color, 36);
        binding.headerCard.setCardBackgroundColor(headerBg);

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setCornerRadius(18f * getResources().getDisplayMetrics().density);
        iconBg.setColor(color);
        binding.viewIcon.setBackground(iconBg);

        int chipBg = ColorUtils.setAlphaComponent(color, 40);
        GradientDrawable creditsChip = new GradientDrawable();
        creditsChip.setCornerRadius(20f * getResources().getDisplayMetrics().density);
        creditsChip.setColor(chipBg);
        binding.tvCreditsChip.setBackground(creditsChip);
        binding.tvCreditsChip.setTextColor(darkenForText(color));
    }

    private int darkenForText(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.max(0.25f, hsv[2] * 0.72f);
        return Color.HSVToColor(hsv);
    }

    private int parseColor(String hex) {
        try {
            return Color.parseColor(hex != null ? hex : "#5E92F3");
        } catch (Exception e) {
            return Color.parseColor("#5E92F3");
        }
    }

    private void setupActions() {
        binding.btnEdit.setOnClickListener(v -> {
            AddEditSubjectSheet.newInstance(userId, subject)
                    .show(requireParentFragment().getChildFragmentManager(), "edit_subject");
            dismiss();
        });

        binding.btnDelete.setOnClickListener(v -> {
            String name = requireArguments().getString("name", "môn học này");
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xóa môn học?")
                    .setMessage("Xóa \"" + name + "\" sẽ không thể hoàn tác.")
                    .setPositiveButton("Xóa", (d, w) -> {
                        subjectViewModel.deleteSubject(userId, subject.getId());
                        dismiss();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
