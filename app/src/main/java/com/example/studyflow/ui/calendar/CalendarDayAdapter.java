package com.example.studyflow.ui.calendar;

import android.graphics.Color;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.R;
import com.example.studyflow.databinding.ItemCalendarDayBinding;
import java.util.*;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    public static class DayItem {
        public final int day;
        public final boolean inMonth;
        public final boolean selected;
        public final boolean hasTask;

        public DayItem(int day, boolean inMonth, boolean selected, boolean hasTask) {
            this.day = day;
            this.inMonth = inMonth;
            this.selected = selected;
            this.hasTask = hasTask;
        }
    }

    public interface OnDayClickListener {
        void onDayClick(int year, int month, int day);
    }

    private List<DayItem> days = new ArrayList<>();
    private int year;
    private int month;
    private final OnDayClickListener listener;

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setMonth(int year, int month, List<DayItem> days) {
        this.year = year;
        this.month = month;
        this.days = new ArrayList<>(days);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DayViewHolder(ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override public int getItemCount() { return days.size(); }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarDayBinding binding;

        DayViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DayItem item) {
            if (!item.inMonth) {
                binding.tvDay.setText("");
                binding.viewDot.setVisibility(View.GONE);
                binding.tvDay.setBackground(null);
                binding.getRoot().setOnClickListener(null);
                return;
            }

            binding.tvDay.setText(String.valueOf(item.day));
            binding.viewDot.setVisibility(item.hasTask ? View.VISIBLE : View.GONE);

            if (item.selected) {
                binding.tvDay.setBackgroundResource(R.drawable.bg_calendar_selected);
                binding.tvDay.setTextColor(Color.WHITE);
            } else {
                binding.tvDay.setBackground(null);
                binding.tvDay.setTextColor(
                        binding.getRoot().getContext().getColor(R.color.text_primary));
            }

            binding.getRoot().setOnClickListener(v ->
                    listener.onDayClick(year, month, item.day));
        }
    }
}
