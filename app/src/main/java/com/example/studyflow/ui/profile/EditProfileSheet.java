package com.example.studyflow.ui.profile;

import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.*;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.*;
import com.example.studyflow.R;
import com.example.studyflow.data.repository.UserRepository;
import com.example.studyflow.databinding.SheetEditProfileBinding;

public class EditProfileSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "edit_profile_result";

    private SheetEditProfileBinding binding;
    private final UserRepository userRepo = new UserRepository();
    private String userId;
    private String initialName = "";
    private String initialPhone = "";
    private String initialBio = "";

    public static EditProfileSheet newInstance(String name, String phone, String bio) {
        EditProfileSheet sheet = new EditProfileSheet();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("phone", phone);
        args.putString("bio", bio);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SheetEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            dismiss();
            return;
        }
        userId = user.getUid();

        Bundle args = getArguments();
        if (args != null) {
            initialName = args.getString("name", "");
            initialPhone = args.getString("phone", "");
            initialBio = args.getString("bio", "");
        }

        binding.etName.setText(initialName);
        binding.etPhone.setText(initialPhone);
        binding.etBio.setText(initialBio);

        binding.btnClose.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Nhập họ tên");
            return;
        }

        binding.btnSave.setEnabled(false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(request).addOnCompleteListener(task -> {
            userRepo.updateProfile(userId, name, phone, bio);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, new Bundle());
            Toast.makeText(requireContext(), "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
