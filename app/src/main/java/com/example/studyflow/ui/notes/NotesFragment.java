package com.example.studyflow.ui.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.example.studyflow.data.model.*;
import com.example.studyflow.databinding.FragmentNotesBinding;
import com.example.studyflow.viewmodel.*;
import java.util.*;

public class NotesFragment extends Fragment {

    private FragmentNotesBinding binding;
    private NoteViewModel noteViewModel;
    private SubjectViewModel subjectViewModel;
    private NoteAdapter adapter;
    private String userId;
    private String filterSubjectId = null; // null = tất cả

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteViewModel    = new ViewModelProvider(this).get(NoteViewModel.class);
        subjectViewModel = new ViewModelProvider(requireActivity()).get(SubjectViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupFab();

        noteViewModel.startListening(null); // Load tất cả
        subjectViewModel.startListening(userId);
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onClick(Note note) {
                // Mở NoteDetailActivity để edit
                Intent intent = new Intent(requireContext(), NoteDetailActivity.class);
                intent.putExtra("noteId", note.getId());
                intent.putExtra("noteTitle", note.getTitle());
                intent.putExtra("noteContent", note.getContent());
                intent.putExtra("subjectName", note.getSubjectName());
                intent.putExtra("subjectId", note.getSubjectId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Note note) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Xóa ghi chú?")
                        .setMessage("\"" + note.getTitle() + "\"")
                        .setPositiveButton("Xóa", (d, w) -> noteViewModel.deleteNote(note.getId()))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.rvNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotes.setAdapter(adapter);
    }

    private void setupObservers() {
        noteViewModel.notes.observe(getViewLifecycleOwner(), notes -> {
            binding.progressBar.setVisibility(View.GONE);
            adapter.submitList(notes);
            updateEmptyState(notes.isEmpty());
        });

        // Build subject filter chips động
        subjectViewModel.subjects.observe(getViewLifecycleOwner(), subjects -> {
            binding.chipGroupSubjects.removeAllViews();

            // Chip "Tất cả"
            Chip chipAll = new Chip(requireContext());
            chipAll.setText("Tất cả");
            chipAll.setCheckable(true);
            chipAll.setChecked(true);
            chipAll.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    filterSubjectId = null;
                    noteViewModel.startListening(null);
                }
            });
            binding.chipGroupSubjects.addView(chipAll);

            // Chip từng môn
            for (Subject s : subjects) {
                Chip chip = new Chip(requireContext());
                chip.setText(s.getName());
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((btn, checked) -> {
                    if (checked) {
                        filterSubjectId = s.getId();
                        noteViewModel.startListening(s.getId());
                    }
                });
                binding.chipGroupSubjects.addView(chip);
            }
        });
    }

    private void setupFab() {
        binding.fabAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NoteDetailActivity.class);
            // Truyền subjectId nếu đang filter
            if (filterSubjectId != null) {
                intent.putExtra("subjectId", filterSubjectId);
            }
            startActivity(intent);
        });
    }

    private void updateEmptyState(boolean empty) {
        binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvNotes.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}