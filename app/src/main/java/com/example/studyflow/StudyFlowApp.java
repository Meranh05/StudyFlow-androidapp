package com.example.studyflow;

import android.app.Application;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.google.firebase.FirebaseApp;
import com.example.studyflow.notification.NotificationHelper;

/**
 * Application class — khởi tạo Firebase và WorkManager
 */
public class StudyFlowApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        NotificationHelper.ensureChannels(this);
    }
}