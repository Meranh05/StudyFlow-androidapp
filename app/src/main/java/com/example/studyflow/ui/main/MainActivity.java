package com.example.studyflow.ui.main;

import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import com.example.studyflow.R;
import com.example.studyflow.databinding.ActivityMainBinding;
import com.example.studyflow.notification.DeadlineScheduler;
import com.example.studyflow.notification.NotificationPermissionHelper;
import com.example.studyflow.utils.AppPreferences;
import com.example.studyflow.viewmodel.DeadlineViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private boolean permissionPromptScheduled;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                AppPreferences.setNotificationIntroCompleted(this, true);
                if (!granted && !shouldShowRequestPermissionRationale(
                        android.Manifest.permission.POST_NOTIFICATIONS)) {
                    NotificationPermissionHelper.showOpenSettingsDialog(this);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        binding.bottomNav.setOnItemSelectedListener(item -> {
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(true)
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), false, true)
                    .build();
            try {
                navController.navigate(item.getItemId(), null, options);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });

        binding.bottomNav.setOnItemReselectedListener(item ->
                navController.popBackStack(item.getItemId(), false));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            AppPreferences.setLastUserId(this, user.getUid());
            DeadlineViewModel deadlineViewModel =
                    new ViewModelProvider(this).get(DeadlineViewModel.class);
            deadlineViewModel.startListening(user.getUid());
            deadlineViewModel.deadlines.observe(this, deadlines -> {
                if (deadlines != null) {
                    DeadlineScheduler.rescheduleAll(getApplicationContext(), deadlines);
                }
            });
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        scheduleNotificationPermissionPrompt();
    }

    private void scheduleNotificationPermissionPrompt() {
        if (permissionPromptScheduled) return;

        boolean firstLaunch = !AppPreferences.isNotificationIntroCompleted(this);
        boolean needsAccess = !NotificationPermissionHelper.canPostNotifications(this);
        if (!firstLaunch && !needsAccess) return;

        permissionPromptScheduled = true;
        getWindow().getDecorView().postDelayed(() -> {
            if (isFinishing()) return;
            if (!AppPreferences.isNotificationIntroCompleted(this)) {
                NotificationPermissionHelper.promptOnFirstLaunch(
                        this, notificationPermissionLauncher);
            } else if (!NotificationPermissionHelper.canPostNotifications(this)) {
                NotificationPermissionHelper.ensureNotificationAccess(
                        this, notificationPermissionLauncher);
            }
        }, 450);
    }
}
