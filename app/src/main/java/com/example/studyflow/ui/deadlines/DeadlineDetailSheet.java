package com.example.studyflow.ui.deadlines;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.SheetDeadlineDetailBinding;
import com.example.studyflow.utils.DateUtils;
import com.example.studyflow.viewmodel.DeadlineViewModel;
import java.util.Locale;

public class DeadlineDetailSheet extends BottomSheetDialogFragment {
    private SheetDeadlineDetailBinding binding;
    private DeadlineViewModel viewModel;
    private String userId;
    private Deadline deadline;

    public static DeadlineDetailSheet newInstance(String userId, Deadline deadline) {
        DeadlineDetailSheet sheet = new DeadlineDetailSheet();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("deadlineId", deadline.getId());
        args.putString("title", deadline.getTitle());
        args.putString("desc", deadline.getDescription());
        args.putString("subject", deadline.getSubjectName());
        args.putString("priority", deadline.getPriority());
        args.putString("status", deadline.getStatus());
        if (deadline.getDueDate() != null) {
            args.putLong("due", deadline.getDueDate().toDate().getTime());
        }
        sheet.setArguments(args);
        sheet.deadline = deadline;
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SheetDeadlineDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userId = requireArguments().getString("userId");
        viewModel = new ViewModelProvider(requireActivity()).get(DeadlineViewModel.class);
        bindData();
        setupActions();
    }

    private void bindData() {
        Bundle args = requireArguments();
        binding.tvDetailTitle.setText(args.getString("title", ""));
        String desc = args.getString("desc", "");
        if (desc != null && !desc.isEmpty()) {
            binding.tvDetailDesc.setVisibility(View.VISIBLE);
            binding.tvDetailDesc.setText(desc);
        }

        String subject = args.getString("subject", "");
        if (subject == null || subject.isEmpty()) subject = "CHUNG";
        binding.chipDetailSubject.setText(subject.toUpperCase(Locale.getDefault()));
        DeadlineAdapter.applySubjectChip(binding.chipDetailSubject, subject);

        String priority = args.getString("priority", "MEDIUM");
        DeadlineAdapter.applyPriorityChip(binding.chipDetailPriority, priority);

        long due = args.getLong("due", 0);
        if (due > 0) {
            binding.tvDetailDue.setText(DateUtils.formatDateTime(
                    new com.google.firebase.Timestamp(new java.util.Date(due))));
        }

        boolean isDone = "DONE".equals(args.getString("status"));
        if (isDone) {
            binding.tvDetailStatus.setText("Trạng thái: Hoàn thành");
            binding.tvDetailStatus.setTextColor(requireContext().getColor(R.color.status_done));
            binding.btnMarkDone.setText("Đánh dấu chưa hoàn thành");
            binding.btnMarkDone.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            requireContext().getColor(R.color.text_secondary)));
        } else {
            binding.tvDetailStatus.setText("Trạng thái: Chưa hoàn thành");
            binding.tvDetailStatus.setTextColor(requireContext().getColor(R.color.primary));
        }
    }

    private void setupActions() {
        binding.btnMarkDone.setOnClickListener(v -> {
            boolean isDone = "DONE".equals(deadline.getStatus());
            if (isDone) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận")
                        .setMessage("Đánh dấu deadline này là chưa hoàn thành?")
                        .setPositiveButton("Xác nhận", (d, w) -> {
                            deadline.setStatus("TODO");
                            deadline.setUpdatedAt(Timestamp.now());
                            viewModel.updateDeadline(userId, deadline);
                            dismiss();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Hoàn thành deadline")
                        .setMessage("Xác nhận bạn đã hoàn thành \"" + deadline.getTitle() + "\"?")
                        .setPositiveButton("Hoàn thành", (d, w) -> {
                            deadline.setStatus("DONE");
                            deadline.setUpdatedAt(Timestamp.now());
                            viewModel.updateDeadline(userId, deadline);
                            dismiss();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.btnEdit.setOnClickListener(v -> {
            dismiss();
            AddEditDeadlineSheet.newInstance(userId, deadline)
                    .show(requireActivity().getSupportFragmentManager(), "edit_from_detail");
        });

        binding.btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa deadline")
                        .setMessage("Bạn có chắc muốn xóa \"" + deadline.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            viewModel.deleteDeadline(userId, deadline.getId());
                            dismiss();
                        })
                        .setNegativeButton("Hủy", null)
                        .show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
