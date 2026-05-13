package com.example.studyflow.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.*;
import com.example.studyflow.data.model.User;
import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface UserCallback {
        void onResult(User user);
    }

    public void getUser(String userId, UserCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onResult(doc.toObject(User.class));
                    } else {
                        callback.onResult(null);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    public void updateProfile(String userId, String displayName, String phone, String bio) {
        Map<String, Object> data = new HashMap<>();
        data.put("displayName", displayName);
        data.put("phone", phone);
        data.put("bio", bio);
        db.collection("users").document(userId).set(data, SetOptions.merge());
    }

    public void updateAvatarUrl(String userId, String avatarUrl) {
        db.collection("users").document(userId)
                .set(Map.of("avatarUrl", avatarUrl), SetOptions.merge());
    }

    public void updateNotificationsEnabled(String userId, boolean enabled) {
        db.collection("users").document(userId)
                .set(Map.of("notificationsEnabled", enabled), SetOptions.merge());
    }

    public void updateDefaultReminderMinutes(String userId, int minutes) {
        db.collection("users").document(userId)
                .set(Map.of("defaultReminderMinutes", minutes), SetOptions.merge());
    }
}
