package com.example.studyflow.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.*;
import com.example.studyflow.databinding.ActivityForgotPasswordBinding;
import com.example.studyflow.viewmodel.AuthViewModel;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private AuthViewModel authViewModel;
    private final MutableLiveData<String> resetResult = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        resetResult.observe(this, result -> {
            binding.btnSend.setEnabled(true);
            if ("success".equals(result)) {
                binding.cardSuccess.setVisibility(View.VISIBLE);
                binding.tilEmail.setError(null);
            } else if (result != null && result.startsWith("error:")) {
                binding.tilEmail.setError("Email không tồn tại hoặc có lỗi xảy ra");
            }
        });

        binding.btnSend.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            binding.tilEmail.setError(null);

            if (TextUtils.isEmpty(email)) {
                binding.tilEmail.setError("Vui lòng nhập email"); return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.setError("Email không hợp lệ"); return;
            }

            binding.btnSend.setEnabled(false);
            authViewModel.resetPassword(email, resetResult);
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }
}