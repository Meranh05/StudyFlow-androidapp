package com.example.studyflow.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import androidx.activity.result.*;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.*;
import com.example.studyflow.R;
import com.example.studyflow.databinding.FragmentProfileBinding;
import com.example.studyflow.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private String userId;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    uploadAvatar(imageUri);
                }
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        userId = user.getUid();

        loadUserData(user);
        setupClickListeners();
    }

    private void loadUserData(FirebaseUser user) {
        binding.tvDisplayName.setText(
                user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
        binding.tvEmail.setText(user.getEmail());

        // Load avatar
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(binding.ivAvatar);
        }

        // Load notification setting từ Firestore
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean notifEnabled = doc.getBoolean("notificationsEnabled");
                        binding.switchNotifications.setChecked(
                                notifEnabled != null ? notifEnabled : true);
                    }
                });
    }

    private void setupClickListeners() {
        // Change avatar
        binding.fabChangeAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Notification toggle → lưu Firestore
        binding.switchNotifications.setOnCheckedChangeListener((btn, isChecked) -> {
            db.collection("users").document(userId)
                    .update("notificationsEnabled", isChecked);
        });

        // Edit profile
        binding.itemEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Logout
        binding.btnLogout.setOnClickListener(v ->
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage(getString(R.string.confirm_logout))
                        .setPositiveButton("Đăng xuất", (d, w) -> {
                            auth.signOut();
                            startActivity(new Intent(requireContext(), LoginActivity.class));
                            requireActivity().finish();
                        })
                        .setNegativeButton("Hủy", null)
                        .show()
        );
    }

    private void uploadAvatar(Uri imageUri) {
        if (imageUri == null) return;

        StorageReference ref = storage.getReference()
                .child("avatars/" + userId + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            // Cập nhật Firebase Auth profile
                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri).build();
                            auth.getCurrentUser().updateProfile(request);

                            // Cập nhật Firestore
                            db.collection("users").document(userId)
                                    .update("avatarUrl", downloadUri.toString());

                            // Load ảnh mới vào UI
                            Glide.with(this).load(downloadUri)
                                    .into(binding.ivAvatar);
                        })
                )
                .addOnFailureListener(e ->
                        com.google.android.material.snackbar.Snackbar
                                .make(binding.getRoot(), "Tải ảnh thất bại", 2000).show()
                );
    }

    private void showEditProfileDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        com.google.android.material.textfield.TextInputEditText input =
                new com.google.android.material.textfield.TextInputEditText(requireContext());
        input.setText(user.getDisplayName());
        input.setPadding(48, 24, 48, 24);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Đổi tên hiển thị")
                .setView(input)
                .setPositiveButton("Lưu", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName).build();
                        user.updateProfile(request).addOnSuccessListener(v -> {
                            binding.tvDisplayName.setText(newName);
                            db.collection("users").document(userId)
                                    .update("displayName", newName);
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}