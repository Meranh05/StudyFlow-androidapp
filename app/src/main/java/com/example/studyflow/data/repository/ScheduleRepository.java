package com.example.studyflow.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.*;
import com.example.studyflow.data.firebase.FirebaseHelper;
import com.example.studyflow.data.model.Schedule;
import java.util.*;

public class ScheduleRepository {

    private final FirebaseHelper fb = FirebaseHelper.getInstance();
    private ListenerRegistration listenerRegistration;

    public void listenSchedules(MutableLiveData<List<Schedule>> liveData) {
        listenerRegistration = fb.schedulesCol()
                .orderBy("dayOfWeek")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Schedule> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Schedule s = doc.toObject(Schedule.class);
                        if (s != null) {
                            s.setId(doc.getId());
                            list.add(s);
                        }
                    }
                    liveData.postValue(list);
                });
    }

    public void addSchedule(Schedule schedule, MutableLiveData<String> result) {
        fb.schedulesCol().add(schedule)
                .addOnSuccessListener(ref -> result.setValue("success"))
                .addOnFailureListener(e  -> result.setValue("error:" + e.getMessage()));
    }

    public void deleteSchedule(String scheduleId, MutableLiveData<String> result) {
        fb.schedulesCol().document(scheduleId).delete()
                .addOnSuccessListener(v -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void removeListener() {
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}