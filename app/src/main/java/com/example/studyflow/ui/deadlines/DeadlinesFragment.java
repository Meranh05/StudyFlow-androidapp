package com.example.studyflow.ui.deadlines;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.FragmentDeadlinesBinding;
import com.example.studyflow.notification.DeadlineScheduler;
import com.example.studyflow.viewmodel.DeadlineViewModel;
import com.example.studyflow.viewmodel.SubjectViewModel;
import java.util.*;

public class DeadlinesFragment extends Fragment {
    private FragmentDeadlinesBinding binding;
    private DeadlineViewModel deadlineViewModel;
    private SubjectViewModel subjectViewModel;
    private DeadlineAdapter adapter;
    private String userId;
    private List<Deadline> allDeadlines = new ArrayList<>();
    private String currentFilter = "ALL";
    private String searchQuery = "";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeadlinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        deadlineViewModel = new ViewModelProvider(requireActivity()).get(DeadlineViewModel.class);
        subjectViewModel = new ViewModelProvider(requireActivity()).get(SubjectViewModel.class);

        setupRecyclerView();
        setupFilterChips();
        setupSearch();
        setupObservers();
        setupFab();

        deadlineViewModel.startListening(userId);
        subjectViewModel.startListening(userId);
    }

    private void setupRecyclerView() {
        adapter = new DeadlineAdapter(new DeadlineAdapter.OnDeadlineClickListener() {
            @Override
            public void onClick(Deadline deadline) {
                DeadlineDetailSheet.newInstance(userId, deadline)
                        .show(getChildFragmentManager(), "detail_deadline");
            }

            @Override
            public void onStatusChange(Deadline deadline, String newStatus) {
                deadline.setStatus(newStatus);
                deadline.setUpdatedAt(Timestamp.now());
                deadlineViewModel.updateDeadline(userId, deadline);
            }

            @Override
            public void onFlagColorChange(Deadline deadline, String flagColor) {
                deadline.setFlagColor(flagColor);
                deadline.setUpdatedAt(Timestamp.now());
                deadlineViewModel.updateDeadline(userId, deadline);
            }

            @Override
            public void onDelete(Deadline deadline) {
                deadlineViewModel.deleteDeadline(userId, deadline.getId());
            }
        });
        adapter.setDisplayMode(DeadlineAdapter.MODE_LIST);

        binding.rvDeadlines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDeadlines.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public int getSwipeDirs(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder holder) {
                if (holder instanceof DeadlineAdapter.HeaderViewHolder) return 0;
                return super.getSwipeDirs(rv, holder);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder a,
                                  @NonNull RecyclerView.ViewHolder b) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) {
                int position = holder.getBindingAdapterPosition();
                Deadline d = adapter.getDeadlineAt(position);
                if (d == null) {
                    adapter.notifyItemChanged(position);
                    return;
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa deadline")
                        .setMessage("Xóa \"" + d.getTitle() + "\"?")
                        .setPositiveButton("Xóa", (dialog, w) -> {
                                DeadlineScheduler.cancelReminder(requireContext().getApplicationContext(), d.getId());
                                deadlineViewModel.deleteDeadline(userId, d.getId());
                        })
                        .setNegativeButton("Hủy", (dialog, w) ->
                                adapter.notifyItemChanged(position))
                        .setOnCancelListener(dialog ->
                                adapter.notifyItemChanged(position))
                        .show();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvDeadlines);
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = "ALL";
            } else {
                int id = checkedIds.get(0);
                if (id == binding.chipAll.getId()) currentFilter = "ALL";
                else if (id == binding.chipOverdue.getId()) currentFilter = "OVERDUE";
                else if (id == binding.chipToday.getId()) currentFilter = "TODAY";
                else if (id == binding.chipUpcoming.getId()) currentFilter = "UPCOMING";
                else if (id == binding.chipTodo.getId()) currentFilter = "TODO";
                else if (id == binding.chipInProgress.getId()) currentFilter = "IN_PROGRESS";
                else if (id == binding.chipDone.getId()) currentFilter = "DONE";
                else if (id == binding.chipHigh.getId()) currentFilter = "HIGH";
            }
            applyFilter();
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilter() {
        List<Deadline> filteredList = new ArrayList<>();
        String query = searchQuery.toLowerCase(Locale.getDefault());
        Calendar now = startOfDay(Calendar.getInstance());

        for (Deadline d : allDeadlines) {
            if (!query.isEmpty()) {
                String title = d.getTitle() != null ? d.getTitle().toLowerCase(Locale.getDefault()) : "";
                if (!title.contains(query)) continue;
            }

            switch (currentFilter) {
                case "ALL": break;
                case "TODO": if (!"TODO".equals(d.getStatus())) continue; break;
                case "IN_PROGRESS": if (!"IN_PROGRESS".equals(d.getStatus())) continue; break;
                case "DONE": if (!"DONE".equals(d.getStatus())) continue; break;
                case "HIGH": if (!"HIGH".equals(d.getPriority())) continue; break;
                case "OVERDUE":
                case "TODAY":
                case "UPCOMING":
                    if (d.getDueDate() == null) continue;
                    Calendar due = startOfDay(Calendar.getInstance());
                    due.setTime(d.getDueDate().toDate());
                    if ("OVERDUE".equals(currentFilter) && !due.before(now)) continue;
                    if ("TODAY".equals(currentFilter) && !due.equals(now)) continue;
                    if ("UPCOMING".equals(currentFilter) && !due.after(now)) continue;
                    break;
                default: break;
            }

            filteredList.add(d);
        }

        Collections.sort(filteredList, (d1, d2) -> {
            if (d1.getDueDate() == null && d2.getDueDate() == null) return 0;
            if (d1.getDueDate() == null) return 1;
            if (d2.getDueDate() == null) return -1;
            return d1.getDueDate().compareTo(d2.getDueDate());
        });

        List<DeadlineAdapter.DeadlineListItem> displayList = new ArrayList<>();

        List<Deadline> before = new ArrayList<>();
        List<Deadline> today = new ArrayList<>();
        List<Deadline> upcoming = new ArrayList<>();

        for (Deadline d : filteredList) {
            if (d.getDueDate() == null) {
                upcoming.add(d);
                continue;
            }
            Calendar due = startOfDay(Calendar.getInstance());
            due.setTime(d.getDueDate().toDate());

            if (due.before(now)) before.add(d);
            else if (due.equals(now)) today.add(d);
            else upcoming.add(d);
        }

        if (!before.isEmpty()) {
            displayList.add(new DeadlineAdapter.DeadlineListItem("QUÁ HẠN", before.size()));
            for (Deadline d : before) displayList.add(new DeadlineAdapter.DeadlineListItem(d));
        }
        if (!today.isEmpty()) {
            displayList.add(new DeadlineAdapter.DeadlineListItem("HÔM NAY", today.size()));
            for (Deadline d : today) displayList.add(new DeadlineAdapter.DeadlineListItem(d));
        }
        if (!upcoming.isEmpty()) {
            displayList.add(new DeadlineAdapter.DeadlineListItem("SẮP TỚI", upcoming.size()));
            for (Deadline d : upcoming) displayList.add(new DeadlineAdapter.DeadlineListItem(d));
        }

        adapter.submitList(displayList);
        updateEmptyState(displayList.isEmpty());
    }

    private Calendar startOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private void updateSummary() {
        Calendar now = startOfDay(Calendar.getInstance());
        int overdue = 0, today = 0, upcoming = 0, pending = 0;

        for (Deadline d : allDeadlines) {
            if (d.getDueDate() == null || "DONE".equals(d.getStatus())) continue;
            pending++;
            Calendar due = startOfDay(Calendar.getInstance());
            due.setTime(d.getDueDate().toDate());
            if (due.before(now)) overdue++;
            else if (due.equals(now)) today++;
            else upcoming++;
        }

        setChipLabel(binding.chipOverdue, "Quá hạn", overdue);
        setChipLabel(binding.chipToday, "Hôm nay", today);
        setChipLabel(binding.chipUpcoming, "Sắp tới", upcoming);

        if (pending == 0) {
            binding.tvSubtitle.setText("Không có việc cần làm");
        } else {
            binding.tvSubtitle.setText(pending + " việc cần làm");
        }
    }

    private void setChipLabel(Chip chip, String label, int count) {
        chip.setText(count > 0 ? label + " · " + count : label);
    }

    private void setupObservers() {
        deadlineViewModel.deadlines.observe(getViewLifecycleOwner(), deadlines -> {
            binding.progressBar.setVisibility(View.GONE);
            allDeadlines = deadlines != null ? deadlines : new ArrayList<>();
            updateSummary();
            applyFilter();
        });
    }

    private void setupFab() {
        binding.fabAddDeadline.setOnClickListener(v ->
                AddEditDeadlineSheet.newInstance(userId, null)
                        .show(getChildFragmentManager(), "add_deadline"));
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.layoutEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvDeadlines.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (!isEmpty) return;

        if (!searchQuery.isEmpty()) {
            binding.layoutEmpty.tvEmptyTitle.setText("Không tìm thấy");
            binding.layoutEmpty.tvEmptySubtitle.setText("Thử từ khóa khác hoặc xóa bộ lọc");
        } else if (!"ALL".equals(currentFilter)) {
            binding.layoutEmpty.tvEmptyTitle.setText("Không có kết quả");
            binding.layoutEmpty.tvEmptySubtitle.setText("Thử đổi bộ lọc hoặc tạo deadline mới");
        } else {
            binding.layoutEmpty.tvEmptyTitle.setText("Chưa có deadline");
            binding.layoutEmpty.tvEmptySubtitle.setText("Nhấn + để tạo deadline mới");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
