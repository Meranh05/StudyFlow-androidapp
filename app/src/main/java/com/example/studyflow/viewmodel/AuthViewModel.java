package com.example.studyflow.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.example.studyflow.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository repo = new AuthRepository();
    public final MutableLiveData<String> loginResult = new MutableLiveData<>();
    public final MutableLiveData<String> registerResult = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
    }

    public void login(String email, String password) {
        repo.login(email, password, loginResult);
    }

    public void loginWithGoogle(GoogleSignInAccount account) {
        repo.loginWithGoogle(account, loginResult);
    }

    public void register(String email, String password, String displayName) {
        repo.register(email, password, displayName, registerResult);
    }

    public void resetPassword(String email, MutableLiveData<String> result) {
        repo.resetPassword(email, result);
    }

    public boolean isLoggedIn() {
        return repo.isLoggedIn();
    }

    public String getCurrentUserId() {
        return repo.getCurrentUserId();
    }
}