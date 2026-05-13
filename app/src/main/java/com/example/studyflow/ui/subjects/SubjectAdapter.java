package com.example.studyflow.ui.subjects;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.*;
import com.example.studyflow.data.model.Subject;
import com.example.studyflow.databinding.ItemSubjectBinding;
import java.util.*;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {
    private List<Subject> subjects = new ArrayList<>();
    private final OnSubjectClickListener listener;

    public interface OnSubjectClickListener {
        void onClick(Subject subject);
        void onEdit(Subject subject);
        void onDelete(Subject subject);
    }

    public SubjectAdapter(OnSubjectClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Subject> list) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return subjects.size(); }
            @Override public int getNewListSize() { return list.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return Objects.equals(subjects.get(o).getId(), list.get(n).getId());
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return Objects.equals(subjects.get(o).getName(), list.get(n).getName()) &&
                       Objects.equals(subjects.get(o).getLecturer(), list.get(n).getLecturer()) &&
                       subjects.get(o).getCredits() == list.get(n).getCredits() &&
                       Objects.equals(subjects.get(o).getColorTag(), list.get(n).getColorTag());
            }
        });
        this.subjects = new ArrayList<>(list);
        result.dispatchUpdatesTo(this);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSubjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(subjects.get(position));
    }

    @Override public int getItemCount() { return subjects.size(); }

    public Subject getSubjectAt(int position) {
        if (position < 0 || position >= subjects.size()) return null;
        return subjects.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubjectBinding binding;

        ViewHolder(ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Subject subject) {
            String name = subject.getName() != null ? subject.getName() : "Môn học";
            binding.tvSubjectName.setText(name);

            String lecturer = subject.getLecturer();
            binding.tvLecturer.setText(
                    lecturer == null || lecturer.isEmpty()
                            ? "Chưa có giảng viên"
                            : "GV: " + lecturer);

            binding.tvCredits.setText(subject.getCredits() + " TC");

            String initial = name.trim().isEmpty() ? "?" : name.trim().substring(0, 1).toUpperCase(Locale.ROOT);
            binding.tvInitial.setText(initial);

            int color = parseColor(subject.getColorTag(), "#5E92F3");
            applyColorAccent(color);

            binding.getRoot().setOnClickListener(v -> listener.onClick(subject));

            binding.btnMore.setOnClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), v);
                popup.getMenu().add("Chỉnh sửa");
                popup.getMenu().add("Xóa");
                popup.setOnMenuItemClickListener(item -> {
                    if ("Chỉnh sửa".equals(item.getTitle().toString())) {
                        listener.onEdit(subject);
                    } else {
                        listener.onDelete(subject);
                    }
                    return true;
                });
                popup.show();
            });
        }

        private void applyColorAccent(int color) {
            binding.viewColorBar.setBackgroundColor(color);

            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setCornerRadius(14f);
            iconBg.setColor(color);
            binding.viewColorTag.setBackground(iconBg);

            int lightBg = ColorUtils.setAlphaComponent(color, 28);
            GradientDrawable chipBg = new GradientDrawable();
            chipBg.setCornerRadius(20f);
            chipBg.setColor(lightBg);
            binding.tvCredits.setBackground(chipBg);
            binding.tvCredits.setTextColor(darkenForText(color));
        }

        private int darkenForText(int color) {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] = Math.max(0.25f, hsv[2] * 0.72f);
            return Color.HSVToColor(hsv);
        }

        private int parseColor(String hex, String fallback) {
            try {
                return Color.parseColor(hex != null ? hex : fallback);
            } catch (Exception e) {
                return Color.parseColor(fallback);
            }
        }
    }
}
