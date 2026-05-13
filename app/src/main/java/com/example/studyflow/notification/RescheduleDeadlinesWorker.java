package com.example.studyflow.notification;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.studyflow.data.model.Deadline;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Reschedules deadline alarms after device reboot. */
public class RescheduleDeadlinesWorker extends Worker {

    public static final String KEY_USER_ID = "user_id";

    public RescheduleDeadlinesWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override
    public Result doWork() {
        String userId = getInputData().getString(KEY_USER_ID);
        if (userId == null || userId.isEmpty()) return Result.failure();

        try {
            List<Deadline> deadlines = new ArrayList<>();
            for (DocumentSnapshot doc : Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("users").document(userId)
                            .collection("deadlines")
                            .get(),
                    30, TimeUnit.SECONDS).getDocuments()) {
                Deadline d = doc.toObject(Deadline.class);
                if (d != null) {
                    d.setId(doc.getId());
                    deadlines.add(d);
                }
            }

            DeadlineScheduler.rescheduleAll(getApplicationContext(), deadlines);
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
