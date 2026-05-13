package com.example.studyflow.ui.subjects;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.studyflow.data.model.Subject;
import com.example.studyflow.databinding.FragmentSubjectsBinding;
import com.example.studyflow.viewmodel.*;
import com.google.firebase.auth.FirebaseAuth;
import java.util.*;

public class SubjectsFragment extends Fragment {
    private FragmentSubjectsBinding binding;
    private SubjectViewModel subjectViewModel;
    private SubjectAdapter adapter;
    private String userId;
    private List<Subject> allSubjects = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSubjectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        subjectViewModel = new ViewModelProvider(this).get(SubjectViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        setupFab();

        subjectViewModel.startListening(userId);
    }

    private void setupRecyclerView() {
        adapter = new SubjectAdapter(new SubjectAdapter.OnSubjectClickListener() {
            @Override
            public void onClick(Subject subject) {
                SubjectDetailSheet.newInstance(userId, subject)
                        .show(getChildFragmentManager(), "subject_detail");
            }

            @Override
            public void onEdit(Subject subject) {
                AddEditSubjectSheet sheet = AddEditSubjectSheet.newInstance(userId, subject);
                sheet.show(getChildFragmentManager(), "edit_subject");
            }

            @Override
            public void onDelete(Subject subject) {
                confirmDeleteSubject(subject, null);
            }
        });

        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSubjects.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder from,
                                  @NonNull RecyclerView.ViewHolder to) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) {
                int position = holder.getBindingAdapterPosition();
                Subject subject = adapter.getSubjectAt(position);
                if (subject == null) {
                    adapter.notifyItemChanged(position);
                    return;
                }
                confirmDeleteSubject(subject, () -> adapter.notifyItemChanged(position));
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvSubjects);
    }

    private void confirmDeleteSubject(Subject subject, @Nullable Runnable onCancel) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa môn học?")
                .setMessage("Xóa \"" + subject.getName() + "\" sẽ không thể hoàn tác.")
                .setPositiveButton("Xóa", (d, w) ->
                        subjectViewModel.deleteSubject(userId, subject.getId()))
                .setNegativeButton("Hủy", (d, w) -> {
                    if (onCancel != null) onCancel.run();
                })
                .setOnCancelListener(d -> {
                    if (onCancel != null) onCancel.run();
                })
                .show();
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterSubjects(s.toString().trim());
            }
        });
    }

    private void filterSubjects(String query) {
        if (query.isEmpty()) {
            adapter.submitList(new ArrayList<>(allSubjects));
            updateSummary(allSubjects);
            updateEmptyState(false, allSubjects.isEmpty());
            return;
        }
        List<Subject> filtered = new ArrayList<>();
        String q = query.toLowerCase(Locale.ROOT);
        for (Subject s : allSubjects) {
            String name = s.getName() != null ? s.getName().toLowerCase(Locale.ROOT) : "";
            String lecturer = s.getLecturer() != null ? s.getLecturer().toLowerCase(Locale.ROOT) : "";
            if (name.contains(q) || lecturer.contains(q)) {
                filtered.add(s);
            }
        }
        adapter.submitList(filtered);
        updateSummary(filtered);
        updateEmptyState(true, filtered.isEmpty());
    }

    private void setupObservers() {
        subjectViewModel.subjects.observe(getViewLifecycleOwner(), subjects -> {
            binding.progressBar.setVisibility(View.GONE);
            allSubjects = subjects != null ? subjects : new ArrayList<>();
            String query = binding.etSearch.getText().toString().trim();
            if (query.isEmpty()) {
                adapter.submitList(new ArrayList<>(allSubjects));
                updateSummary(allSubjects);
                updateEmptyState(false, allSubjects.isEmpty());
            } else {
                filterSubjects(query);
            }
        });
    }

    private void updateSummary(List<Subject> list) {
        int count = list.size();
        int credits = 0;
        for (Subject s : list) credits += s.getCredits();

        binding.tvStatCount.setText(String.valueOf(count));
        binding.tvStatCredits.setText(String.valueOf(credits));
    }

    private void setupFab() {
        binding.fabAddSubject.setOnClickListener(v -> {
            AddEditSubjectSheet sheet = AddEditSubjectSheet.newInstance(userId, null);
            sheet.show(getChildFragmentManager(), "add_subject");
        });
    }

    private void updateEmptyState(boolean isSearch, boolean isEmpty) {
        binding.layoutEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvSubjects.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (!isEmpty) return;

        if (isSearch) {
            binding.layoutEmpty.tvEmptyTitle.setText("Không tìm thấy");
            binding.layoutEmpty.tvEmptySubtitle.setText("Thử từ khóa khác hoặc xóa ô tìm kiếm");
        } else {
            binding.layoutEmpty.tvEmptyTitle.setText("Chưa có môn học");
            binding.layoutEmpty.tvEmptySubtitle.setText("Nhấn + để thêm môn học mới");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
