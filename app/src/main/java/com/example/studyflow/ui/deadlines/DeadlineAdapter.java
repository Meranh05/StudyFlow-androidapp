package com.example.studyflow.ui.deadlines;

import android.graphics.Color;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.ItemDeadlineBinding;
import com.example.studyflow.utils.DateUtils;
import java.util.*;

public class DeadlineAdapter extends RecyclerView.Adapter<DeadlineAdapter.ViewHolder> {
    private List<Deadline> deadlines = new ArrayList<>();
    private final OnDeadlineClickListener listener;

    public interface OnDeadlineClickListener {
        void onClick(Deadline deadline);
        void onEdit(Deadline deadline);
        void onDelete(Deadline deadline);
        void onStatusChange(Deadline deadline, String newStatus);
    }

    public DeadlineAdapter(OnDeadlineClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Deadline> list) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return deadlines.size(); }
            @Override public int getNewListSize() { return list.size(); }
            @Override public boolean areItemsTheSame(int oldPos, int newPos) {
                return Objects.equals(deadlines.get(oldPos).getId(), list.get(newPos).getId());
            }
            @Override public boolean areContentsTheSame(int oldPos, int newPos) {
                return Objects.equals(deadlines.get(oldPos).getTitle(), list.get(newPos).getTitle())
                        && Objects.equals(deadlines.get(oldPos).getStatus(), list.get(newPos).getStatus());
            }
        });
        this.deadlines = new ArrayList<>(list);
        result.dispatchUpdatesTo(this);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeadlineBinding binding = ItemDeadlineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(deadlines.get(position));
    }

    @Override public int getItemCount() { return deadlines.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeadlineBinding binding;

        ViewHolder(ItemDeadlineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Deadline deadline) {
            binding.tvTitle.setText(deadline.getTitle());
            binding.chipSubject.setText(deadline.getSubjectName());
            binding.tvDueDate.setText(DateUtils.formatDateTime(deadline.getDueDate()));

            // Priority color bar
            int priorityColor;
            switch (deadline.getPriority()) {
                case "HIGH":   priorityColor = Color.parseColor("#FFCDD2"); break;
                case "MEDIUM": priorityColor = Color.parseColor("#FFE0B2"); break;
                default:       priorityColor = Color.parseColor("#C8E6C9"); break;
            }
            binding.viewPriority.setBackgroundColor(priorityColor);

            // Status badge
            String statusText;
            int statusBg, statusTextColor;
            switch (deadline.getStatus()) {
                case "DONE":
                    statusText = "Hoàn thành";
                    statusBg = Color.parseColor("#E8F5E9");
                    statusTextColor = Color.parseColor("#2E7D32");
                    break;
                case "IN_PROGRESS":
                    statusText = "Đang làm";
                    statusBg = Color.parseColor("#E3F2FD");
                    statusTextColor = Color.parseColor("#1565C0");
                    break;
                default:
                    statusText = "Chưa làm";
                    statusBg = Color.parseColor("#F5F5F5");
                    statusTextColor = Color.parseColor("#757575");
            }
            binding.tvStatus.setText(statusText);
            binding.tvStatus.setBackgroundColor(statusBg);
            binding.tvStatus.setTextColor(statusTextColor);

            // Item click → show details
            binding.getRoot().setOnClickListener(v -> listener.onClick(deadline));

            // Long press → edit/delete menu
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onEdit(deadline);
                return true;
            });

            // Click status → cycle status
            binding.tvStatus.setOnClickListener(v -> {
                String next;
                switch (deadline.getStatus()) {
                    case "TODO":        next = "IN_PROGRESS"; break;
                    case "IN_PROGRESS": next = "DONE"; break;
                    default:            next = "TODO"; break;
                }
                listener.onStatusChange(deadline, next);
            });
        }
    }
}