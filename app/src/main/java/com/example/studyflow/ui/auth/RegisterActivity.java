package com.example.studyflow.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.studyflow.databinding.ActivityRegisterBinding;
import com.example.studyflow.ui.main.MainActivity;
import com.example.studyflow.utils.ValidationUtils;
import com.example.studyflow.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        authViewModel.registerResult.observe(this, result -> {
            setLoading(false);
            if ("success".equals(result)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else if (result != null && result.startsWith("error:")) {
                binding.tilEmail.setError(mapError(result.substring(6)));
            }
        });
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();

        // Reset
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        // Validate
        String nameErr = ValidationUtils.validateRequired(name, "Họ tên");
        if (nameErr != null) { binding.tilName.setError(nameErr); return; }

        String emailErr = ValidationUtils.validateEmail(email);
        if (emailErr != null) { binding.tilEmail.setError(emailErr); return; }

        String passErr = ValidationUtils.validatePassword(pass);
        if (passErr != null) { binding.tilPassword.setError(passErr); return; }

        if (!pass.equals(confirm)) {
            binding.tilConfirmPassword.setError("Mật khẩu không khớp"); return;
        }

        setLoading(true);
        authViewModel.register(email, pass, name);
    }

    private void setLoading(boolean loading) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
    }

    private String mapError(String err) {
        if (err.contains("email-already-in-use")) return "Email này đã được đăng ký";
        if (err.contains("weak-password")) return "Mật khẩu quá yếu";
        return "Đăng ký thất bại, thử lại";
    }
}