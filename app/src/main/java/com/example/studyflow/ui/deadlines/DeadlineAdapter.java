package com.example.studyflow.ui.deadlines;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.*;
import com.example.studyflow.R;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.databinding.ItemDeadlineBinding;
import com.example.studyflow.databinding.ItemDeadlineHeaderBinding;
import com.example.studyflow.utils.DateUtils;
import java.util.*;

public class DeadlineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int MODE_HOME = 0;
    public static final int MODE_LIST = 1;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public static class DeadlineListItem {
        public int type;
        public String headerTitle;
        public int headerCount;
        public Deadline deadline;

        public DeadlineListItem(String headerTitle, int count) {
            this.type = TYPE_HEADER;
            this.headerTitle = headerTitle;
            this.headerCount = count;
        }

        public DeadlineListItem(Deadline deadline) {
            this.type = TYPE_ITEM;
            this.deadline = deadline;
        }
    }

    private List<DeadlineListItem> items = new ArrayList<>();
    private final OnDeadlineClickListener listener;
    private int displayMode = MODE_HOME;

    public interface OnDeadlineClickListener {
        void onClick(Deadline deadline);
        void onStatusChange(Deadline deadline, String newStatus);
        void onFlagColorChange(Deadline deadline, String flagColor);
        void onDelete(Deadline deadline);
    }

    public DeadlineAdapter(OnDeadlineClickListener listener) {
        this.listener = listener;
    }

    public void setDisplayMode(int mode) {
        this.displayMode = mode;
        notifyDataSetChanged();
    }

    public void submitList(List<DeadlineListItem> list) {
        this.items = new ArrayList<>(list);
        notifyDataSetChanged();
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
        return new ItemViewHolder(ItemDeadlineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            DeadlineListItem item = items.get(position);
            ((HeaderViewHolder) holder).bind(item.headerTitle, item.headerCount);
        } else {
            ((ItemViewHolder) holder).bind(items.get(position).deadline);
        }
    }

    @Override public int getItemCount() { return items.size(); }

    public Deadline getDeadlineAt(int position) {
        if (position >= 0 && position < items.size()) {
            DeadlineListItem item = items.get(position);
            if (item.type == TYPE_ITEM) return item.deadline;
        }
        return null;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeadlineHeaderBinding binding;

        HeaderViewHolder(ItemDeadlineHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String title, int count) {
            binding.tvHeaderTitle.setText(title);
            binding.tvHeaderCount.setText(String.valueOf(count));

            int color;
            if ("QUÁ HẠN".equals(title)) {
                color = ContextCompat.getColor(binding.getRoot().getContext(), R.color.status_overdue_soft);
            } else if ("HÔM NAY".equals(title)) {
                color = ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary);
            } else {
                color = ContextCompat.getColor(binding.getRoot().getContext(), R.color.text_hint);
            }
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(color);
            binding.viewHeaderDot.setBackground(dot);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemDeadlineBinding binding;

        ItemViewHolder(ItemDeadlineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Deadline deadline) {
            binding.tvTitle.setText(deadline.getTitle());

            String subjectName = deadline.getSubjectName();
            if (subjectName == null || subjectName.isEmpty()) {
                binding.chipSubject.setText("CHUNG");
            } else {
                binding.chipSubject.setText(subjectName.toUpperCase(Locale.getDefault()));
            }
            applySubjectChip(binding.chipSubject, subjectName);

            String priority = deadline.getPriority() != null ? deadline.getPriority() : "MEDIUM";
            applyPriorityChip(binding.chipPriority, priority);
            applyPriorityBar(binding.viewPriorityBar, priority);

            binding.chipDate.setVisibility(View.VISIBLE);
            binding.chipTime.setVisibility(View.VISIBLE);
            binding.chipDate.setText(DateUtils.formatDateLabel(deadline.getDueDate()));
            binding.chipTime.setText(DateUtils.formatTimeLabel(deadline.getDueDate()));
            binding.layoutTime.setVisibility(View.GONE);

            applyFlagChip(binding.chipFlag, deadline.getFlagColor());
            binding.chipFlag.setOnClickListener(v -> showFlagColorPicker(v, deadline));

            boolean isListMode = displayMode == MODE_LIST;
            boolean isDone = "DONE".equals(deadline.getStatus());
            boolean isLate = !isDone && DateUtils.isLate(deadline);

            binding.viewPriorityBar.setVisibility(isListMode ? View.VISIBLE : View.GONE);

            if (isListMode) {
                binding.tvLateBadge.setBackgroundResource(R.drawable.bg_status_late);
                binding.tvLateBadge.setTextColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary));

                if (isLate) {
                    binding.tvLateBadge.setVisibility(View.VISIBLE);
                    binding.tvLateBadge.setText(DateUtils.formatLateMinutes(deadline));
                } else if (isDone) {
                    binding.tvLateBadge.setVisibility(View.VISIBLE);
                    binding.tvLateBadge.setText("XONG");
                    binding.tvLateBadge.setBackgroundResource(R.drawable.bg_status_on_time);
                    binding.tvLateBadge.setTextColor(
                            ContextCompat.getColor(binding.getRoot().getContext(), R.color.status_done_soft));
                } else {
                    binding.tvLateBadge.setVisibility(View.GONE);
                }
            } else {
                binding.tvLateBadge.setVisibility(View.GONE);
            }

            if (isListMode) {
                binding.cbDone.setVisibility(View.VISIBLE);
                binding.cbDone.setImageResource(isDone
                        ? R.drawable.ic_check_circle
                        : R.drawable.bg_checkbox_unchecked);
                binding.cbDone.setOnClickListener(v -> showStatusDialog(v, deadline, isDone));
            } else {
                binding.cbDone.setVisibility(View.GONE);
            }

            binding.tvTitle.setAlpha(isDone ? 0.45f : 1f);
            binding.getRoot().setAlpha(isDone && isListMode ? 0.85f : 1f);
            binding.getRoot().setOnClickListener(v -> listener.onClick(deadline));
        }

        private void showFlagColorPicker(View anchor, Deadline deadline) {
            if (!(anchor.getContext() instanceof FragmentActivity)) return;
            FragmentActivity activity = (FragmentActivity) anchor.getContext();
            FlagColorSheet.newInstance(deadline.getFlagColor())
                    .setOnFlagColorSelectedListener(color ->
                            listener.onFlagColorChange(deadline, color))
                    .show(activity.getSupportFragmentManager(), "flag_color");
        }

        private void showStatusDialog(View v, Deadline deadline, boolean isDone) {
            if (isDone) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Xác nhận")
                        .setMessage("Đánh dấu chưa hoàn thành?")
                        .setPositiveButton("Xác nhận", (d, w) ->
                                listener.onStatusChange(deadline, "TODO"))
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Hoàn thành")
                        .setMessage("Xác nhận đã hoàn thành \"" + deadline.getTitle() + "\"?")
                        .setPositiveButton("Hoàn thành", (d, w) ->
                                listener.onStatusChange(deadline, "DONE"))
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        }
    }

    private static void applyFlagChip(ImageView chip, String flagColor) {
        int tint;
        if (flagColor == null || flagColor.isEmpty()) {
            tint = Color.parseColor("#C7C7CC");
        } else {
            tint = Color.parseColor(flagColor);
        }
        chip.setColorFilter(tint);
        chip.setImageResource(R.drawable.ic_flag);
    }

    private static void applyPriorityBar(View bar, String priority) {
        int color;
        switch (priority) {
            case "HIGH": color = Color.parseColor("#E07A7A"); break;
            case "LOW": color = Color.parseColor("#D1D1D6"); break;
            default: color = Color.parseColor("#FF8A00"); break;
        }
        bar.setBackgroundColor(color);
    }

    public static void applySubjectChip(TextView chip, String subjectName) {
        String[][] palettes = {
                {"#FFF5EB", "#FF8A00"},
                {"#F4F4F5", "#8E8E93"},
                {"#FFF0F0", "#E07A7A"},
                {"#FFF5EB", "#FF8A00"}
        };
        int idx = Math.abs((subjectName != null ? subjectName : "CHUNG").hashCode()) % palettes.length;
        setChipStyle(chip, palettes[idx][0], palettes[idx][1]);
    }

    public static void applyPriorityChip(TextView chip, String priority) {
        switch (priority) {
            case "HIGH":
                chip.setText("CAO");
                setChipStyle(chip, "#FFF0F0", "#E07A7A");
                break;
            case "LOW":
                chip.setText("THẤP");
                setChipStyle(chip, "#F4F4F5", "#8E8E93");
                break;
            default:
                chip.setText("TB");
                setChipStyle(chip, "#FFF5EB", "#FF8A00");
                break;
        }
    }

    private static void setChipStyle(TextView chip, String bgColor, String textColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(16f);
        bg.setColor(Color.parseColor(bgColor));
        chip.setBackground(bg);
        chip.setTextColor(Color.parseColor(textColor));
    }
}
