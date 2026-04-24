package com.example.studyflow.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.*;
import com.example.studyflow.data.model.Subject;
import java.util.ArrayList;
import java.util.List;

public class SubjectRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;

    private CollectionReference getCollection(String userId) {
        return db.collection("users").document(userId).collection("subjects");
    }

    // Realtime listener — tự động cập nhật khi Firestore thay đổi
    public void listenSubjects(String userId, MutableLiveData<List<Subject>> liveData) {
        listenerRegistration = getCollection(userId)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Subject> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Subject s = doc.toObject(Subject.class);
                        if (s != null) {
                            s.setId(doc.getId());
                            list.add(s);
                        }
                    }
                    liveData.postValue(list);
                });
    }

    public void addSubject(String userId, Subject subject, MutableLiveData<String> result) {
        getCollection(userId).add(subject)
                .addOnSuccessListener(ref -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void updateSubject(String userId, Subject subject, MutableLiveData<String> result) {
        getCollection(userId).document(subject.getId()).set(subject)
                .addOnSuccessListener(v -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void deleteSubject(String userId, String subjectId, MutableLiveData<String> result) {
        getCollection(userId).document(subjectId).delete()
                .addOnSuccessListener(v -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void removeListener() {
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}