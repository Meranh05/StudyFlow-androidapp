package com.example.studyflow.ui.home;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.*;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.FragmentHomeBinding;
import com.example.studyflow.ui.deadlines.*;
import com.example.studyflow.utils.DateUtils;
import com.example.studyflow.utils.LocalAvatarStorage;
import com.example.studyflow.viewmodel.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private DeadlineViewModel deadlineViewModel;
    private SubjectViewModel subjectViewModel;
    private DeadlineAdapter adapter;
    private String userId;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        userId = user.getUid();

        deadlineViewModel = new ViewModelProvider(requireActivity()).get(DeadlineViewModel.class);
        subjectViewModel = new ViewModelProvider(requireActivity()).get(SubjectViewModel.class);

        setupHeader(user);
        setupRecyclerView();
        setupQuickActions();
        setupObservers();

        deadlineViewModel.startListening(userId);
        subjectViewModel.startListening(userId);
    }

    private void setupHeader(FirebaseUser user) {
        String name = user.getDisplayName();
        if (name == null || name.isEmpty()) name = "Mera";
        String firstName = name.split(" ")[name.split(" ").length - 1];
        binding.tvGreeting.setText(DateUtils.getGreeting(firstName));

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi"));
        binding.tvDate.setText(sdf.format(new Date()));

        loadHeaderAvatar(user);
    }

    private void loadHeaderAvatar(FirebaseUser user) {
        if (LocalAvatarStorage.exists(requireContext(), userId)) {
            LocalAvatarStorage.loadInto(this, userId, binding.ivAvatar);
        } else if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(binding.ivAvatar);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId == null || binding == null) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) loadHeaderAvatar(user);
    }

    private void setupRecyclerView() {
        adapter = new DeadlineAdapter(new DeadlineAdapter.OnDeadlineClickListener() {
            @Override
            public void onClick(Deadline d) {
                DeadlineDetailSheet.newInstance(userId, d)
                        .show(getChildFragmentManager(), "detail_home");
            }

            @Override
            public void onStatusChange(Deadline d, String status) {
                d.setStatus(status);
                deadlineViewModel.updateDeadline(userId, d);
            }

            @Override
            public void onFlagColorChange(Deadline d, String flagColor) {
                d.setFlagColor(flagColor);
                d.setUpdatedAt(Timestamp.now());
                deadlineViewModel.updateDeadline(userId, d);
            }

            @Override
            public void onDelete(Deadline d) {
                deadlineViewModel.deleteDeadline(userId, d.getId());
            }
        });
        adapter.setDisplayMode(DeadlineAdapter.MODE_HOME);
        binding.rvUpcomingDeadlines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingDeadlines.setAdapter(adapter);
    }

    private void setupQuickActions() {
        binding.btnAddTask.setOnClickListener(v ->
                AddEditDeadlineSheet.newInstance(userId, null).show(getChildFragmentManager(), "add"));
        binding.btnAddSubject.setOnClickListener(v -> navigateToTab(R.id.nav_subjects));
        binding.btnAddSchedule.setOnClickListener(v -> navigateToTab(R.id.nav_calendar));
        binding.tvSeeAll.setOnClickListener(v -> navigateToTab(R.id.nav_deadlines));
        binding.fabAdd.setOnClickListener(v ->
                AddEditDeadlineSheet.newInstance(userId, null).show(getChildFragmentManager(), "fab_add"));
    }

    private void navigateToTab(int destinationId) {
        NavController navController = Navigation.findNavController(requireView());
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.nav_home, false, true)
                .build();
        navController.navigate(destinationId, null, options);
    }

    private void setupObservers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        deadlineViewModel.deadlines.observe(getViewLifecycleOwner(), deadlines -> {
            if (deadlines == null) return;
            binding.progressBar.setVisibility(View.GONE);

            long now = System.currentTimeMillis();
            int upcoming = 0;
            int overdue = 0;

            List<Deadline> display = new ArrayList<>();
            for (Deadline d : deadlines) {
                if ("DONE".equals(d.getStatus()) || d.getDueDate() == null) continue;
                long dueTime = d.getDueDate().toDate().getTime();
                if (dueTime < now) {
                    overdue++;
                } else {
                    upcoming++;
                }
                display.add(d);
            }

            binding.tvUpcomingCount.setText(String.valueOf(upcoming));
            binding.tvOverdueCount.setText(String.valueOf(overdue));

            Collections.sort(display, (d1, d2) -> d1.getDueDate().compareTo(d2.getDueDate()));
            if (display.size() > 5) {
                display = display.subList(0, 5);
            }

            if (display.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvUpcomingDeadlines.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvUpcomingDeadlines.setVisibility(View.VISIBLE);
                List<DeadlineAdapter.DeadlineListItem> displayList = new ArrayList<>();
                for (Deadline d : display) {
                    displayList.add(new DeadlineAdapter.DeadlineListItem(d));
                }
                adapter.submitList(displayList);
            }
        });

        subjectViewModel.subjects.observe(getViewLifecycleOwner(), subjects -> {
            if (subjects != null) binding.tvTotalSubjects.setText(String.valueOf(subjects.size()));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
