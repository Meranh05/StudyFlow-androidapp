package com.example.studyflow.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.example.studyflow.R;
import com.example.studyflow.databinding.ActivityLoginBinding;
import com.example.studyflow.ui.main.MainActivity;
import com.example.studyflow.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Nếu đã đăng nhập → vào thẳng MainActivity
        if (authViewModel.isLoggedIn()) {
            startMain();
            return;
        }

        setupGoogleSignIn();
        setupObservers();
        setupClickListeners();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupObservers() {
        authViewModel.loginResult.observe(this, result -> {
            setLoading(false);
            if ("success".equals(result)) {
                startMain();
            } else if (result != null && result.startsWith("error:")) {
                binding.tilPassword.setError(getErrorMessage(result.substring(6)));
            }
        });
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.btnGoogle.setOnClickListener(v -> signInWithGoogle());
        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Reset errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        // Validate
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Vui lòng nhập email"); return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email)); return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu"); return;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_short)); return;
        }

        setLoading(true);
        authViewModel.login(email, password);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                setLoading(true);
                authViewModel.loginWithGoogle(account);
            } catch (ApiException e) {
                binding.tilPassword.setError("Google sign-in thất bại: " + e.getMessage());
            }
        }
    }

    private void setLoading(boolean loading) {
        binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnGoogle.setEnabled(!loading);
    }

    private String getErrorMessage(String firebaseError) {
        if (firebaseError.contains("user-not-found") ||
                firebaseError.contains("wrong-password")) {
            return "Email hoặc mật khẩu không đúng";
        } else if (firebaseError.contains("too-many-requests")) {
            return "Quá nhiều lần thử, vui lòng thử lại sau";
        }
        return getString(R.string.error_general);
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}