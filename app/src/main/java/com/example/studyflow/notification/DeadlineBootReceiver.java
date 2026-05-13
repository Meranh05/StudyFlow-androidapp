package com.example.studyflow.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.studyflow.utils.AppPreferences;

public class DeadlineBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        String userId = AppPreferences.getLastUserId(context);
        if (userId == null || userId.isEmpty()) return;

        Data data = new Data.Builder().putString(RescheduleDeadlinesWorker.KEY_USER_ID, userId).build();
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(RescheduleDeadlinesWorker.class)
                .setInputData(data)
                .build();
        WorkManager.getInstance(context).enqueue(work);
    }
}
