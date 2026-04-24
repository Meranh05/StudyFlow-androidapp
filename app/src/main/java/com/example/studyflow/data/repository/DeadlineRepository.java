package com.example.studyflow.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import com.example.studyflow.data.model.Deadline;
import java.util.ArrayList;
import java.util.List;

public class DeadlineRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;

    private CollectionReference getCollection(String userId) {
        return db.collection("users").document(userId).collection("deadlines");
    }

    public void listenDeadlines(String userId, MutableLiveData<List<Deadline>> liveData) {
        listenerRegistration = getCollection(userId)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Deadline> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Deadline d = doc.toObject(Deadline.class);
                        if (d != null) {
                            d.setId(doc.getId());
                            list.add(d);
                        }
                    }
                    liveData.postValue(list);
                });
    }

    // Lấy deadline sắp tới trong 24h (dùng cho Home Dashboard)
    public void getUpcomingDeadlines(String userId, MutableLiveData<List<Deadline>> liveData) {
        Timestamp now = Timestamp.now();
        Timestamp tomorrow = new Timestamp(now.getSeconds() + 86400, 0);
        getCollection(userId)
                .whereGreaterThan("dueDate", now)
                .whereLessThan("dueDate", tomorrow)
                .whereNotEqualTo("status", "DONE")
                .orderBy("dueDate")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Deadline> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Deadline d = doc.toObject(Deadline.class);
                        if (d != null) { d.setId(doc.getId()); list.add(d); }
                    }
                    liveData.postValue(list);
                });
    }

    public void addDeadline(String userId, Deadline deadline, MutableLiveData<String> result) {
        getCollection(userId).add(deadline)
                .addOnSuccessListener(ref -> result.setValue("success:" + ref.getId()))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void updateDeadline(String userId, Deadline deadline, MutableLiveData<String> result) {
        getCollection(userId).document(deadline.getId()).set(deadline)
                .addOnSuccessListener(v -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void deleteDeadline(String userId, String deadlineId, MutableLiveData<String> result) {
        getCollection(userId).document(deadlineId).delete()
                .addOnSuccessListener(v -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void removeListener() {
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}