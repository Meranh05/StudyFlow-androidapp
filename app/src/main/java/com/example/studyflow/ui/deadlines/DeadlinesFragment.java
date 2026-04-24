package com.example.studyflow.ui.deadlines;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.FragmentDeadlinesBinding;
import com.example.studyflow.notification.DeadlineScheduler;
import com.example.studyflow.viewmodel.DeadlineViewModel;
import com.example.studyflow.viewmodel.SubjectViewModel;
import java.util.*;

public class DeadlinesFragment extends Fragment {
    private FragmentDeadlinesBinding binding;
    private DeadlineViewModel deadlineViewModel;
    private SubjectViewModel subjectViewModel;
    private DeadlineAdapter adapter;
    private String userId;
    private List<Deadline> allDeadlines = new ArrayList<>();
    private String currentFilter = "ALL";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeadlinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        deadlineViewModel = new ViewModelProvider(requireActivity()).get(DeadlineViewModel.class);
        subjectViewModel = new ViewModelProvider(requireActivity()).get(SubjectViewModel.class);

        setupRecyclerView();
        setupFilterChips();
        setupObservers();
        setupFab();

        deadlineViewModel.startListening(userId);
        subjectViewModel.startListening(userId);
    }

    private void setupRecyclerView() {
        adapter = new DeadlineAdapter(new DeadlineAdapter.OnDeadlineClickListener() {
            @Override
            public void onEdit(Deadline deadline) {
                AddEditDeadlineSheet sheet =
                        AddEditDeadlineSheet.newInstance(userId, deadline);
                sheet.show(getChildFragmentManager(), "edit_deadline");
            }

            @Override
            public void onDelete(Deadline deadline) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa deadline?")
                        .setMessage("Xóa \"" + deadline.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            deadlineViewModel.deleteDeadline(userId, deadline.getId());
                            DeadlineScheduler.cancelReminder(requireContext(), deadline.getId());
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onStatusChange(Deadline deadline, String newStatus) {
                deadline.setStatus(newStatus);
                deadlineViewModel.updateDeadline(userId, deadline);
            }
        });

        binding.rvDeadlines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDeadlines.setAdapter(adapter);
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == binding.chipAll.getId())         currentFilter = "ALL";
            else if (id == binding.chipTodo.getId())   currentFilter = "TODO";
            else if (id == binding.chipInProgress.getId()) currentFilter = "IN_PROGRESS";
            else if (id == binding.chipDone.getId())   currentFilter = "DONE";
            else if (id == binding.chipHigh.getId())   currentFilter = "HIGH";
            applyFilter();
        });
    }

    private void applyFilter() {
        List<Deadline> filtered = new ArrayList<>();
        for (Deadline d : allDeadlines) {
            switch (currentFilter) {
                case "ALL":         filtered.add(d); break;
                case "TODO":        if ("TODO".equals(d.getStatus())) filtered.add(d); break;
                case "IN_PROGRESS": if ("IN_PROGRESS".equals(d.getStatus())) filtered.add(d); break;
                case "DONE":        if ("DONE".equals(d.getStatus())) filtered.add(d); break;
                case "HIGH":        if ("HIGH".equals(d.getPriority())) filtered.add(d); break;
            }
        }
        adapter.submitList(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void setupObservers() {
        deadlineViewModel.deadlines.observe(getViewLifecycleOwner(), deadlines -> {
            binding.progressBar.setVisibility(View.GONE);
            allDeadlines = deadlines;
            applyFilter();
        });
    }

    private void setupFab() {
        binding.fabAddDeadline.setOnClickListener(v -> {
            AddEditDeadlineSheet sheet = AddEditDeadlineSheet.newInstance(userId, null);
            sheet.show(getChildFragmentManager(), "add_deadline");
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvDeadlines.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}