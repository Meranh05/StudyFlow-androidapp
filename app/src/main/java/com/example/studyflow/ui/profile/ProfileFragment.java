package com.example.studyflow.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.*;
import com.example.studyflow.R;
import com.example.studyflow.data.model.User;
import com.example.studyflow.data.repository.UserRepository;
import com.example.studyflow.databinding.FragmentProfileBinding;
import com.example.studyflow.ui.auth.LoginActivity;
import com.example.studyflow.utils.AppPreferences;
import com.example.studyflow.utils.LocalAvatarStorage;
import com.example.studyflow.utils.ReminderUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth auth;
    private final UserRepository userRepo = new UserRepository();
    private String userId;
    private User currentUser;
    private int defaultReminderMinutes = 60;
    private boolean suppressNotifListener;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::saveAvatarLocally);

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

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        userId = user.getUid();
        loadUserProfile(user);
        setupClickListeners();
    }

    private void loadUserProfile(FirebaseUser authUser) {
        userRepo.getUser(userId, user -> {
            if (!isAdded()) return;
            if (user == null) {
                applyAuthFallback(authUser);
                return;
            }
            currentUser = user;
            bindProfileUi(authUser);
        });
    }

    private void bindProfileUi(FirebaseUser authUser) {
        binding.tvDisplayName.setText(
                currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()
                        ? currentUser.getDisplayName() : "Người dùng");

        if (currentUser.getBio() != null && !currentUser.getBio().isEmpty()) {
            binding.tvBio.setVisibility(View.VISIBLE);
            binding.tvBio.setText(currentUser.getBio());
        } else {
            binding.tvBio.setVisibility(View.GONE);
        }

        defaultReminderMinutes = currentUser.getDefaultReminderMinutes() > 0
                ? currentUser.getDefaultReminderMinutes() : 60;
        binding.tvDefaultReminder.setText(ReminderUtils.getLabel(defaultReminderMinutes));

        suppressNotifListener = true;
        binding.switchNotifications.setChecked(currentUser.isNotificationsEnabled());
        AppPreferences.setNotificationsEnabled(requireContext(), currentUser.isNotificationsEnabled());
        suppressNotifListener = false;

        loadAvatarImage(authUser);
    }

    private void loadAvatarImage(FirebaseUser authUser) {
        if (LocalAvatarStorage.exists(requireContext(), userId)) {
            LocalAvatarStorage.loadInto(this, userId, binding.ivAvatar);
            return;
        }
        if (authUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(authUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(binding.ivAvatar);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private void applyAuthFallback(FirebaseUser authUser) {
        binding.tvDisplayName.setText(
                authUser.getDisplayName() != null ? authUser.getDisplayName() : "Người dùng");
        binding.tvBio.setVisibility(View.GONE);
        binding.tvDefaultReminder.setText(ReminderUtils.getLabel(defaultReminderMinutes));
        loadAvatarImage(authUser);
    }

    private void setupClickListeners() {
        getChildFragmentManager().setFragmentResultListener(
                EditProfileSheet.REQUEST_KEY, this, (key, bundle) -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) loadUserProfile(user);
        });

        View.OnClickListener pickAvatar = v -> pickImage();
        binding.btnChangeAvatar.setOnClickListener(pickAvatar);
        binding.ivAvatar.setOnClickListener(pickAvatar);

        binding.itemEditProfile.setOnClickListener(v -> openEditProfile());

        binding.switchNotifications.setOnCheckedChangeListener((btn, isChecked) -> {
            if (suppressNotifListener) return;
            AppPreferences.setNotificationsEnabled(requireContext(), isChecked);
            userRepo.updateNotificationsEnabled(userId, isChecked);
        });

        binding.itemDefaultReminder.setOnClickListener(v -> showDefaultReminderPicker());

        binding.btnLogout.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage(R.string.confirm_logout)
                        .setPositiveButton("Đăng xuất", (d, w) -> {
                            auth.signOut();
                            startActivity(new android.content.Intent(requireContext(), LoginActivity.class));
                            requireActivity().finish();
                        })
                        .setNegativeButton("Hủy", null)
                        .show()
        );
    }

    private void pickImage() {
        imagePickerLauncher.launch("image/*");
    }

    private void openEditProfile() {
        String name = binding.tvDisplayName.getText().toString();
        String phone = currentUser != null && currentUser.getPhone() != null ? currentUser.getPhone() : "";
        String bio = currentUser != null && currentUser.getBio() != null ? currentUser.getBio() : "";
        EditProfileSheet.newInstance(name, phone, bio)
                .show(getChildFragmentManager(), "edit_profile");
    }

    private void showDefaultReminderPicker() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Nhắc mặc định")
                .setSingleChoiceItems(ReminderUtils.REMINDER_LABELS,
                        ReminderUtils.indexOf(defaultReminderMinutes),
                        (dialog, which) -> {
                            defaultReminderMinutes = ReminderUtils.REMINDER_VALUES[which];
                            binding.tvDefaultReminder.setText(ReminderUtils.REMINDER_LABELS[which]);
                            userRepo.updateDefaultReminderMinutes(userId, defaultReminderMinutes);
                            dialog.dismiss();
                        })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveAvatarLocally(Uri imageUri) {
        if (imageUri == null || userId == null) return;

        setAvatarUploading(true);

        try {
            byte[] imageBytes = readAndCompress(imageUri);
            LocalAvatarStorage.save(requireContext(), userId, imageBytes);

            File saved = LocalAvatarStorage.getFile(requireContext(), userId);
            if (saved != null) {
                Glide.with(this)
                        .load(saved)
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(binding.ivAvatar);
            }
            showSnack("Đã lưu ảnh đại diện trên thiết bị");
        } catch (Exception e) {
            showSnack("Lưu ảnh thất bại: " + e.getMessage());
        } finally {
            setAvatarUploading(false);
        }
    }

    private byte[] readAndCompress(Uri uri) throws Exception {
        InputStream input = requireContext().getContentResolver().openInputStream(uri);
        if (input == null) throw new Exception("Không mở được file ảnh");

        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();
        if (bitmap == null) throw new Exception("Định dạng ảnh không hợp lệ");

        int maxSize = 1024;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = Math.min(1f, (float) maxSize / Math.max(width, height));
        if (scale < 1f) {
            bitmap = Bitmap.createScaledBitmap(
                    bitmap, Math.round(width * scale), Math.round(height * scale), true);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output);
        return output.toByteArray();
    }

    private void setAvatarUploading(boolean uploading) {
        binding.progressAvatar.setVisibility(uploading ? View.VISIBLE : View.GONE);
        binding.btnChangeAvatar.setEnabled(!uploading);
        binding.ivAvatar.setEnabled(!uploading);
    }

    private void showSnack(String message) {
        if (binding != null) {
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
