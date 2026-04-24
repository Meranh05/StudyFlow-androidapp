package com.example.studyflow.ui.home;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.*;
import com.example.studyflow.R;
import com.example.studyflow.databinding.FragmentHomeBinding;
import com.example.studyflow.ui.deadlines.*;
import com.example.studyflow.utils.DateUtils;
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
        setupMiniStats();
        setupRecyclerView();
        setupQuickActions();
        setupObservers();

        deadlineViewModel.startListening(userId);
        deadlineViewModel.loadUpcoming(userId);
        subjectViewModel.startListening(userId);
    }

    private void setupHeader(FirebaseUser user) {
        String name = user.getDisplayName();
        if (name == null || name.isEmpty()) name = "bạn";
        // Lấy first name
        String firstName = name.split(" ")[name.split(" ").length - 1];
        binding.tvGreeting.setText(DateUtils.getGreeting(firstName));

        // Ngày hôm nay
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi"));
        binding.tvDate.setText(sdf.format(new Date()));

        // Avatar
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(binding.ivAvatar);
        }
    }

    private void setupMiniStats() {
        subjectViewModel.subjects.observe(getViewLifecycleOwner(), subjects ->
                binding.tvTotalSubjects.setText(String.valueOf(subjects.size())));

        deadlineViewModel.deadlines.observe(getViewLifecycleOwner(), deadlines -> {
            long pending = 0;
            for (var d : deadlines) {
                if (!"DONE".equals(d.getStatus())) pending++;
            }
            binding.tvPendingDeadlines.setText(String.valueOf(pending));
        });
    }

    private void setupRecyclerView() {
        adapter = new DeadlineAdapter(new DeadlineAdapter.OnDeadlineClickListener() {
            @Override public void onEdit(com.example.studyflow.data.model.Deadline d) {}
            @Override public void onDelete(com.example.studyflow.data.model.Deadline d) {}
            @Override
            public void onStatusChange(com.example.studyflow.data.model.Deadline d, String status) {
                d.setStatus(status);
                deadlineViewModel.updateDeadline(userId, d);
            }
        });
        binding.rvUpcomingDeadlines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingDeadlines.setAdapter(adapter);
    }

    private void setupQuickActions() {
        binding.btnAddTask.setOnClickListener(v -> {
            AddEditDeadlineSheet sheet = AddEditDeadlineSheet.newInstance(userId, null);
            sheet.show(getChildFragmentManager(), "add_deadline");
        });
        binding.btnAddSubject.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.nav_subjects));
        binding.tvSeeAll.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.nav_deadlines));
        binding.fabAdd.setOnClickListener(v -> {
            AddEditDeadlineSheet sheet = AddEditDeadlineSheet.newInstance(userId, null);
            sheet.show(getChildFragmentManager(), "add_deadline_fab");
        });
    }

    private void setupObservers() {
        deadlineViewModel.upcomingDeadlines.observe(getViewLifecycleOwner(), deadlines -> {
            binding.progressBar.setVisibility(View.GONE);
            if (deadlines.isEmpty()) {
                binding.layoutEmpty.setVisibility(View.VISIBLE);
                binding.rvUpcomingDeadlines.setVisibility(View.GONE);
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.rvUpcomingDeadlines.setVisibility(View.VISIBLE);
                adapter.submitList(deadlines);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}