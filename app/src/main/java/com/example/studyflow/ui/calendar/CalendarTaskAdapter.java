package com.example.studyflow.ui.calendar;

import android.os.Handler;
import android.os.Looper;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.ItemCalendarTaskBinding;
import com.example.studyflow.databinding.ItemDeadlineHeaderBinding;
import com.example.studyflow.utils.DateUtils;
import java.util.*;

public class CalendarTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    public static class TaskListItem {
        public int type;
        public String headerTitle;
        public Deadline deadline;

        public TaskListItem(String headerTitle) {
            this.type = TYPE_HEADER;
            this.headerTitle = headerTitle;
        }

        public TaskListItem(Deadline deadline) {
            this.type = TYPE_TASK;
            this.deadline = deadline;
        }
    }

    public interface OnTaskClickListener {
        void onTaskClick(Deadline deadline);
    }

    private List<TaskListItem> items = new ArrayList<>();
    private final OnTaskClickListener listener;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = this::notifyDataSetChanged;

    public CalendarTaskAdapter(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TaskListItem> list) {
        refreshHandler.removeCallbacks(refreshRunnable);
        this.items = new ArrayList<>(list);
        notifyDataSetChanged();
        scheduleRefresh();
    }

    private void scheduleRefresh() {
        boolean hasLate = false;
        for (TaskListItem item : items) {
            if (item.type == TYPE_TASK && item.deadline != null) {
                if (DateUtils.isLate(item.deadline)) {
                    hasLate = true;
                    break;
                }
            }
        }
        if (hasLate) {
            refreshHandler.postDelayed(refreshRunnable, 60_000);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(ItemDeadlineHeaderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }
        return new TaskViewHolder(ItemCalendarTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(items.get(position).headerTitle);
        } else {
            ((TaskViewHolder) holder).bind(items.get(position).deadline);
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeadlineHeaderBinding binding;
        HeaderViewHolder(ItemDeadlineHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(String title) { binding.tvHeaderTitle.setText(title); }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarTaskBinding binding;

        TaskViewHolder(ItemCalendarTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Deadline deadline) {
            binding.tvTitle.setText(deadline.getTitle());
            binding.tvSubtitle.setText(DateUtils.formatCalendarSubtitle(deadline));

            boolean isDone = "DONE".equals(deadline.getStatus());
            boolean isLate = DateUtils.isLate(deadline);

            if (isDone) {
                binding.layoutStatusIcon.setBackgroundResource(R.drawable.bg_status_icon_done);
                binding.ivStatus.setImageResource(R.drawable.ic_check_circle);
                binding.ivStatus.setColorFilter(
                        binding.getRoot().getContext().getColor(R.color.status_done));
            } else if (isLate) {
                binding.layoutStatusIcon.setBackgroundResource(R.drawable.bg_status_icon_late);
                binding.ivStatus.setImageResource(R.drawable.ic_clock);
                binding.ivStatus.setColorFilter(
                        binding.getRoot().getContext().getColor(R.color.primary));
            } else {
                binding.layoutStatusIcon.setBackgroundResource(R.drawable.bg_status_icon_late);
                binding.ivStatus.setImageResource(R.drawable.ic_clock);
                binding.ivStatus.setColorFilter(
                        binding.getRoot().getContext().getColor(R.color.primary));
            }

            if (isDone) {
                if (DateUtils.isCompletedLate(deadline)) {
                    binding.tvStatusBadge.setText(DateUtils.formatLateBadge(deadline));
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_late);
                    binding.tvStatusBadge.setTextColor(
                            binding.getRoot().getContext().getColor(R.color.primary));
                } else {
                    binding.tvStatusBadge.setText("ĐÚNG HẠN");
                    binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_on_time);
                    binding.tvStatusBadge.setTextColor(
                            binding.getRoot().getContext().getColor(R.color.status_done));
                }
            } else if (isLate) {
                binding.tvStatusBadge.setText(DateUtils.formatLateMinutes(deadline));
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_late);
                binding.tvStatusBadge.setTextColor(
                        binding.getRoot().getContext().getColor(R.color.primary));
            } else {
                binding.tvStatusBadge.setText("ĐÚNG HẠN");
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_on_time);
                binding.tvStatusBadge.setTextColor(
                        binding.getRoot().getContext().getColor(R.color.status_done));
            }

            binding.getRoot().setOnClickListener(v -> listener.onTaskClick(deadline));
        }
    }
}
