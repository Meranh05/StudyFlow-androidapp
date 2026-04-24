package com.example.studyflow;

import android.app.Application;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.google.firebase.FirebaseApp;

/**
 * Application class — khởi tạo Firebase và WorkManager
 */
public class StudyFlowApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo Firebase
        FirebaseApp.initializeApp(this);

    }
}