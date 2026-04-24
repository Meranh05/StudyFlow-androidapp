package com.example.studyflow.ui.notes;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.firebase.Timestamp;
import com.example.studyflow.data.model.Note;
import com.example.studyflow.databinding.ActivityNoteDetailBinding;
import com.example.studyflow.viewmodel.NoteViewModel;

public class NoteDetailActivity extends AppCompatActivity {

    private ActivityNoteDetailBinding binding;
    private NoteViewModel noteViewModel;
    private String noteId;        // null = tạo mới
    private String subjectId;
    private String subjectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // Nhận data từ Intent
        noteId      = getIntent().getStringExtra("noteId");
        subjectId   = getIntent().getStringExtra("subjectId");
        subjectName = getIntent().getStringExtra("subjectName");

        // Setup toolbar back button
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.getNavigationIcon()
                .setAutoMirrored(true); // back arrow

        if (noteId != null) {
            // Edit mode — pre-fill
            binding.etNoteTitle.setText(getIntent().getStringExtra("noteTitle"));
            binding.etNoteContent.setText(getIntent().getStringExtra("noteContent"));
            binding.chipSubject.setText(subjectName != null ? subjectName : "");
            binding.tvToolbarTitle.setText("Chỉnh sửa ghi chú");
        } else {
            binding.tvToolbarTitle.setText("Ghi chú mới");
            if (subjectName != null) binding.chipSubject.setText(subjectName);
        }

        // Ẩn chip nếu không có môn
        if (subjectName == null || subjectName.isEmpty()) {
            binding.chipSubject.setVisibility(android.view.View.GONE);
        }

        binding.btnSaveNote.setOnClickListener(v -> saveNote());

        // Observe kết quả
        noteViewModel.operationResult.observe(this, result -> {
            if (result != null && result.startsWith("success")) finish();
        });
    }

    private void saveNote() {
        String title   = binding.etNoteTitle.getText().toString().trim();
        String content = binding.etNoteContent.getText().toString().trim();

        if (content.isEmpty() && title.isEmpty()) {
            // Không lưu note trống
            finish();
            return;
        }

        if (noteId == null) {
            // Tạo mới
            Note note = new Note(
                    title.isEmpty() ? "Ghi chú không có tiêu đề" : title,
                    content,
                    subjectId   != null ? subjectId   : "",
                    subjectName != null ? subjectName : ""
            );
            noteViewModel.addNote(note);
        } else {
            // Cập nhật
            Note note = new Note(title, content, subjectId, subjectName);
            note.setId(noteId);
            note.setUpdatedAt(Timestamp.now());
            noteViewModel.updateNote(note);
        }
    }
}