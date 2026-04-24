package com.example.studyflow.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.studyflow.data.model.User;

public class AuthRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MutableLiveData<FirebaseUser> getCurrentUser() {
        MutableLiveData<FirebaseUser> liveData = new MutableLiveData<>();
        liveData.setValue(auth.getCurrentUser());
        return liveData;
    }

    public void register(String email, String password,
                         String displayName, MutableLiveData<String> result) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        // Cập nhật display name
                        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName).build();
                        firebaseUser.updateProfile(profile);

                        // Lưu user vào Firestore
                        User user = new User(firebaseUser.getUid(), displayName, email);
                        db.collection("users").document(firebaseUser.getUid())
                                .set(user);
                        result.setValue("success");
                    }
                })
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void login(String email, String password, MutableLiveData<String> result) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void loginWithGoogle(GoogleSignInAccount account, MutableLiveData<String> result) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser u = authResult.getUser();
                    if (u != null) {
                        // Tạo user doc nếu chưa có
                        db.collection("users").document(u.getUid()).get()
                                .addOnSuccessListener(doc -> {
                                    if (!doc.exists()) {
                                        User user = new User(u.getUid(),
                                                u.getDisplayName() != null ? u.getDisplayName() : "",
                                                u.getEmail() != null ? u.getEmail() : "");
                                        db.collection("users").document(u.getUid()).set(user);
                                    }
                                    result.setValue("success");
                                });
                    }
                })
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void resetPassword(String email, MutableLiveData<String> result) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> result.setValue("success"))
                .addOnFailureListener(e -> result.setValue("error:" + e.getMessage()));
    }

    public void logout() { auth.signOut(); }

    public boolean isLoggedIn() { return auth.getCurrentUser() != null; }

    public String getCurrentUserId() {
        FirebaseUser u = auth.getCurrentUser();
        return u != null ? u.getUid() : null;
    }
}