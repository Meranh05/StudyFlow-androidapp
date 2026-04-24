package com.example.studyflow.ui.notes;

import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.example.studyflow.data.model.Note;
import com.example.studyflow.databinding.ItemNoteBinding;
import com.example.studyflow.utils.DateUtils;
import java.util.*;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onClick(Note note);
        void onDelete(Note note);
    }

    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Note> list) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return notes.size(); }
            @Override public int getNewListSize() { return list.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return Objects.equals(notes.get(o).getId(), list.get(n).getId());
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                return Objects.equals(notes.get(o).getTitle(), list.get(n).getTitle())
                        && Objects.equals(notes.get(o).getContent(), list.get(n).getContent());
            }
        });
        this.notes = new ArrayList<>(list);
        result.dispatchUpdatesTo(this);
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemNoteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(notes.get(position));
    }

    @Override public int getItemCount() { return notes.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNoteBinding binding;

        ViewHolder(ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Note note) {
            binding.tvTitle.setText(
                    note.getTitle() != null && !note.getTitle().isEmpty()
                            ? note.getTitle() : "Ghi chú không có tiêu đề");

            binding.tvContentPreview.setText(note.getContent());
            binding.chipSubject.setText(note.getSubjectName());
            binding.tvUpdatedAt.setText(DateUtils.formatDate(note.getUpdatedAt()));

            // Click → mở detail
            binding.getRoot().setOnClickListener(v -> listener.onClick(note));

            // Long press → xóa
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onDelete(note);
                return true;
            });
        }
    }
}