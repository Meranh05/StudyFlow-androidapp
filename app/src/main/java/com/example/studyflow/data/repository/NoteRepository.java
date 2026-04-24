package com.example.studyflow.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import com.example.studyflow.data.firebase.FirebaseHelper;
import com.example.studyflow.data.model.Note;
import java.util.*;

public class NoteRepository {

    private final FirebaseHelper fb = FirebaseHelper.getInstance();
    private ListenerRegistration listenerRegistration;

    // ── Realtime listener ──────────────────────────────────
    public void listenNotes(String subjectId,
                            MutableLiveData<List<Note>> liveData) {

        Query query = fb.notesCol()
                .orderBy("updatedAt", Query.Direction.DESCENDING);

        // Nếu truyền subjectId thì lọc theo môn
        if (subjectId != null && !subjectId.isEmpty()) {
            query = query.whereEqualTo("subjectId", subjectId);
        }

        listenerRegistration = query.addSnapshotListener((snapshots, error) -> {
            if (error != null || snapshots == null) return;
            List<Note> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Note n = doc.toObject(Note.class);
                if (n != null) {
                    n.setId(doc.getId());
                    list.add(n);
                }
            }
            liveData.postValue(list);
        });
    }

    public void addNote(Note note, MutableLiveData<String> result) {
        fb.notesCol().add(note)
                .addOnSuccessListener(ref -> result.setValue("success:" + ref.getId()))
                .addOnFailureListener(e  -> result.setValue("error:" + e.getMessage()));
    }

    public void updateNote(Note note, MutableLiveData<String> result) {
        note.setUpdatedAt(Timestamp.now());
        fb.notesCol().document(note.getId()).set(note)
                .addOnSuccessListener(v -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void deleteNote(String noteId, MutableLiveData<String> result) {
        fb.notesCol().document(noteId).delete()
                .addOnSuccessListener(v -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void removeListener() {
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}