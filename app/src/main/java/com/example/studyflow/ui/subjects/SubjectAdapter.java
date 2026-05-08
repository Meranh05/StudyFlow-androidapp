package com.example.studyflow.ui.subjects;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import androidx.annotation.NonNull;
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSubjectBinding binding;

        ViewHolder(ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Subject subject) {
            binding.tvSubjectName.setText(subject.getName());
            binding.tvLecturer.setText(subject.getLecturer());
            binding.tvCredits.setText(subject.getCredits() + " tín chỉ");

            // Set color circle
            try {
                GradientDrawable circle = new GradientDrawable();
                circle.setShape(GradientDrawable.OVAL);
                circle.setColor(Color.parseColor(subject.getColorTag()));
                binding.viewColorTag.setBackground(circle);
            } catch (Exception e) {
                binding.viewColorTag.setBackgroundColor(Color.parseColor("#5E92F3"));
            }

            // Item click
            binding.getRoot().setOnClickListener(v -> listener.onClick(subject));

            // More button popup menu
            binding.btnMore.setOnClickListener(v -> {
                android.widget.PopupMenu popup =
                        new android.widget.PopupMenu(v.getContext(), v);
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
    }
}