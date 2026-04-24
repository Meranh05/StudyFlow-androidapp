package com.example.studyflow.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Singleton helper — cung cấp Firebase instances
 * và shortcut tới các collection của user hiện tại.
 */
public class FirebaseHelper {

    // ── Singleton ──────────────────────────────────────────
    private static FirebaseHelper instance;

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    private FirebaseHelper() {
        auth    = FirebaseAuth.getInstance();
        db      = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) instance = new FirebaseHelper();
        return instance;
    }

    // ── Auth shortcuts ─────────────────────────────────────
    public FirebaseAuth getAuth() { return auth; }

    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }

    public String getCurrentUserId() {
        FirebaseUser u = auth.getCurrentUser();
        return u != null ? u.getUid() : null;
    }

    public boolean isLoggedIn() { return auth.getCurrentUser() != null; }

    // ── Firestore shortcuts ────────────────────────────────
    public FirebaseFirestore getDb() { return db; }

    /** users/{uid} */
    public com.google.firebase.firestore.DocumentReference userDoc() {
        return db.collection("users").document(getCurrentUserId());
    }

    /** users/{uid}/subjects */
    public CollectionReference subjectsCol() {
        return userDoc().collection("subjects");
    }

    /** users/{uid}/deadlines */
    public CollectionReference deadlinesCol() {
        return userDoc().collection("deadlines");
    }

    /** users/{uid}/schedules */
    public CollectionReference schedulesCol() {
        return userDoc().collection("schedules");
    }

    /** users/{uid}/notes */
    public CollectionReference notesCol() {
        return userDoc().collection("notes");
    }

    // ── Storage shortcuts ──────────────────────────────────
    public FirebaseStorage getStorage() { return storage; }

    /** avatars/{uid}.jpg */
    public StorageReference avatarRef() {
        return storage.getReference()
                .child("avatars/" + getCurrentUserId() + ".jpg");
    }

    /** note_attachments/{uid}/{filename} */
    public StorageReference noteAttachmentRef(String filename) {
        return storage.getReference()
                .child("note_attachments/" + getCurrentUserId() + "/" + filename);
    }
}