package com.example.studyflow.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.model.Note;
import com.example.studyflow.data.repository.NoteRepository;
import java.util.List;

public class NoteViewModel extends ViewModel {

    private final NoteRepository repo = new NoteRepository();
    public final MutableLiveData<List<Note>> notes = new MutableLiveData<>();
    public final MutableLiveData<String> operationResult = new MutableLiveData<>();

    /** subjectId = null → lấy tất cả notes */
    public void startListening(String subjectId) {
        repo.listenNotes(subjectId, notes);
    }

    public void addNote(Note note) {
        repo.addNote(note, operationResult);
    }

    public void updateNote(Note note) {
        repo.updateNote(note, operationResult);
    }

    public void deleteNote(String noteId) {
        repo.deleteNote(noteId, operationResult);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repo.removeListener();
    }
}