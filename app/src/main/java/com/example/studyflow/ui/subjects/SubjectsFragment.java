package com.example.studyflow.ui.subjects;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
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
            public void onEdit(Subject subject) {
                AddEditSubjectSheet sheet = AddEditSubjectSheet.newInstance(userId, subject);
                sheet.show(getChildFragmentManager(), "edit_subject");
            }

            @Override
            public void onDelete(Subject subject) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa môn học?")
                        .setMessage("Xóa \"" + subject.getName() + "\" sẽ không thể hoàn tác.")
                        .setPositiveButton("Xóa", (d, w) ->
                                subjectViewModel.deleteSubject(userId, subject.getId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSubjects.setAdapter(adapter);
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
            return;
        }
        List<Subject> filtered = new ArrayList<>();
        for (Subject s : allSubjects) {
            if (s.getName().toLowerCase().contains(query.toLowerCase()) ||
                    s.getLecturer().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(s);
            }
        }
        adapter.submitList(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void setupObservers() {
        subjectViewModel.subjects.observe(getViewLifecycleOwner(), subjects -> {
            binding.progressBar.setVisibility(View.GONE);
            allSubjects = subjects;
            String query = binding.etSearch.getText().toString().trim();
            if (query.isEmpty()) {
                adapter.submitList(new ArrayList<>(subjects));
                updateEmptyState(subjects.isEmpty());
            } else {
                filterSubjects(query);
            }
        });
    }

    private void setupFab() {
        binding.fabAddSubject.setOnClickListener(v -> {
            AddEditSubjectSheet sheet = AddEditSubjectSheet.newInstance(userId, null);
            sheet.show(getChildFragmentManager(), "add_subject");
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvSubjects.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}